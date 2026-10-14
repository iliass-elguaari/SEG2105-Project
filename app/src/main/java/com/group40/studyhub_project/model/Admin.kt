package com.group40.studyhub_project.model

data class Admin(
    val user: User
) {
    init {
        require(user.role=="admin") {
            "An Admin must have the admin role."
        }
    }
}
