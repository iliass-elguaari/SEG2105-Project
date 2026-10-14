package com.group40.studyhub_project.model

data class Student(
    val user: User
) {
    init {
        require(user.role=="student") {
            "A Student must have the student role."
        }
    }
}
