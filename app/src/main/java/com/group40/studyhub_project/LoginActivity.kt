package com.group40.studyhub_project

import android.R.attr.password
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.AuthManager
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

class LoginActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session= SessionManager(this)

        // Already logged in: skip straight to the welcome screen.
        if (session.currentUser()!= null) {
            openWelcome()
            return
        }

        val auth= AuthManager(this)

        setContent {
            StudyHubProjectTheme {
                LoginScreen(
                    onLogin= { username, password ->
                        val user= auth.login(username, password)
                        if (user!= null) {
                            session.startSession(user)
                            openWelcome()
                            null
                        } else if (auth.isDisabled(username, password)) {
                            "This account has been disabled. Please contact the administrator."
                        } else {
                            "Incorrect username or password."
                        }
                    },
                    onRegister= {
                        startActivity(Intent(this, SignUpActivity::class.java))
                    }
                )
            }
        }
    }

    private fun openWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}

// onLogin returns an error message, or null on success.
@Composable
fun LoginScreen(
    onLogin: (String, String) -> String?,
    onRegister: () -> Unit
) {
    var username by remember {mutableStateOf("") }
    var password by remember {mutableStateOf("") }
    var usernameError by remember {mutableStateOf<String?>(null) }
    var passwordError by remember {mutableStateOf<String?>(null) }
    var message by remember {mutableStateOf("") }

    Column(
        modifier= Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ) {
        Text("StudyHub", style= MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value= username,
            onValueChange= { username= it; usernameError= null },
            label= { Text("Username") },
            singleLine= true,
            isError= usernameError!= null,
            supportingText= usernameError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value= password,
            onValueChange= { password= it; passwordError= null },
            label= { Text("Password") },
            singleLine= true,
            visualTransformation= PasswordVisualTransformation(),
            isError= passwordError!= null,
            supportingText= passwordError?.let { { Text(it) } }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick= {
                message= ""
                usernameError= if (username.isBlank()) "Username is required." else null
                passwordError= if (password.isEmpty()) "Password is required." else null

                if (usernameError== null && passwordError== null) {
                    message= onLogin(username.trim(), password) ?: ""
                }
            },
            modifier= Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        TextButton(onClick= onRegister) {
            Text("Need an account? Register")
        }

        if (message.isNotEmpty()) {
            Text(text= message, color= MaterialTheme.colorScheme.error)
        }
    }
}
