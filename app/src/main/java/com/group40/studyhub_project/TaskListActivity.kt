package com.group40.studyhub_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.group40.studyhub_project.model.Task
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Shows the tasks of one course. Only the owner can open it:
// getCourse() returns null for someone else's course, and the screen closes.
class TaskListActivity: ComponentActivity() {
    private lateinit var database: UserDatabase
    private lateinit var username: String
    private var courseId= 0L
    private var course by mutableStateOf<Course?>(null)
    private var tasks by mutableStateOf<List<Task>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user= SessionManager(this).currentUser()
        courseId= intent.getLongExtra(CourseDetailActivity.EXTRA_COURSE_ID, 0L)
        if (user== null || user.role== "admin" || courseId<= 0L) {
            finish()
            return
        }

        database= UserDatabase(this)
        username= user.username

        setContent {
            StudyHubProjectTheme {
                course?.let { current ->
                    TaskListScreen(
                        course= current,
                        tasks= tasks,
                        onBack= { finish() }
                    )
                }
            }
        }
    }

    // Reload every time the screen comes back, so the list is always up to date.
    override fun onResume() {
        super.onResume()
        if (!::database.isInitialized) return

        course= database.getCourse(courseId, username)
        if (course== null) {
            finish()
            return
        }
        tasks= database.getTasks(courseId, username)
    }
}

@Composable
fun TaskListScreen(
    course: Course,
    tasks: List<Task>,
    onBack: () -> Unit
) {
    Column(
        modifier= Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally
    ) {
        Text("Tasks (${tasks.size})", style= MaterialTheme.typography.headlineLarge)
        Text("${course.code} · ${course.title}", style= MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            // Empty state: shown when the course has no tasks yet.
            Column(
                modifier= Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment= Alignment.CenterHorizontally,
                verticalArrangement= Arrangement.Center
            ) {
                Text("No tasks yet", style= MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "This course doesn't have any tasks.",
                    textAlign= TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier= Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement= Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key= { it.id }) { task ->
                    TaskRow(task)
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
fun TaskRow(task: Task) {
    Card(modifier= Modifier.fillMaxWidth()) {
        Column(modifier= Modifier.padding(16.dp)) {
            Text(task.title, style= MaterialTheme.typography.titleMedium)
            if (task.description.isNotBlank()) {
                Text(task.description)
            }
            Text(
                "Due ${formatDueDate(task.dueDate)} · ${if (task.isCompleted) "Done" else "To do"}",
                style= MaterialTheme.typography.bodySmall,
                color= MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// dueDate is stored as milliseconds; this turns it into e.g. "Nov 15, 2026".
fun formatDueDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))