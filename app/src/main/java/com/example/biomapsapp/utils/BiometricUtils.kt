package com.example.biomapsapp.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.Executor

class ShowBiometricPrompt(
    private val context: Context,
    private val onAuthenticationSucceeded: () -> Unit,
    private val onAuthenticationFailed: () -> Unit,
    private val onAuthenticationError: (String) -> Unit,
    private val onAuthenticationNotAvailable: () -> Unit
) {
    private val executor = ContextCompat.getMainExecutor(context)
    private val biometricManager = BiometricManager.from(context)
    private var biometricPrompt: BiometricPrompt? = null

    init {
        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Log.d("Biometric", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("Biometric", "No biometric features available on this device.")
                onAuthenticationNotAvailable()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("Biometric", "Biometric hardware is currently unavailable.")
                onAuthenticationNotAvailable()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e("Biometric", "The user hasn't associated any biometric credentials with their account.")
                onAuthenticationNotAvailable()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Log.e("Biometric", "Security update required.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Log.e("Biometric", "Biometric sensors are disabled.")
            else -> Log.w("Biometric", "Unknown biometric state.")
        }

        biometricPrompt = (context as? FragmentActivity)?.let {
            BiometricPrompt(
                it,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onAuthenticationError(errString.toString())
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onAuthenticationSucceeded()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onAuthenticationFailed()
                    }
                })
        }
    }

    fun showBiometricPrompt(title: String, description: String) {
        val promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(if (Build.VERSION.SDK_INT >= 30) BIOMETRIC_STRONG or DEVICE_CREDENTIAL else BIOMETRIC_STRONG)
            .apply {
                if (Build.VERSION.SDK_INT < 30) {
                    setNegativeButtonText("Cancel")
                }
            }
            .build()

        biometricPrompt?.authenticate(promptInfo)
            ?: run {
                Log.e("Biometric", "BiometricPrompt is null, FragmentActivity context might be missing.")
                onAuthenticationNotAvailable()
            }
    }
}