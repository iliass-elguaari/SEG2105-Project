package com.group40.studyhub_project.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.group40.studyhub_project.model.User

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, "StudyHub.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL 
            )
            """.trimIndent())

        val admin= ContentValues().apply {
            put("username", "admin")
            put("password", "XPI76SZUqyCjVxgnUjm0")
            put("firstName", "System")
            put("lastName", "Admin")
            put("email", "admin@studyhub.com")
            put("role", "admin")
        }
        db.insert("users", null, admin)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int ) {
    }
    fun addUser(user: User): Boolean {
        val values=ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("role", user.role)
        }
        return writableDatabase.insert(
            "users",
            null,
            values
        ) != -1L
    }

    fun usernameExists(username: String): Boolean {
        val cursor =readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE username =?",
            arrayOf(username)
        )
        return cursor.use {
            it.moveToFirst()
        }
    }
    fun getUser(
        username:String,
        password:String
    ): User? {
        val cursor= readableDatabase.rawQuery(
            "SELECT * FROM users WHERE username=? AND password=?",
            arrayOf(username, password)
        )
        return cursor.use {
            if (!it.moveToFirst()) {
                return@use null
            }

            User(
                username= it.getString(0),
                password= it.getString(1),
                firstName= it.getString(2),
                lastName= it.getString(3),
                email= it.getString(4),
                role= it.getString(5)
            )
        }
    }
    fun getAllUsers(): List<User> {
        val users= mutableListOf<User>()

        val cursor= readableDatabase.rawQuery(
            "SELECT * FROM users",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                users.add(
                    User(
                        username= it.getString(0),
                        password= it.getString(1),
                        firstName= it.getString(2),
                        lastName= it.getString(3),
                        email= it.getString(4),
                        role= it.getString(5))
                ) } }
        return users
    }
}
