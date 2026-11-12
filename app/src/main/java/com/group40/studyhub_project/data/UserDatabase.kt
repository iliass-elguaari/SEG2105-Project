package com.group40.studyhub_project.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.model.Category
import com.group40.studyhub_project.model.Course
import com.group40.studyhub_project.model.Task

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, "StudyHub.db", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'active'    )
            """.trimIndent()
        )

        createCategoriesTable(db)
        createStudyTables(db)
        seedAdmin(db)
    }
    private fun createCategoriesTable(db:SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createStudyTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS courses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                term TEXT NOT NULL,
                location TEXT,
                ownerUsername TEXT NOT NULL,
                UNIQUE(ownerUsername, code)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                courseId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                dueDate INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(courseId) REFERENCES courses(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun seedAdmin(db: SQLiteDatabase) {
        val admin= ContentValues().apply {
            put("username", "admin")
            put("password", "XPI76SZUqyCjVxgnUjm0")
            put("firstName", "System")
            put("lastName", "Admin")
            put("email", "admin@studyhub.com")
            put("role", "admin")
            put("status", "active")
        }
        db.insertWithOnConflict(
            "users",
            null,
            admin,
            SQLiteDatabase.CONFLICT_IGNORE        )
    }
override fun onUpgrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int
) {
    if (oldVersion < 2) {
        db.execSQL(
            """
            ALTER TABLE users
            ADD COLUMN status TEXT NOT NULL DEFAULT 'active'
            """.trimIndent()
        )
        createCategoriesTable(db)
    }

    if (oldVersion < 3) {
        createStudyTables(db)
    }
    seedAdmin(db)
}

    fun addUser(user: User): Boolean {
        val values = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("role", user.role)
            put("status", user.status)
        }

        return writableDatabase.insert("users", null, values) != -1L
    }

    fun usernameExists(username: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE username = ?",
            arrayOf(username)
        )

        return cursor.use { it.moveToFirst() }
    }

    fun getUser(username: String, password: String): User? {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT username, password, firstName, lastName,
                   email, role, status
            FROM users
            WHERE username = ?
              AND password = ?
              AND status= 'active'
            """.trimIndent(),
            arrayOf(username, password)
        )


        return cursor.use {
            if (!it.moveToFirst()) {
                null
            } else {
                User(
                    username = it.getString(0),
                    password = it.getString(1),
                    firstName = it.getString(2),
                    lastName = it.getString(3),
                    email = it.getString(4),
                    role = it.getString(5),
                    status =it.getString(6)
                )
            }
        }
    }
    // True when the username and password are right but the admin disabled the account.
    fun isDisabled(username: String, password: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE username = ? AND password = ? AND status = 'disabled'",
            arrayOf(username, password)
        )
        return cursor.use { it.moveToFirst() }
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT username, password, firstName, lastName,
                   email, role, status
            FROM users
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(
                    User(
                        username = it.getString(0),
                        password = it.getString(1),
                        firstName = it.getString(2),
                        lastName = it.getString(3),
                        email = it.getString(4),
                        role = it.getString(5),
                        status=it.getString(6)
                    )
                )
            }
        }

        return users
    }

    fun disableUser(username: String): Boolean {
        if (username.equals("admin", ignoreCase = true)) return false

        val values = ContentValues().apply {
            put("status", "disabled")
        }

        return writableDatabase.update(
            "users", values, "username = ?", arrayOf(username)
        ) > 0
    }

    fun enableUser(username: String): Boolean {
        if (username.equals("admin", ignoreCase = true)) return false

        val values = ContentValues().apply {
            put("status", "active")
        }

        return writableDatabase.update(
            "users", values, "username = ?", arrayOf(username)
        ) > 0
    }

    fun deleteUser(username: String): Boolean {
        if (username.equals("admin", ignoreCase = true)) return false

        return writableDatabase.delete(
            "users", "username = ?", arrayOf(username)
        ) > 0
    }

    fun addCategory(category: Category): Boolean {
        val values = ContentValues().apply {
            put("name", category.name)
            put("description", category.description)
        }

        return writableDatabase.insert(
            "categories", null, values
        ) != -1L
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()

        val cursor = readableDatabase.rawQuery(
            "SELECT id, name, description FROM categories ORDER BY name",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                categories.add(
                    Category(
                        id = it.getInt(0),
                        name = it.getString(1),
                        description = it.getString(2)
                    )
                )
            }
        }

        return categories
    }

    fun updateCategory(category: Category): Boolean {
        val values = ContentValues().apply {
            put("name", category.name)
            put("description", category.description)
        }

        return writableDatabase.update(
            "categories",
            values,
            "id = ?",
            arrayOf(category.id.toString())
        ) > 0
    }

    fun deleteCategory(categoryId: Int): Boolean {
        return writableDatabase.delete(
            "categories",
            "id = ?",
            arrayOf(categoryId.toString())
        ) > 0
    }

    fun addCourse(course: Course): Boolean {
        val values = ContentValues().apply {
            put("code", course.code)
            put("title", course.title)
            put("category", course.category)
            put("term", course.term)
            put("location", course.location)
            put("ownerUsername", course.ownerUsername)
        }
        return writableDatabase.insert("courses", null, values) != -1L
    }

    fun getCourses(ownerUsername: String): List<Course> {
        val courses = mutableListOf<Course>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, code, title, category, term, location, ownerUsername FROM courses WHERE ownerUsername = ? ORDER BY code",
            arrayOf(ownerUsername)
        )
        cursor.use {
            while (it.moveToNext()) {
                courses.add(
                    Course(
                        id = it.getLong(0),
                        code = it.getString(1),
                        title = it.getString(2),
                        category = it.getString(3),
                        term = it.getString(4),
                        location = it.getString(5),
                        ownerUsername = it.getString(6)
                    )
                )
            }
        }
        return courses
    }

    fun getCourse(courseId: Long, ownerUsername: String): Course? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, code, title, category, term, location, ownerUsername FROM courses WHERE id = ? AND ownerUsername = ?",
            arrayOf(courseId.toString(), ownerUsername)
        )
        return cursor.use {
            if (!it.moveToFirst()) null else Course(
                id = it.getLong(0),
                code = it.getString(1),
                title = it.getString(2),
                category = it.getString(3),
                term = it.getString(4),
                location = it.getString(5),
                ownerUsername = it.getString(6)
            )
        }
    }

    fun updateCourse(course: Course): Boolean {
        val values = ContentValues().apply {
            put("code", course.code)
            put("title", course.title)
            put("category", course.category)
            put("term", course.term)
            put("location", course.location)
        }
        return writableDatabase.update(
            "courses", values, "id = ? AND ownerUsername = ?",
            arrayOf(course.id.toString(), course.ownerUsername)
        ) > 0
    }

    fun deleteCourse(courseId: Long, ownerUsername: String): Boolean {
        return writableDatabase.delete(
            "courses", "id = ? AND ownerUsername = ?",
            arrayOf(courseId.toString(), ownerUsername)
        ) > 0
    }

    fun addTask(task: Task, ownerUsername: String): Boolean {
        if (getCourse(task.courseId, ownerUsername) == null) return false
        val values = ContentValues().apply {
            put("courseId", task.courseId)
            put("title", task.title)
            put("description", task.description)
            put("dueDate", task.dueDate)
            put("isCompleted", if (task.isCompleted) 1 else 0)
        }
        return writableDatabase.insert("tasks", null, values) != -1L
    }

    fun getTasks(courseId: Long, ownerUsername: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT t.id, t.courseId, t.title, t.description, t.dueDate, t.isCompleted
            FROM tasks t INNER JOIN courses c ON c.id = t.courseId
            WHERE t.courseId = ? AND c.ownerUsername = ? ORDER BY t.dueDate
            """.trimIndent(),
            arrayOf(courseId.toString(), ownerUsername)
        )
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(
                    Task(
                        id = it.getLong(0),
                        courseId = it.getLong(1),
                        title = it.getString(2),
                        description = it.getString(3),
                        dueDate = it.getLong(4),
                        isCompleted = it.getInt(5) != 0
                    )
                )
            }
        }
        return tasks
    }

    fun updateTask(task: Task, ownerUsername: String): Boolean {
        if (getCourse(task.courseId, ownerUsername) == null) return false
        val values = ContentValues().apply {
            put("courseId", task.courseId)
            put("title", task.title)
            put("description", task.description)
            put("dueDate", task.dueDate)
            put("isCompleted", if (task.isCompleted) 1 else 0)
        }
        return writableDatabase.update(
            "tasks", values,
            "id = ? AND courseId IN (SELECT id FROM courses WHERE ownerUsername = ?)",
            arrayOf(task.id.toString(), ownerUsername)
        ) > 0
    }

    fun deleteTask(taskId: Long, ownerUsername: String): Boolean {
        return writableDatabase.delete(
            "tasks",
            "id = ? AND courseId IN (SELECT id FROM courses WHERE ownerUsername = ?)",
            arrayOf(taskId.toString(), ownerUsername)
        ) > 0
    }
}
