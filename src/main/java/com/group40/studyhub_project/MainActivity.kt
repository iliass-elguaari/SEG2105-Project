package com.group40.studyhub_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.AuthManager
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth= AuthManager(this)

        setContent {
            StudyHubProjectTheme {
                var username by remember {mutableStateOf("") }
                var password by remember {mutableStateOf("") }
                var firstName by remember {mutableStateOf("") }
                var lastName by remember {mutableStateOf("") }
                var email by remember {mutableStateOf("") }
                var role by remember {mutableStateOf("student") }
                var registering by remember {mutableStateOf(false) }
                var currentUser by remember {mutableStateOf<User?>(null) }
                var message by remember {mutableStateOf("") }

                if (currentUser!= null) {
                    Column(
                        modifier= Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment= Alignment.CenterHorizontally,
                        verticalArrangement= Arrangement.Center
                    ) {
                        Text(
                            "Welcome ${currentUser!!.firstName}!",
                            style=MaterialTheme.typography.headlineMedium
                        )
                        Text("You are logged in as \"${currentUser!!.role}\".")

                        Spacer(Modifier.height(24.dp))

                        Button(onClick= {
                            currentUser= null
                            username= ""
                            password= ""
                            message= ""
                            registering= false
                        }) {
                            Text("Log Out")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment= Alignment.CenterHorizontally,
                        verticalArrangement= Arrangement.Center
                    ) {
                        Text(
                            "StudyHub",
                            style= MaterialTheme.typography.headlineLarge
                        )

                        Spacer(Modifier.height(16.dp))

                        if (registering) {
                            OutlinedTextField(
                                value= firstName,
                                onValueChange= { firstName = it },
                                label= { Text("First name") },
                                singleLine= true
                            )

                            OutlinedTextField(
                                value= lastName,
                                onValueChange= { lastName = it },
                                label= { Text("Last name") },
                                singleLine= true
                            )

                            OutlinedTextField(
                                value= email,
                                onValueChange= { email = it },
                                label= { Text("Email") },
                                singleLine= true,
                                keyboardOptions= KeyboardOptions(
                                    keyboardType= KeyboardType.Email
                                )
                            )
                        }

                        OutlinedTextField(
                            value= username,
                            onValueChange= { username = it },
                            label= { Text("Username") },
                            singleLine= true
                        )

                        OutlinedTextField(
                            value= password,
                            onValueChange= { password = it },
                            label= { Text("Password") },
                            singleLine= true,
                            visualTransformation=
                                PasswordVisualTransformation()
                        )

                        if (registering) {
                            Spacer(Modifier.height(8.dp))
                            Text("Choose account type")

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected= role == "student",
                                    onClick= { role = "student" }
                                )
                                Text("Student")

                                RadioButton(
                                    selected= role == "student_group_leader",
                                    onClick= {
                                        role= "student_group_leader"
                                    }
                                )
                                Text("Group Leader")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick= {
                                message = ""

                                if (registering) {
                                    val user= User(
                                        username= username.trim(),
                                        password= password,
                                        firstName= firstName.trim(),
                                        lastName= lastName.trim(),
                                        email= email.trim(),
                                        role= role
                                    )

                                    if (auth.register(user)) {
                                        message= "Account created. You can log in!"
                                        registering= false
                                        firstName= ""
                                        lastName= ""
                                        email= ""
                                        username= ""
                                        password= ""
                                    } else {
                                        message=
                                            "Check all fields and make sure the username is available."
                                    }
                                } else {
                                    val user= auth.login(
                                        username.trim(),
                                        password
                                    )

                                    if (user!= null) {
                                        currentUser= user
                                    } else {
                                        message=
                                            "Incorrect username or password."
                                    }
                                }
                            },
                            modifier= Modifier.fillMaxWidth()
                        ) {
                            Text(if (registering) "Create Account" else "Log In")
                        }

                        TextButton(onClick = {
                            registering= !registering
                            message= ""
                        }) {
                            Text(
                                if (registering) {
                                    "Already have an account? Log in"
                                } else {
                                    "Need an account? Register"
                                }
                            )
                        }

                        if (message.isNotEmpty()) {
                            Text(
                                text= message,
                                color= MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
