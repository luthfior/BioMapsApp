package com.example.biomapsapp.utils

import android.content.Context
import androidx.core.content.edit
import com.example.biomapsapp.data.LoginResponse
import com.example.biomapsapp.model.User

object TokenManager {
    private const val PREF_NAME = "bio_maps_app_prefs"
    private const val NAME = "jwt_token"
    private const val EMAIL = "jwt_token"
    private const val KEY_JWT_TOKEN = "jwt_token"

    fun saveAccount(context: Context, loginResponse: LoginResponse) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() {
            putString(KEY_JWT_TOKEN, loginResponse.token)
            putString(NAME, loginResponse.user.name)
            putString(EMAIL, loginResponse.user.email)
        }
    }

    fun getAccount(context: Context, loginResponse: LoginResponse): MutableList<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mutableListOf<String>(
            loginResponse.token,
            loginResponse.user.name,
            loginResponse.user.email
        )
    }

    fun clearToken(context: Context, loginResponse: LoginResponse) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() {
            remove(KEY_JWT_TOKEN)
            remove(NAME)
            remove(EMAIL)
        }
    }
}
