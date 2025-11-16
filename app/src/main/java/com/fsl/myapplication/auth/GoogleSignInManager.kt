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
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.fsl.myapplication.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
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
            webClientId = context.getString(R.string.default_web_client_id)
            isConfigured = true
            Log.d(TAG, "Google Identity Services configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Identity Services: ${e.javaClass.simpleName}")
        }
    }

    /**
     * Generates a cryptographically secure nonce for improved security.
     * This helps prevent replay attacks.
     */
    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
        )
    }

    /**
     * Handles the "Sign in with Google" button flow using the official API.
     * This should be used when user explicitly clicks the Sign in with Google button.
     */
    suspend fun signInWithGoogleButton(): Result<String> {
        return try {
            if (!isConfigured || webClientId == null) {
                return Result.failure(Exception("Google Sign-In is not configured."))
            }

            val nonce = generateNonce()

            // Use GetGoogleIdOption for the modern bottom sheet UI (not GetSignInWithGoogleOption)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all accounts for button clicks
                .setServerClientId(webClientId!!)
                .setAutoSelectEnabled(false) // Disable auto-select for explicit button clicks
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context as ComponentActivity
            )

            handleCredentialResponse(result)

        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign in with Google button failed: ${e.javaClass.simpleName}")
            Result.failure(Exception("Sign-in failed: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error: ${e.javaClass.simpleName}")
            Result.failure(Exception("Unexpected error occurred during sign-in"))
        }
    }

    /**
     * Handles the credential response from Credential Manager.
     * Supports Google ID tokens, passwords, and passkeys.
     */
    private suspend fun handleCredentialResponse(result: GetCredentialResponse): Result<String> {
        return try {
            when (val credential = result.credential) {
                // Handle Google ID Token (our primary use case)
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        handleGoogleIdToken(credential)
                    } else {
                        Log.e(TAG, "Unexpected custom credential type: ${credential.type}")
                        Result.failure(Exception("Unexpected credential type"))
                    }
                }

                // Handle password credentials (if you support password auth)
                is PasswordCredential -> {
                    Log.d(TAG, "Password credential received: ${credential.id}")
                    // You can implement password sign-in here if needed
                    Result.failure(Exception("Password sign-in not implemented"))
                }

                // Handle passkey credentials (if you support passkeys)
                is PublicKeyCredential -> {
                    Log.d(TAG, "Passkey credential received")
                    // You can implement passkey sign-in here if needed
                    Result.failure(Exception("Passkey sign-in not implemented"))
                }

                else -> {
                    Log.e(TAG, "Unexpected credential type: ${credential::class.java.simpleName}")
                    Result.failure(Exception("Unexpected credential type"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle credential response: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * Processes the Google ID token and authenticates with Firebase.
     * Updated to avoid logging sensitive user information.
     */
    private suspend fun handleGoogleIdToken(credential: CustomCredential): Result<String> {
        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            // Don't log the actual ID token or sensitive user data

            Log.d(TAG, "Google Sign-In successful for user")
            // Only log non-sensitive information
            Log.d(TAG, "User has display name: ${googleIdTokenCredential.displayName != null}")
            Log.d(
                TAG,
                "User has profile picture: ${googleIdTokenCredential.profilePictureUri != null}"
            )

            // Authenticate with Firebase using the Google ID token
            val firebaseCredential =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user

            firebaseUser?.let {
                Log.d(TAG, "Firebase authentication successful")
                // Don't log the actual UID or sensitive user information
                Result.success("Sign-in successful")
            } ?: Result.failure(Exception("Firebase user is null"))

        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Invalid Google ID token: ${e.javaClass.simpleName}")
            Result.failure(Exception("Invalid Google ID token"))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed: ${e.javaClass.simpleName}")
            Result.failure(Exception("Authentication failed"))
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