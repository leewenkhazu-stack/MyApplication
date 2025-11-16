package com.fsl.myapplication.auth

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.fsl.myapplication.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import java.security.SecureRandom

/**
 * Modern Google Sign-In Manager following Google's official Credential Manager guidelines.
 * Implements the complete flow with proper credential management and security best practices.
 * Updated to follow GitHub Copilot's security recommendations.
 */
class ModernGoogleSignInManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    private var isConfigured = false
    private var webClientId: String? = null

    companion object {
        private const val TAG = "ModernGoogleSignIn"
    }

    init {
        initializeGoogleSignIn()
    }

    private fun initializeGoogleSignIn() {
        try {
            webClientId = context.getString(R.string.default_web_client_id).takeIf { it.isNotBlank() }
            if (webClientId != null) {
                isConfigured = true
                Log.d(TAG, "Google Identity Services configured successfully")
            } else {
                Log.e(TAG, "default_web_client_id is blank or missing")
            }
        } catch (_: android.content.res.Resources.NotFoundException) {
            Log.e(TAG, "default_web_client_id not found in resources")
            webClientId = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Identity Services: ${e.javaClass.simpleName}")
        }
    }

    /**
     * Generates a cryptographically secure nonce for improved security.
     * This helps prevent replay attacks.
     */
    fun createNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
        )
    }

    /**
     * Initiates the Google credential request and returns the ID token string on success.
     * This method DOES NOT sign in with Firebase; it simply returns the Google ID token
     * so the caller (ViewModel) can verify the nonce and complete authentication.
     */
    suspend fun requestGoogleIdToken(nonce: String): Result<String> {
        try {
            if (!isConfigured || webClientId == null) {
                return Result.failure(Exception("Google Sign-In is not configured."))
            }

            // Use GetGoogleIdOption and attach provided nonce
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId!!)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()

            val request = androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context as? ComponentActivity ?: return Result.failure(
                    Exception("Context must be a ComponentActivity for credential manager operations")
                )
            )

            // Extract ID token from response without early returns
            val credential = result.credential
            val idTokenResult = if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                if (idToken.isBlank()) {
                    Result.failure(Exception("ID token missing from credential"))
                } else {
                    Result.success(idToken)
                }
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential::class.java.simpleName}"))
            }

            return idTokenResult

        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign-in failed: ${e.javaClass.simpleName}")
            return Result.failure(Exception("Sign-in failed: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error: ${e.javaClass.simpleName}")
            return Result.failure(Exception("Unexpected error occurred during sign-in"))
        }
    }

    /**
     * Signs out from the app and clears credential state.
     * This is crucial for proper sign-out behavior and security.
     * Updated to ensure complete cleanup and proper error handling.
     */
    suspend fun signOut(): Result<String> {
        return try {
            Log.d(TAG, "Starting sign-out process")

            // First, sign out from Firebase
            auth.signOut()
            Log.d(TAG, "Firebase sign-out completed")

            // Then clear credential state from all providers (IMPORTANT!)
            // This ensures that stored credential sessions are cleared
            val clearResult = clearCredentialState()

            if (clearResult) {
                Log.d(TAG, "Complete sign-out successful")
                Result.success("Sign out successful")
            } else {
                Log.w(TAG, "Sign-out completed but credential state clearing failed")
                // Still consider it successful since Firebase sign-out worked
                Result.success("Sign out successful")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed: ${e.javaClass.simpleName}")
            Result.failure(Exception("Sign out failed"))
        }
    }

    /**
     * Clears the current user credential state from all credential providers.
     * This should be called when users explicitly sign out.
     * Updated to return success/failure status.
     */
    private suspend fun clearCredentialState(): Boolean {
        return try {
            credentialManager.clearCredentialState(
                request = ClearCredentialStateRequest()
            )
            Log.d(TAG, "Credential state cleared successfully")
            true
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Failed to clear credential state: ${e.javaClass.simpleName}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error clearing credential state: ${e.javaClass.simpleName}")
            false
        }
    }
}

@Composable
fun rememberModernGoogleSignInManager(): ModernGoogleSignInManager {
    val context = LocalContext.current
    return remember { ModernGoogleSignInManager(context) }
}