package com.group40.studyhub_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.Course
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme

// Lists the signed-in Student's own courses. Tapping a course opens its detail screen.
class CourseListActivity: ComponentActivity() {
    private lateinit var database: UserDatabase
    private lateinit var username: String
    private var courses by mutableStateOf<List<Course>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Courses belong to students; the admin has none.
        val user= SessionManager(this).currentUser()
        if (user== null || user.role== "admin") {
            finish()
            return
        }

        database= UserDatabase(this)
        username= user.username

        setContent {
            StudyHubProjectTheme {
                CourseListScreen(
                    courses= courses,
                    onAddCourse= {
                        startActivity(Intent(this, AddCourseActivity::class.java))
                    },
                    onOpenCourse= { course ->
                        startActivity(
                            Intent(this, CourseDetailActivity::class.java)
                                .putExtra(CourseDetailActivity.EXTRA_COURSE_ID, course.id)
                        )
                    },
                    onBack= { finish() }
                )
            }
        }
    }

    // Reload every time we come back, so added, edited or deleted courses show up.
    override fun onResume() {
        super.onResume()
        if (::database.isInitialized) {
            courses= database.getCourses(username)
        }
    }
}

@Composable
fun CourseListScreen(
    courses: List<Course>,
    onAddCourse: () -> Unit,
    onOpenCourse: (Course) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier= Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally
    ) {
        Text("My Courses (${courses.size})", style= MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        Button(onClick= onAddCourse, modifier= Modifier.fillMaxWidth()) {
            Text("Add Course")
        }

        Spacer(Modifier.height(12.dp))

        if (courses.isEmpty()) {
            Column(
                modifier= Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment= Alignment.CenterHorizontally,
                verticalArrangement= Arrangement.Center
            ) {
                Text("No courses yet", style= MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap \"Add Course\" to create your first course.",
                    textAlign= TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier= Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement= Arrangement.spacedBy(8.dp)
            ) {
                items(courses, key= { it.id }) { course ->
                    CourseRow(course= course, onClick= { onOpenCourse(course) })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick= onBack, modifier= Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun CourseRow(course: Course, onClick: () -> Unit) {
    Card(modifier= Modifier.fillMaxWidth().clickable(onClick= onClick)) {
        Column(modifier= Modifier.padding(16.dp)) {
            Text(course.code, style= MaterialTheme.typography.titleMedium)
            Text(course.title)
            Text(
                "${course.category} · ${course.term}",
                style= MaterialTheme.typography.bodySmall,
                color= MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
