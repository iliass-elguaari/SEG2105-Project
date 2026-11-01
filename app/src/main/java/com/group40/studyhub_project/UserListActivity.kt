package com.group40.studyhub_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

// Admin-only screen: lists every account and lets the admin
// disable, enable or delete students and group leaders.
class UserListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only the admin may open this screen.
        val user = SessionManager(this).currentUser()
        if (user == null || user.role != "admin") {
            finish()
            return
        }

        val db = UserDatabase(this)

        setContent {
            StudyHubProjectTheme {
                UserListScreen(db = db, onBack = { finish() })
            }
        }
    }
}

// The action the admin picked, waiting for "Yes" in the popup.
enum class UserAction { DISABLE, ENABLE, DELETE }

@Composable
fun UserListScreen(db: UserDatabase, onBack: () -> Unit) {
    var users by remember { mutableStateOf(db.getAllUsers()) }
    var pending by remember { mutableStateOf<Pair<User, UserAction>?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("All Users (${users.size})", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.username }) { user ->
                UserRow(user = user, onAction = { action -> pending = user to action })
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }

    // Confirmation popup: nothing changes until the admin taps "Yes".
    pending?.let { (user, action) ->
        val verb = when (action) {
            UserAction.DISABLE -> "disable"
            UserAction.ENABLE -> "enable"
            UserAction.DELETE -> "delete"
        }
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Are you sure?") },
            text = { Text("Do you want to $verb ${user.firstName} ${user.lastName} (${user.username})?") },
            confirmButton = {
                TextButton(onClick = {
                    when (action) {
                        UserAction.DISABLE -> db.disableUser(user.username)
                        UserAction.ENABLE -> db.enableUser(user.username)
                        UserAction.DELETE -> db.deleteUser(user.username)
                    }
                    users = db.getAllUsers()   // reload so the list updates
                    pending = null
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            }
        )
    }
}

// One card per user. The password is never shown.
@Composable
fun UserRow(user: User, onAction: (UserAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
            Text("Username: ${user.username}")
            Text("Email: ${user.email}")
            Text("Role: ${SessionManager.roleLabel(user.role)}")
            Text("Status: ${user.status.replaceFirstChar { it.uppercase() }}")

            // Students and group leaders only, never the admin.
            if (user.role != "admin") {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user.status == "active") {
                        OutlinedButton(onClick = { onAction(UserAction.DISABLE) }) { Text("Disable") }
                    } else {
                        OutlinedButton(onClick = { onAction(UserAction.ENABLE) }) { Text("Enable") }
                    }
                    Button(
                        onClick = { onAction(UserAction.DELETE) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                }
            }
        }
    }
}