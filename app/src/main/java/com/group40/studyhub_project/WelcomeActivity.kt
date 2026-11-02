package com.group40.studyhub_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

class WelcomeActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session= SessionManager(this)
        val user= session.currentUser()

        // No session (e.g. it was cleared): go back to login.
        if (user== null) {
            openLogin()
            return
        }

        setContent {
            StudyHubProjectTheme {
                WelcomeScreen(
                    firstName= user.firstName,
                    role= SessionManager.roleLabel(user.role),

                    // Only the admin can open Category Management.
                    onViewCategories = if (user.role == "admin") {
                        { startActivity(Intent(this, CategoryActivity::class.java)) }
                    } else null,
                    // Only the admin gets the "View All Users" button.
                    onViewUsers= if (user.role== "admin") {
                        { startActivity(Intent(this, UserListActivity::class.java)) }
                    } else null,
                    onSignOut= {
                        session.endSession()
                        openLogin()
                    }
                )
            }
        }
    }

    private fun openLogin() {
        val intent= Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

@Composable
fun WelcomeScreen(
    firstName: String,
    role: String,
    onSignOut: () -> Unit,
    onViewCategories: (() -> Unit)? = null,
    onViewUsers: (() -> Unit)?= null
) {
    Column(
        modifier= Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ) {
        Text(
            "Welcome $firstName! You are logged in as \"$role\".",
            style= MaterialTheme.typography.headlineSmall,
            textAlign= TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        if (onViewCategories != null) {
            Button(onClick = onViewCategories) {
                Text("Categories")
            }

            Spacer(Modifier.height(12.dp))
        }

        if (onViewUsers!= null) {
            Button(onClick= onViewUsers) {
                Text("Users")
            }

            Spacer(Modifier.height(12.dp))
        }

        Button(onClick= onSignOut) {
            Text("Logout")
        }
    }
}
