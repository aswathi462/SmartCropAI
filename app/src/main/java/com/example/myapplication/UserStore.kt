package com.example.myapplication

import android.content.Context

data class RegisteredUser(
    val fullName: String,
    val email: String,
    val password: String
)

object UserStore {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_CURRENT_NAME = "current_name"
    private const val KEY_CURRENT_EMAIL = "current_email"

    fun saveUser(context: Context, user: RegisteredUser) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, user.fullName)
            .putString(KEY_EMAIL, user.email.trim())
            .putString(KEY_PASSWORD, user.password)
            .apply()
    }

    fun getUser(context: Context): RegisteredUser? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)

        if (name.isNullOrBlank() || email.isNullOrBlank() || password.isNullOrBlank()) {
            return null
        }

        return RegisteredUser(name, email, password)
    }

    fun saveCurrentUser(context: Context, fullName: String, email: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENT_NAME, fullName)
            .putString(KEY_CURRENT_EMAIL, email.trim())
            .apply()
    }

    fun getCurrentUser(context: Context): Pair<String, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_CURRENT_NAME, null)
        val email = prefs.getString(KEY_CURRENT_EMAIL, null)

        if (name.isNullOrBlank() || email.isNullOrBlank()) {
            return null
        }

        return name to email
    }

    fun clearCurrentUser(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CURRENT_NAME)
            .remove(KEY_CURRENT_EMAIL)
            .apply()
    }
}
