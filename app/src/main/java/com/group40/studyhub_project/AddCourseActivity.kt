package com.group40.studyhub_project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.group40.studyhub_project.auth.SessionManager
import com.group40.studyhub_project.data.UserDatabase
import com.group40.studyhub_project.model.Course
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme
import com.group40.studyhub_project.util.ValidatedTextField
import com.group40.studyhub_project.util.Validator

// Lets a Student create a course. The category must be one the admin defined.
class AddCourseActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user= SessionManager(this).currentUser()
        if (user== null || user.role== "admin") {
            finish()
            return
        }

        val database= UserDatabase(this)
        val categories= database.getAllCategories().map { it.name }

        setContent {
            StudyHubProjectTheme {
                CourseForm(
                    heading= "Add Course",
                    initial= null,
                    categories= categories,
                    codeExists= { code, excludeId ->
                        database.getCourses(user.username)
                            .any { it.id!= excludeId && it.code.equals(code, ignoreCase= true) }
                    },
                    onSave= { code, title, category, term, location ->
                        val saved= database.addCourse(
                            Course(
                                code= code,
                                title= title,
                                category= category,
                                term= term,
                                location= location,
                                ownerUsername= user.username
                            )
                        )
                        if (saved) {
                            toast("Course added.")
                            finish()
                        } else {
                            toast("Could not save the course.")
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

// Course form shared by "add" (initial = null) and "edit" (initial = the course to pre-fill).
// onSave only runs once every field is valid; values are already trimmed/normalised,
// term is "Season Year" (e.g. "Fall 2026") and an empty location is passed as null.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseForm(
    heading: String,
    initial: Course?,
    categories: List<String>,
    codeExists: (code: String, excludeId: Long) -> Boolean,
    onSave: (code: String, title: String, category: String, term: String, location: String?) -> Unit,
    onCancel: () -> Unit
) {
    val initialTerm= initial?.term?.split(" ")
    var code by remember {mutableStateOf(initial?.code ?: "") }
    var title by remember {mutableStateOf(initial?.title ?: "") }
    var category by remember {mutableStateOf(initial?.category ?: "") }
    var season by remember {mutableStateOf(initialTerm?.getOrNull(0) ?: "") }
    var year by remember {mutableStateOf(initialTerm?.getOrNull(1) ?: "") }
    var location by remember {mutableStateOf(initial?.location ?: "") }

    var codeError by remember {mutableStateOf<String?>(null) }
    var titleError by remember {mutableStateOf<String?>(null) }
    var categoryError by remember {mutableStateOf<String?>(null) }
    var seasonError by remember {mutableStateOf<String?>(null) }
    var yearError by remember {mutableStateOf<String?>(null) }
    var locationError by remember {mutableStateOf<String?>(null) }

    Column(
        modifier= Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(heading, style= MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value= code,
            onValueChange= { code= it; codeError= null },
            label= "Course code (e.g. SEG2105)",
            error= codeError,
            modifier= Modifier.fillMaxWidth(),
            keyboardOptions= KeyboardOptions(capitalization= KeyboardCapitalization.Characters)
        )

        ValidatedTextField(
            value= title,
            onValueChange= { title= it; titleError= null },
            label= "Title",
            error= titleError,
            modifier= Modifier.fillMaxWidth(),
            keyboardOptions= KeyboardOptions(capitalization= KeyboardCapitalization.Words)
        )

        if (categories.isEmpty()) {
            Text(
                "No categories are available yet. Ask the administrator to add one.",
                color= MaterialTheme.colorScheme.error,
                modifier= Modifier.padding(vertical= 8.dp)
            )
        } else {
            DropdownField(
                label= "Category",
                value= category,
                options= categories,
                error= categoryError,
                onSelect= { category= it; categoryError= null }
            )
        }

        Row(
            modifier= Modifier.fillMaxWidth(),
            horizontalArrangement= Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label= "Term",
                value= season,
                options= Validator.COURSE_TERM_SEASONS,
                error= seasonError,
                onSelect= { season= it; seasonError= null },
                modifier= Modifier.weight(1f)
            )
            ValidatedTextField(
                value= year,
                onValueChange= { year= it; yearError= null },
                label= "Year",
                error= yearError,
                modifier= Modifier.weight(1f),
                keyboardOptions= KeyboardOptions(keyboardType= KeyboardType.Number)
            )
        }

        ValidatedTextField(
            value= location,
            onValueChange= { location= it; locationError= null },
            label= "Location (optional)",
            error= locationError,
            modifier= Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick= {
                val normalizedCode= Validator.normalizeCourseCode(code)
                codeError= Validator.validateCourseCode(code)
                    ?: Validator.validateCourseCodeAvailability(
                        codeExists(normalizedCode, initial?.id ?: 0L)
                    )
                titleError= Validator.validateCourseTitle(title)
                categoryError= Validator.validateCourseCategory(category, categories)
                seasonError= Validator.validateTermSeason(season)
                yearError= Validator.validateTermYear(year)
                locationError= Validator.validateCourseLocation(location)

                val valid= listOf(
                    codeError, titleError, categoryError, seasonError, yearError, locationError
                ).all { it== null }

                if (valid) {
                    onSave(
                        normalizedCode,
                        title.trim(),
                        category.trim(),
                        "$season ${year.trim()}",
                        location.trim().ifEmpty { null }
                    )
                }
            },
            enabled= categories.isNotEmpty(),
            modifier= Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(onClick= onCancel, modifier= Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

// Read-only dropdown: the user can only pick one of the given options.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    error: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier= Modifier.fillMaxWidth()
) {
    var expanded by remember {mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded= expanded,
        onExpandedChange= { expanded= it },
        modifier= modifier
    ) {
        OutlinedTextField(
            value= value,
            onValueChange= {},
            readOnly= true,
            label= { Text(label) },
            trailingIcon= { ExposedDropdownMenuDefaults.TrailingIcon(expanded= expanded) },
            isError= error!= null,
            supportingText= {
                error?.let { Text(it, color= MaterialTheme.colorScheme.error) }
            },
            singleLine= true,
            modifier= Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded= expanded,
            onDismissRequest= { expanded= false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text= { Text(option) },
                    onClick= {
                        onSelect(option)
                        expanded= false
                    }
                )
            }
        }
    }
}
