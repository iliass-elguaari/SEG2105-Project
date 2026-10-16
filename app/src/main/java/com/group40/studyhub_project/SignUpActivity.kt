package com.group40.studyhub_project

import android.os.Bundle
import android.widget.Toast
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
import com.group40.studyhub_project.util.ValidatedTextField
import com.group40.studyhub_project.util.Validator

// Registration form moved out of the old MainActivity as-is.
// Owned by member 3 (sign-up and validation).
class SignUpActivity: ComponentActivity() {
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
                var message by remember {mutableStateOf("") }

                var firstNameError by remember { mutableStateOf<String?>(null) }
                var lastNameError by remember { mutableStateOf<String?>(null) }
                var emailError by remember { mutableStateOf<String?>(null) }
                var usernameError by remember { mutableStateOf<String?>(null) }
                var passwordError by remember { mutableStateOf<String?>(null) }
                var roleError by remember { mutableStateOf<String?>(null) }

                Column(
                    modifier= Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment= Alignment.CenterHorizontally,
                    verticalArrangement= Arrangement.Center
                ) {
                    Text("Create Account", style= MaterialTheme.typography.headlineLarge)

                    Spacer(Modifier.height(16.dp))

                    ValidatedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            firstNameError = null
                        },
                        label = "First name",
                        error = firstNameError
                    )

                    ValidatedTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            lastNameError = null
                        },
                        label = "Last name",
                        error = lastNameError
                    )

                    ValidatedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Email",
                        error = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    ValidatedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = null
                        },
                        label = "Username",
                        error = usernameError
                    )

                    ValidatedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = "Password",
                        error = passwordError,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Choose account type")

                    Row(verticalAlignment= Alignment.CenterVertically) {
                        RadioButton(
                            selected= role== "student",
                            onClick= {
                                role= "student"
                                roleError = null
                            }
                        )
                        Text("Student")

                        RadioButton(
                            selected= role== "student_group_leader",
                            onClick= {
                                role= "student_group_leader"
                                roleError = null
                            }
                        )
                        Text("Group Leader")
                    }

                    roleError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick= {
                            firstNameError = Validator.validateFirstName(firstName)
                            lastNameError = Validator.validateLastName(lastName)
                            emailError = Validator.validateEmail(email)
                            usernameError = Validator.validateUsername(username)
                            passwordError = Validator.validatePassword(password)
                            roleError = Validator.validateRole(role)

                            if (
                                firstNameError != null ||
                                lastNameError != null ||
                                emailError != null ||
                                usernameError != null ||
                                passwordError != null ||
                                roleError != null
                            ) {
                                message = ""
                                return@Button
                            }

                            if (auth.usernameExists(username.trim())) {
                                usernameError = Validator.validateUsernameAvailability(
                                    username,
                                    usernameExists = true
                                )
                                message = ""
                                return@Button
                            }

                            val user= User(
                                username= username.trim(),
                                password= password,
                                firstName= firstName.trim(),
                                lastName= lastName.trim(),
                                email= email.trim(),
                                role= role
                            )

                            if (auth.register(user)) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Account created. You can log in!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                message= "Check all fields and make sure the username is available."
                            }
                        },
                        modifier= Modifier.fillMaxWidth()
                    ) {
                        Text("Create Account")
                    }

                    TextButton(onClick= { finish() }) {
                        Text("Already have an account? Log in")
                    }

                    if (message.isNotEmpty()) {
                        Text(text= message, color= MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
