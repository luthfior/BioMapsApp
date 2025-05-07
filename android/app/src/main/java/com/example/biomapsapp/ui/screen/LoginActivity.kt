@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.biomapsapp.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.biomapsapp.BuildConfig
import com.example.biomapsapp.MainActivity
import com.example.biomapsapp.R
import com.example.biomapsapp.repository.AuthRepository
import com.example.biomapsapp.ui.component.BottomLinkQuestion
import com.example.biomapsapp.ui.component.TextFieldComp
import com.example.biomapsapp.ui.component.ErrorText
import com.example.biomapsapp.ui.component.GoogleLoginButton
import com.example.biomapsapp.ui.component.HeaderWelcome
import com.example.biomapsapp.ui.component.LoginOrRegisterButton
import com.example.biomapsapp.ui.component.OrDivider
import com.example.biomapsapp.ui.component.PasswordTextField
import com.example.biomapsapp.ui.theme.BioMapsAppTheme
import com.example.biomapsapp.utils.AuthPreference
import com.example.biomapsapp.viewmodel.AuthViewModel
import com.example.biomapsapp.utils.AuthViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

class LoginActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = AuthRepository()
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        val webClientId = BuildConfig.DEFAULT_WEB_CLIENT_ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        googleSignInClient = client

        googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    authViewModel.loginWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
            }
        }

        setContent {
            BioMapsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        context = this,
                        authViewModel = authViewModel,
                        onRegisterClick = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        },
                        onLoginSuccess = {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onGoogleSignInClick = {
                            val signInIntent = googleSignInClient.signInIntent
                            googleLauncher.launch(signInIntent)
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun LoginScreen(
    context: Context?,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsState()
    val loginResponse by authViewModel.loginResponse.collectAsState()
    val googleResponse by authViewModel.googleLoginResponse.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(loginResponse) {
        loginResponse?.let { response ->
            if (response.success) {
                context?.let {
                    AuthPreference(it).saveData(response)
                }
                onLoginSuccess()
            } else {
                loginError = response.message ?: "Login gagal"
                showDialog = true
                Log.e("Login", response.message)
                Log.d("LoginDebug", "Response: $loginResponse")
            }
        }
    }

    LaunchedEffect(googleResponse) {
        googleResponse?.let { response ->
            if (response.success) {
                context?.let {
                    AuthPreference(it).saveData(response)
                }
                onLoginSuccess()
            } else {
                loginError = response.message ?: "Google Login gagal"
                showDialog = true
                Log.e("GoogleLogin", response.message)
            }
        }
    }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            delay(4000L)
            showDialog = false
            authViewModel.resetLoginResponse()
            authViewModel.resetGoogleLoginResponse()
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
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
                    text2 = "Silahkan Masuk"
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
                    email,
                    emailError,
                    "Email"
                ) {
                    email = it
                    emailError = false
                }

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    password,
                    passwordError,
                    "Password"
                ) {
                    password = it
                    passwordError = false
                }

                if (emailError) ErrorText("Email tidak boleh kosong")
                if (passwordError) ErrorText("Password tidak boleh kosong")

                Spacer(modifier = Modifier.height(24.dp))

                LoginOrRegisterButton(
                    "Login",
                    enabled = !isLoading
                ) {
                    if (email.isBlank()) emailError = true
                    if (password.isBlank()) passwordError = true
                    if (emailError || passwordError) return@LoginOrRegisterButton

                    loginError = ""
                    authViewModel.login(email, password)
                }

                OrDivider()
                GoogleLoginButton {
                    onGoogleSignInClick()
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
                    onRegisterClick,
                    textQ = "Belum punya Akun?",
                    textB = "Daftar"
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
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        authViewModel.resetLoginResponse()
                    },
                    title = {
                        androidx.compose.material3.Text("Login Gagal")
                    },
                    text = {
                        androidx.compose.material3.Text(loginError)
                    },
                    confirmButton = {}
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenPreview() {
    BioMapsAppTheme {
        LoginScreen(
            context = null,
            authViewModel = AuthViewModel(AuthRepository()),
            onRegisterClick = {},
            onLoginSuccess = {},
            onGoogleSignInClick = {}
        )
    }
}
