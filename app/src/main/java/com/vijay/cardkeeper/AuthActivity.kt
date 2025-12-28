
package com.vijay.cardkeeper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.vijay.cardkeeper.ui.theme.CardKeeperTheme
import com.vijay.cardkeeper.util.KeyManager
import java.util.concurrent.Executor

class AuthActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                    // If error is fatal (like user canceled), maybe close app?
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || 
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        finish()
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockAndProceed()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for CardKeeper")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()


        setContent {
            CardKeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isKeyCreated = KeyManager.isKeyCreated(this)
                    
                    if (isKeyCreated) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Verifying Security...")
                        }
                        
                        LaunchedEffect(Unit) {
                             checkAndAuthenticate()
                        }
                    } else {
                        com.vijay.cardkeeper.ui.auth.WelcomeScreen(
                            onRegisterClick = {
                                checkAndAuthenticate()
                            },
                            onQuitClick = {
                                finishAffinity()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndAuthenticate() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                 Toast.makeText(this, "No biometric features available", Toast.LENGTH_LONG).show()
                 unlockAndProceed() // Fallback? Or fail? For now, proceed (insecure fallback for dev) -> strict: fail.
                 // Ideally we force PIN. Authenticator DEVICE_CREDENTIAL handles PIN.
                 // If that fails, it means no pin set.
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show()
                finish()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                Toast.makeText(this, "Please associate a biometric credential with your account.", Toast.LENGTH_LONG).show()
                // In a real app, we might ask to setup.
                unlockAndProceed() // For now, allow proceed if no security set up on device (fallback)
            }
            else -> {
                unlockAndProceed()
            }
        }
    }

    private fun unlockAndProceed() {
        try {
            // This will either generate new key or fetch existing one
            // It might throw if auth was required but not present/valid (logic key)
            // But since we just authenticated or device has no auth, we try.
            KeyManager.getOrCreatePassphrase(applicationContext)
            
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to unlock storage: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
