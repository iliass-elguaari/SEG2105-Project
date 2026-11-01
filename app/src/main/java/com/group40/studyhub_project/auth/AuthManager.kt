package com.group40.studyhub_project.auth

import android.content.Context
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.util.Validator

class AuthManager(context: Context) {
    private val database= UserDatabase(context)

    fun register(user: User): Boolean {
        if (Validator.validateFirstName(user.firstName) != null) return false
        if (Validator.validateLastName(user.lastName) != null) return false
        if (Validator.validateEmail(user.email) != null) return false
        if (Validator.validateUsername(user.username) != null) return false
        if (Validator.validatePassword(user.password) != null) return false
        if (Validator.validateRole(user.role) != null) return false
        if (database.usernameExists(user.username.trim())) return false

        return database.addUser(user)
    }
    fun login(username:String, password:String): User?{
        return database.getUser(username, password)
    }
    fun isDisabled(username: String, password: String): Boolean {
        return database.isDisabled(username, password)
    }

    fun usernameExists(username: String): Boolean {
        return database.usernameExists(username.trim())
    }
}
