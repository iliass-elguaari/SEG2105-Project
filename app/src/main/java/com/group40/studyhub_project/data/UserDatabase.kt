package com.group40.studyhub_project.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.group40.studyhub_project.model.User
import com.group40.studyhub_project.model.Category

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, "StudyHub.db", null, 2) {

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
}
