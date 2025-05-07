package com.example.biomapsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biomapsapp.repository.AuthRepository
import com.example.biomapsapp.data.GoogleLoginResponse
import com.example.biomapsapp.data.LoginResponse
import com.example.biomapsapp.data.RegisterResponse
import com.example.biomapsapp.utils.AuthPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerResponse = MutableStateFlow<RegisterResponse?>(null)
    val registerResponse: StateFlow<RegisterResponse?> = _registerResponse.asStateFlow()

    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)
    val loginResponse: StateFlow<LoginResponse?> = _loginResponse.asStateFlow()

    private val _googleLoginResponse = MutableStateFlow<GoogleLoginResponse?>(null)
    val googleLoginResponse: StateFlow<GoogleLoginResponse?> = _googleLoginResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = authRepository.register(name, email, password)
            _registerResponse.value = response
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = authRepository.login(email, password)
            _loginResponse.value = response
            _isLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = authRepository.loginWithGoogle(idToken)
            _googleLoginResponse.value = response
            _isLoading.value = false
        }
    }

    fun resetLoginResponse() {
        _loginResponse.value = null
    }
    fun resetRegisterResponse() {
        _registerResponse.value = null
    }
    fun resetGoogleLoginResponse() {
        _googleLoginResponse.value = null
    }
}