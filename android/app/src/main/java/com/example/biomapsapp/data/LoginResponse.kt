package com.example.biomapsapp.data

import com.example.biomapsapp.model.User

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
)
