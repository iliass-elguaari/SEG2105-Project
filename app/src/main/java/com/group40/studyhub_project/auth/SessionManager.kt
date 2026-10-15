package com.group40.studyhub_project.auth

import android.content.Context
import com.group40.studyhub_project.model.User

// Remembers who is logged in so the app reopens on the welcome screen.
// The password is never stored here.
class SessionManager(context: Context) {
    private val prefs= context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun startSession(user: User) {
        prefs.edit()
            .putString("username", user.username)
            .putString("firstName", user.firstName)
            .putString("lastName", user.lastName)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun currentUser(): User? {
        val username= prefs.getString("username", null) ?: return null
        return User(
            username= username,
            password= "",
            firstName= prefs.getString("firstName", "") ?: "",
            lastName= prefs.getString("lastName", "") ?: "",
            email= prefs.getString("email", "") ?: "",
            role= prefs.getString("role", "") ?: ""
        )
    }

    fun endSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        fun roleLabel(role: String): String= when (role) {
            "admin" -> "admin"
            "student" -> "student"
            "student_group_leader" -> "study group leader"
            else -> role
        }
    }
}
