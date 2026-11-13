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

    fun validateCategoryName(name: String): String? {
        val value = name.trim()

        if (value.isEmpty()) {
            return "Category name is required."
        }

        if (value.length !in 2..50) {
            return "Category name must be between 2 and 50 characters."
        }

        if (!value.matches(Regex("[\\p{L}\\p{N}]+([ &'.,()+#/-]+[\\p{L}\\p{N}]+)*[).+#]*"))) {
            return "Use letters, numbers, spaces and & ' . , ( ) + # / - only."
        }

        return null
    }

    fun validateCategoryNameAvailability(nameExists: Boolean): String? {
        if (nameExists) {
            return "A category with this name already exists."
        }

        return null
    }

    fun validateCategoryDescription(description: String): String? {
        val value = description.trim()

        if (value.isEmpty()) {
            return "Description is required."
        }

        if (value.length !in 10..250) {
            return "Description must be between 10 and 250 characters."
        }

        if (!value.any { it.isLetter() }) {
            return "Description must contain letters."
        }

        return null
    }

    // ---- Courses (Deliverable 3) ----

    val COURSE_TERM_SEASONS = listOf("Fall", "Winter", "Summer")

    // "seg 2105" -> "SEG2105". Use this before validating, checking duplicates and saving.
    fun normalizeCourseCode(code: String): String =
        code.trim().replace(Regex("\\s+"), "").uppercase()

    fun validateCourseCode(code: String): String? {
        val value = normalizeCourseCode(code)

        if (value.isEmpty()) {
            return "Course code is required."
        }

        if (!value.matches(Regex("[A-Z]{3,4}[0-9]{4}[A-Z]?"))) {
            return "Use 3-4 letters then 4 digits (e.g. SEG2105)."
        }

        return null
    }

    fun validateCourseCodeAvailability(codeExists: Boolean): String? {
        if (codeExists) {
            return "You already have a course with this code."
        }

        return null
    }

    fun validateCourseTitle(title: String): String? {
        val value = title.trim()

        if (value.isEmpty()) {
            return "Course title is required."
        }

        if (value.length !in 3..100) {
            return "Course title must be between 3 and 100 characters."
        }

        if (!value.any { it.isLetter() }) {
            return "Course title must contain letters."
        }

        if (!value.matches(Regex("[\\p{L}\\p{N} &'.,:()+#/-]+"))) {
            return "Use letters, numbers, spaces and & ' . , : ( ) + # / - only."
        }

        return null
    }

    // The category must be one the administrator defined.
    fun validateCourseCategory(category: String, adminCategories: List<String>): String? {
        val value = category.trim()

        if (value.isEmpty()) {
            return "Please select a category."
        }

        if (adminCategories.none { it == value }) {
            return "Please select a category from the list."
        }

        return null
    }

    fun validateTermSeason(season: String): String? {
        if (season !in COURSE_TERM_SEASONS) {
            return "Please select Fall, Winter or Summer."
        }

        return null
    }

    fun validateTermYear(year: String): String? {
        val value = year.trim()

        if (value.isEmpty()) {
            return "Year is required."
        }

        if (!value.matches(Regex("[0-9]{4}"))) {
            return "Enter a 4-digit year."
        }

        if (value.toInt() !in 2000..2100) {
            return "Year must be between 2000 and 2100."
        }

        return null
    }

    // Location is optional, but checked when it is filled in.
    fun validateCourseLocation(location: String): String? {
        val value = location.trim()

        if (value.isEmpty()) {
            return null
        }

        if (value.length !in 2..100) {
            return "Location must be between 2 and 100 characters."
        }

        if (!value.matches(Regex("[\\p{L}\\p{N} &'.,:()#/-]+"))) {
            return "Use letters, numbers, spaces and & ' . , : ( ) # / - only."
        }

        return null
    }
}
