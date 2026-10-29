package com.group40.studyhub_project

import android.os.Bundle
import android.widget.Toast
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
import com.group40.studyhub_project.model.Category
import com.group40.studyhub_project.ui.theme.StudyHubProjectTheme
import com.group40.studyhub_project.util.ValidatedTextField
import com.group40.studyhub_project.util.Validator

// Admin-only screen to add, edit and delete course categories.
class CategoryActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only the admin may manage categories.
        val user= SessionManager(this).currentUser()
        if (user== null || user.role!= "admin") {
            finish()
            return
        }

        val database= UserDatabase(this)

        setContent {
            StudyHubProjectTheme {
                var categories by remember {mutableStateOf(database.getAllCategories()) }

                CategoryScreen(
                    categories= categories,
                    // Case-insensitive, and the category being edited does not count.
                    nameExists= { name, excludeId ->
                        categories.any { it.id!= excludeId && it.name.equals(name, ignoreCase= true) }
                    },
                    onSave= { category ->
                        val saved= if (category.id== 0) {
                            database.addCategory(category)
                        } else {
                            database.updateCategory(category)
                        }
                        if (saved) {
                            categories= database.getAllCategories()
                            toast(if (category.id== 0) "Category added." else "Category updated.")
                        } else {
                            toast("Could not save the category.")
                        }
                    },
                    onDelete= { category ->
                        if (database.deleteCategory(category.id)) {
                            categories= database.getAllCategories()
                            toast("Category deleted.")
                        }
                    },
                    onBack= { finish() }
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CategoryScreen(
    categories: List<Category>,
    nameExists: (String, Int) -> Boolean,
    onSave: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onBack: () -> Unit
) {
    // null = no dialog, id 0 = adding a new category, otherwise editing.
    var editing by remember {mutableStateOf<Category?>(null) }
    var deleting by remember {mutableStateOf<Category?>(null) }

    Column(
        modifier= Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment= Alignment.CenterHorizontally
    ) {
        Text("Categories (${categories.size})", style= MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick= { editing= Category(name= "", description= "") },
            modifier= Modifier.fillMaxWidth()
        ) {
            Text("Add Category")
        }

        Spacer(Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Text(
                "No categories yet. Tap \"Add Category\" to create one.",
                modifier= Modifier.weight(1f),
                textAlign= TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier= Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement= Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key= { it.id }) { category ->
                    CategoryRow(
                        category= category,
                        onEdit= { editing= category },
                        onDelete= { deleting= category }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick= onBack, modifier= Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }

    editing?.let { category ->
        CategoryDialog(
            category= category,
            nameExists= nameExists,
            onSave= {
                onSave(it)
                editing= null
            },
            onDismiss= { editing= null }
        )
    }

    deleting?.let { category ->
        AlertDialog(
            onDismissRequest= { deleting= null },
            title= { Text("Delete category?") },
            text= { Text("\"${category.name}\" will be removed permanently.") },
            confirmButton= {
                TextButton(onClick= {
                    onDelete(category)
                    deleting= null
                }) {
                    Text("Delete", color= MaterialTheme.colorScheme.error)
                }
            },
            dismissButton= {
                TextButton(onClick= { deleting= null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier= Modifier.fillMaxWidth()) {
        Column(modifier= Modifier.padding(16.dp)) {
            Text(category.name, style= MaterialTheme.typography.titleMedium)
            Text(category.description)
            Row(
                modifier= Modifier.fillMaxWidth(),
                horizontalArrangement= Arrangement.End
            ) {
                TextButton(onClick= onEdit) { Text("Edit") }
                TextButton(onClick= onDelete) {
                    Text("Delete", color= MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Shared by add (id 0) and edit. Only calls onSave once every field is valid.
@Composable
fun CategoryDialog(
    category: Category,
    nameExists: (String, Int) -> Boolean,
    onSave: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember {mutableStateOf(category.name) }
    var description by remember {mutableStateOf(category.description) }
    var nameError by remember {mutableStateOf<String?>(null) }
    var descriptionError by remember {mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest= onDismiss,
        title= { Text(if (category.id== 0) "Add Category" else "Edit Category") },
        text= {
            Column {
                ValidatedTextField(
                    value= name,
                    onValueChange= { name= it; nameError= null },
                    label= "Name",
                    error= nameError
                )
                ValidatedTextField(
                    value= description,
                    onValueChange= { description= it; descriptionError= null },
                    label= "Description",
                    error= descriptionError,
                    singleLine= false
                )
            }
        },
        confirmButton= {
            TextButton(onClick= {
                nameError= Validator.validateCategoryName(name)
                    ?: Validator.validateCategoryNameAvailability(
                        nameExists(name.trim(), category.id)
                    )
                descriptionError= Validator.validateCategoryDescription(description)

                if (nameError== null && descriptionError== null) {
                    onSave(
                        category.copy(
                            name= name.trim(),
                            description= description.trim()
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton= {
            TextButton(onClick= onDismiss) { Text("Cancel") }
        }
    )
}
