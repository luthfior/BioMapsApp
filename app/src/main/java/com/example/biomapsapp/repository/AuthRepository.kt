package com.example.biomapsapp.repository

import android.util.Log
import com.example.biomapsapp.data.ApiClient
import com.example.biomapsapp.data.ApiService
import com.example.biomapsapp.data.GoogleLoginResponse
import com.example.biomapsapp.data.LoginRequest
import com.example.biomapsapp.data.LoginResponse
import com.example.biomapsapp.data.RegisterRequest
import com.example.biomapsapp.data.RegisterResponse
import com.example.biomapsapp.model.User
import com.google.gson.Gson

class AuthRepository {

    private val apiService: ApiService = ApiClient.Companion.getApiService()

    suspend fun register(name: String, email: String, password: String): RegisterResponse? {
        val response = apiService.registerUser(RegisterRequest(name, email, password))
        Log.d("AuthRepository", "Register ErrorBody: ${response.errorBody()?.string()}")
        return if (response.isSuccessful) {
            Log.d("AuthRepository", "Register response: ${response.code()} ${response.message()}")
            response.body()
        } else {
            Log.d("AuthRepository", "Register response: ${response.code()} ${response.message()}")
            try {
                response.errorBody()?.string()?.let {
                    Gson().fromJson(it, RegisterResponse::class.java)
                }
            } catch (e: Exception) {
                Log.d("AuthRepository", "Register ErrorBody: ${response.errorBody()?.string()}")
                Log.d("AuthRepository", "Register error response: $e")
                Log.e("AuthRepository", "Register Parsing error: $e")
                RegisterResponse(
                    success = false,
                    message = "Registrasi gagal karena kesalahan server"
                )
            }
        }
    }

    suspend fun login(email: String, password: String): LoginResponse? {
        val response = apiService.loginUser(LoginRequest(email, password))
        Log.d("AuthRepository", "Login ErrorBody: ${response.errorBody()?.string()}")
        return if (response.isSuccessful) {
            Log.d("AuthRepository", "Login response: ${response.code()} ${response.message()}")
            val loginResponse = response.body()
            if (loginResponse?.token.isNullOrBlank()) {
                Log.d("AuthRepository", "Login ErrorBody: ${response.errorBody()?.string()}")
                val errorMessage = response.errorBody()?.string() ?: "Token kosong dan Login gagal"
                LoginResponse(
                    success = false,
                    message = errorMessage,
                    token = "",
                    user = User(0, "", "")
                )
            } else {
                return loginResponse
            }
        } else {
            try {
                Log.d("AuthRepository", "Login ErrorBody: ${response.errorBody()?.string()}")
                response.errorBody()?.string()?.let {
                    Gson().fromJson(it, LoginResponse::class.java)
                }
            } catch (e: Exception) {
                Log.d("AuthRepository", "Login ErrorBody: ${response.errorBody()?.string()}")
                Log.d("AuthRepository", "Login ErrorBody: $e")
                Log.e("AuthRepository", "Login Parsing error: $e")
                LoginResponse(
                    success = false,
                    message = "Terjadi kesalahan saat login",
                    token = "",
                    user = User(0, "", "")
                )
            }
        }
    }

    suspend fun loginWithGoogle(idToken: String): GoogleLoginResponse {
        return try {
            val response = apiService.loginWithGoogle(mapOf("idToken" to idToken))
            Log.d("GoogleSignIn", "ID Token: $idToken")
            if (response.isSuccessful && response.body() != null) {
                Log.d("AuthRepository", "LoginGoogle response: ${response.code()} ${response.message()}")
                Log.d("AuthRepository", "LoginGoogle response: ${response.body()}")
                response.body()!!
            } else {
                Log.d("AuthRepository", "LoginGoogle ErrorBody: ${response.errorBody()?.string()}")
                val errorMessage = response.errorBody()?.string() ?: "Token kosong dan Login gagal"
                GoogleLoginResponse(
                    success = false,
                    message = errorMessage,
                    token = "",
                    name = "",
                    email = ""
                )
            }
        } catch (e: Exception) {
            Log.d("AuthRepository", "LoginGoogle Error $e")
            Log.e("AuthRepository", "LoginGoogle Parsing error: $e")
            GoogleLoginResponse(
                success = false,
                message = "Terjadi kesalahan saat login",
                token = "",
                name = "",
                email = ""
            )
        }
    }

}