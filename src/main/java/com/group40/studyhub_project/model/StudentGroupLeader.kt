package com.group40.studyhub_project.model

data class StudentGroupLeader
    (val user: User) {
    init {
        require(user.role== "student_group_leader") {
            "A StudentGroupLeader must have the student_group_leader role."
        }
    }
}
