package com.example.biomapsapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.biomapsapp.data.GoogleLoginResponse
import com.example.biomapsapp.data.LoginResponse
import com.example.biomapsapp.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreference(context: Context) {

    private val preference = context.getSharedPreferences("bio_maps_app_prefs", Context.MODE_PRIVATE)

    fun saveData(data: Any) {
        val editor = preference.edit()

        when (data) {
            is LoginResponse -> {
                editor.putBoolean("success", true)
                editor.putString("message", data.message)
                editor.putString("token", data.token)
                editor.putString("name", data.user.name)
                editor.putString("email", data.user.email)
            }
            is GoogleLoginResponse -> {
                editor.putBoolean("success", data.success)
                editor.putString("message", data.message)
                editor.putString("token", data.token)
                editor.putString("name", data.name)
                editor.putString("email", data.email)
            }
            else -> throw IllegalArgumentException("Unsupported data type")
        }

        editor.apply()
    }

    fun getData(): LoginResponse {
        val token = preference.getString("token", "")
        val message = preference.getString("message", "")
        val name = preference.getString("name", "")
        val email = preference.getString("email", "")
        val success = preference.getBoolean("success", false)

        return LoginResponse(
            success,
            message ?: "",
            token ?: "",
            User(0, name ?: "", email ?: "")
        )
    }

    fun removeData() {
        var editor = preference.edit()
        editor.remove("token")
        editor.remove("name")
        editor.remove("email")
        editor.remove("role")
        editor.remove("message")
        editor.remove("success")
        editor.apply()
    }
}