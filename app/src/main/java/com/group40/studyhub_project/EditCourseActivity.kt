package com.group40.studyhub_project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

// Lets a Student edit one of their own courses. The form opens pre-filled.
// getCourse() only finds the course if this student owns it, otherwise the screen closes.
class EditCourseActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user= SessionManager(this).currentUser()
        val courseId= intent.getLongExtra(CourseDetailActivity.EXTRA_COURSE_ID, 0L)
        if (user== null || user.role== "admin" || courseId<= 0L) {
            finish()
            return
        }

        val database= UserDatabase(this)
        val course= database.getCourse(courseId, user.username)
        if (course== null) {
            toast("You can only edit your own courses.")
            finish()
            return
        }

        val categories= database.getAllCategories().map { it.name }

        setContent {
            StudyHubProjectTheme {
                CourseForm(
                    heading= "Edit Course",
                    initial= course,
                    categories= categories,
                    // Same duplicate check as Add, but it skips this course itself,
                    // so saving without changing the code still works.
                    codeExists= { code, excludeId ->
                        database.getCourses(user.username)
                            .any { it.id!= excludeId && it.code.equals(code, ignoreCase= true) }
                    },
                    onSave= { code, title, category, term, location ->
                        val saved= database.updateCourse(
                            course.copy(
                                code= code,
                                title= title,
                                category= category,
                                term= term,
                                location= location
                            )
                        )
                        if (saved) {
                            toast("Course updated.")
                            finish()
                        } else {
                            toast("Could not update the course.")
                        }
                    },
                    onCancel= { finish() }
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}