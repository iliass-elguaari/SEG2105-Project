package com.group40.studyhub_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

// Admin-only screen listing every registered account.
// Owned by member 4 (docs, UML and admin user list).
class UserListActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only the admin may see the user list.
        val user= SessionManager(this).currentUser()
        if (user== null || user.role!= "admin") {
            finish()
            return
        }

        val users= UserDatabase(this).getAllUsers()

        setContent {
            StudyHubProjectTheme {
                UserListScreen(
                    users= users,
                    onBack= { finish() }
                )
            }
        }
    }
}

@Composable
fun UserListScreen(
    users: List<User>,
    onBack: () -> Unit
) {
    Column(
        modifier= Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally
    ) {
        Text("All Users (${users.size})", style= MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier= Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement= Arrangement.spacedBy(8.dp)
        ) {
            items(users, key= { it.username }) { user ->
                UserRow(user)
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick= onBack, modifier= Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

// The password is never shown.
@Composable
fun UserRow(user: User) {
    Card(modifier= Modifier.fillMaxWidth()) {
        Column(modifier= Modifier.padding(16.dp)) {
            Text(
                "${user.firstName} ${user.lastName}",
                style= MaterialTheme.typography.titleMedium
            )
            Text("Username: ${user.username}")
            Text("Email: ${user.email}")
            Text("Role: ${SessionManager.roleLabel(user.role)}")
        }
    }
}
