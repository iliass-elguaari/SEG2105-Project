package com.group40.studyhub_project.model

class Student(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    email: String
) : User(username, password, firstName, lastName, email, "student")
