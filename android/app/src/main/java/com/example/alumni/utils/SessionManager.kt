package com.example.alumni.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.alumni.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val PREF_NAME = "AlumniAppPrefs"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USER_DATA = "user_data"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUser(user: User) {
        val editor = prefs.edit()
        val userJson = gson.toJson(user)
        editor.putString(KEY_USER_DATA, userJson)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun updateUserProfile(updatedUser: User) {
        val currentUser = getUser()
        if (currentUser?.id == updatedUser.id) {
            saveUser(updatedUser)
        }
    }

    // Additional session-related methods
    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_USER_DATA)
        editor.remove(KEY_IS_LOGGED_IN)
        editor.apply()
    }

    fun refreshSession(token: String, user: User) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putString(KEY_USER_DATA, gson.toJson(user))
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun isSessionValid(): Boolean {
        val token = getAuthToken()
        val user = getUser()
        return !token.isNullOrEmpty() && user != null
    }
}
