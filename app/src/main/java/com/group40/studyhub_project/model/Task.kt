package com.group40.studyhub_project.model

data class Task(
    val id: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String,
    val dueDate: Long,
    val isCompleted: Boolean = false
)
