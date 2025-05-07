package com.example.biomapsapp.data

data class GoogleLoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val name: String,
    val email: String
)
