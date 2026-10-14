package com.group40.studyhub_project.auth

import android.content.Context
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.User

class AuthManager(context: Context) {
    private val database= UserDatabase(context)

    fun register(user: User): Boolean {
        if (user.username.isBlank()|| user.password.isBlank()||user.firstName.isBlank()|| user.lastName.isBlank()|| user.email.isBlank()
        ) {
            return false
        }

        if (user.role!= "student" &&
            user.role!= "student_group_leader"
        ) {
            return false
        }

        if (database.usernameExists(user.username.trim())) {
            return false }

        if (!user.email.contains("@") ||
            !user.email.contains("."))
        {
            return false }
        return database.addUser(user)
    }
    fun login(username:String, password:String): User?{
        return database.getUser(username, password)
    }
}
