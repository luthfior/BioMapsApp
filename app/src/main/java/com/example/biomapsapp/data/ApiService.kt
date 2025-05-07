package com.example.biomapsapp.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body payload: Map<String, String>): Response<GoogleLoginResponse>
}