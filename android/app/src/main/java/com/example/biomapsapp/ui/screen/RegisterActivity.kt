@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.biomapsapp.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.biomapsapp.repository.AuthRepository
import com.example.biomapsapp.ui.component.BottomLinkQuestion
import com.example.biomapsapp.ui.component.TextFieldComp
import com.example.biomapsapp.ui.component.ErrorText
import com.example.biomapsapp.ui.component.HeaderWelcome
import com.example.biomapsapp.ui.component.LoginOrRegisterButton
import com.example.biomapsapp.ui.component.PasswordTextField
import com.example.biomapsapp.ui.theme.BioMapsAppTheme
import com.example.biomapsapp.utils.AuthPreference
import com.example.biomapsapp.viewmodel.AuthViewModel
import com.example.biomapsapp.utils.AuthViewModelFactory
import kotlinx.coroutines.delay

class RegisterActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = AuthRepository()
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            BioMapsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onRegisterSuccess = {
                            finish()
                        },
                        onBackToLogin = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState(false)
    var errorMessage by remember { mutableStateOf("") }

    val registerResponse by authViewModel.registerResponse.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(registerResponse) {
        registerResponse?.let { response ->
            dialogSuccess = response.success
            dialogMessage = response.message ?: if (dialogSuccess) "Akun berhasil didaftarkan" else "Gagal mendaftar"
            showDialog = true
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                HeaderWelcome(
                    text1 = "Halo, Selamat Datang",
                    text2 = "Silahkan Daftar"
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextFieldComp(
                    name,
                    false,
                    "Nama Lengkap",
                ) {
                    name = it
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextFieldComp(
                    email,
                    false,
                    "Email"
                ) {
                    email = it
                }
                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    password,
                    false,
                    "Password",
                ) {
                    password = it
                }
                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    confirmPassword,
                    false,
                    "Konfirmasi Password",
                ) {
                    confirmPassword = it
                }

                Spacer(modifier = Modifier.height(8.dp))
                ErrorText(errorMessage)
                Spacer(modifier = Modifier.height(8.dp))

                LoginOrRegisterButton(
                    "Daftar",
                    enabled = !isLoading
                ) {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Semua field harus diisi"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Password tidak cocok"
                        }
                        else -> {
                            authViewModel.register(name, email, password)
                            showDialog = false
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomLinkQuestion(
                    onBackToLogin,
                    "Sudah punya Akun?",
                    "Login"
                )
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (showDialog) {
                LaunchedEffect(showDialog) {
                    if (showDialog) {
                        delay(4000L)
                        showDialog = false
                        if (dialogSuccess) {
                            onRegisterSuccess()
                        } else {
                            authViewModel.resetRegisterResponse()
                        }
                    }
                }
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    },
                    title = {
                        androidx.compose.material3.Text(
                            if (dialogSuccess) "Berhasil" else "Gagal"
                        )
                    },
                    text = {
                        androidx.compose.material3.Text(dialogMessage)
                    },
                    confirmButton = {}
                )
            }
        }
    }

//        OutlinedTextField(
//            value = name,
//            onValueChange = { name = it },
//            label = { Text("Nama Lengkap") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BioMapsAppTheme {
        RegisterScreen(
            authViewModel = AuthViewModel(AuthRepository()),
            onRegisterSuccess = {},
            onBackToLogin = {}
        )
    }
}