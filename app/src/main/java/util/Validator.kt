package com.group40.studyhub_project.util

import android.util.Patterns

object Validator {

    fun validateFirstName(firstName: String): String? {
        val value = firstName.trim()

        if (value.isEmpty()) {
            return "First name is required."
        }

        if (!value.matches(Regex("[\\p{L}]+([ '-][\\p{L}]+)*"))) {
            return "Use letters, spaces, hyphens, or apostrophes only."
        }

        return null
    }

    fun validateLastName(lastName: String): String? {
        val value = lastName.trim()

        if (value.isEmpty()) {
            return "Last name is required."
        }

        if (!value.matches(Regex("[\\p{L}]+([ '-][\\p{L}]+)*"))) {
            return "Use letters, spaces, hyphens, or apostrophes only."
        }

        return null
    }

    fun validateEmail(email: String): String? {
        val value = email.trim()

        if (value.isEmpty()) {
            return "Email is required."
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return "Please enter a valid email address."
        }

        return null
    }

    fun validateUsername(username: String): String? {
        if (username.isBlank()) {
            return "Username is required."
        }

        if (username.any { it.isWhitespace() }) {
            return "Username must not contain spaces."
        }

        if (username.length !in 3..20) {
            return "Username must be between 3 and 20 characters."
        }

        return null
    }

    fun validateUsernameAvailability(
        username: String,
        usernameExists: Boolean
    ): String? {
        if (usernameExists) {
            return "This username is already taken."
        }

        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) {
            return "Password is required."
        }

        if (password.length < 8) {
            return "Password must be at least 8 characters long."
        }

        if (!password.any { it.isUpperCase() }) {
            return "Password must contain at least one uppercase letter."
        }

        if (!password.any { it.isLowerCase() }) {
            return "Password must contain at least one lowercase letter."
        }

        if (!password.any { it.isDigit() }) {
            return "Password must contain at least one number."
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            return "Password must contain at least one special character."
        }

        return null
    }

    fun validateRole(role: String): String? {
        if (role != "student" && role != "student_group_leader") {
            return "Please select a valid account type."
        }

        return null
    }
}



