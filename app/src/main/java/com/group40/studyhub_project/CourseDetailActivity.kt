package com.group40.studyhub_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.Course
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

// Shows every field of one course. Only the owner can open it:
// getCourse() returns null for someone else's course, and the screen closes.
class CourseDetailActivity: ComponentActivity() {
    private lateinit var database: UserDatabase
    private lateinit var username: String
    private var courseId= 0L
    private var course by mutableStateOf<Course?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user= SessionManager(this).currentUser()
        courseId= intent.getLongExtra(EXTRA_COURSE_ID, 0L)
        if (user== null || user.role== "admin" || courseId<= 0L) {
            finish()
            return
        }

        database= UserDatabase(this)
        username= user.username

        setContent {
            StudyHubProjectTheme {
                // true while the "Are you sure?" box is open
                var confirmDelete by remember { mutableStateOf(false) }

                course?.let { current ->
                    CourseDetailScreen(
                        course= current,
                        onBack= { finish() },
                        actions= {
                            Button(
                                onClick= { openScreen(EditCourseActivity::class.java) },
                                modifier= Modifier.fillMaxWidth()
                            ) {
                                Text("Edit Course")
                            }

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick= { openScreen(TaskListActivity::class.java) },
                                modifier= Modifier.fillMaxWidth()
                            ) {
                                Text("View Tasks")
                            }

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick= { confirmDelete= true },
                                colors= ButtonDefaults.buttonColors(
                                    containerColor= MaterialTheme.colorScheme.error
                                ),
                                modifier= Modifier.fillMaxWidth()
                            ) {
                                Text("Delete Course")
                            }

                            Spacer(Modifier.height(8.dp))
                        }
                    )

                    if (confirmDelete) {
                        AlertDialog(
                            onDismissRequest= { confirmDelete= false },
                            title= { Text("Delete course?") },
                            text= {
                                Text("Are you sure you want to delete ${current.code}? All of its tasks will be deleted too.")
                            },
                            confirmButton= {
                                TextButton(onClick= {
                                    confirmDelete= false
                                    deleteCourse()
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton= {
                                TextButton(onClick= { confirmDelete= false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Reload on return so changes made from this screen (e.g. edit) are shown.
    override fun onResume() {
        super.onResume()
        if (!::database.isInitialized) return

        course= database.getCourse(courseId, username)
        if (course== null) finish()
    }

    // Opens the edit or task screen for this course.
    private fun openScreen(screen: Class<*>) {
        startActivity(Intent(this, screen).putExtra(EXTRA_COURSE_ID, courseId))
    }

    // Deletes the course. Its tasks are removed too, because the tasks table
    // uses ON DELETE CASCADE (the delete rule).
    private fun deleteCourse() {
        if (database.deleteCourse(courseId, username)) {
            Toast.makeText(this, "Course and its tasks deleted.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Could not delete the course.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_COURSE_ID= "courseId"
    }
}

// actions: extra buttons (edit, delete, tasks) placed above "Back".
@Composable
fun CourseDetailScreen(
    course: Course,
    onBack: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit= {}
) {
    Column(
        modifier= Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(course.code, style= MaterialTheme.typography.headlineLarge)
        Text(course.title, style= MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        Card(modifier= Modifier.fillMaxWidth()) {
            Column(modifier= Modifier.padding(16.dp)) {
                CourseDetailRow("Course code", course.code)
                CourseDetailRow("Title", course.title)
                CourseDetailRow("Category", course.category)
                CourseDetailRow("Term", course.term)
                CourseDetailRow(
                    "Location",
                    course.location?.takeIf { it.isNotBlank() } ?: "Not specified"
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        actions()

        OutlinedButton(onClick= onBack, modifier= Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun CourseDetailRow(label: String, value: String) {
    Column(modifier= Modifier.padding(vertical= 6.dp)) {
        Text(
            label,
            style= MaterialTheme.typography.labelMedium,
            color= MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style= MaterialTheme.typography.bodyLarge)
    }
}