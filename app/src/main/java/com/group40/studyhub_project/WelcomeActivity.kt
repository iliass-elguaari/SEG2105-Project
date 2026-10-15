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
    onSignOut: () -> Unit
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

        Button(onClick= onSignOut) {
            Text("Sign Out")
        }
    }
}
