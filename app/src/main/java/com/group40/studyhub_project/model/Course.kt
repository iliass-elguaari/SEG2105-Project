package com.group40.studyhub_project.model

data class Course(
    val id: Long = 0,
    val code: String,
    val title: String,
    val category: String,
    val term: String,
    val location: String? = null,
    val ownerUsername: String
)
