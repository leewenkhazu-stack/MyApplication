package com.fsl.myapplication.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for authentication logic following security best practices.
 * Implements GitHub Copilot's recommendations for proper architecture.
 */
class AuthViewModel(
    private val googleSignInManager: ModernGoogleSignInManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Private mutable state
    private val _uiState = MutableStateFlow(AuthUiState.initial())

    // Public read-only state
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Firebase auth state listener
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            // User signed out, reset all loading states
            resetAllStates()
        } else {
            // User signed in, reset loading states
            resetLoadingStates()
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"

        // Minimum password length for basic validation
        private const val MIN_PASSWORD_LENGTH = 6
    }

    init {
        // Listen to Firebase auth state changes
        auth.addAuthStateListener(authStateListener)
    }

    /**
     * Handles all authentication actions with proper error handling and security
     */
    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.UpdateEmail -> updateEmail(action.email)
            is AuthAction.UpdatePassword -> updatePassword(action.password)
            AuthAction.ToggleAuthMode -> toggleAuthMode()
            AuthAction.SignInWithEmail -> signInWithEmail()
            AuthAction.SignInWithGoogle -> signInWithGoogle()
            AuthAction.ClearError -> clearError()
            AuthAction.ClearForm -> clearForm()
            AuthAction.ResetState -> resetAllStates()
        }
    }

    private fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            isFormValid = isFormValid(email, _uiState.value.password)
        )
    }

    private fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            isFormValid = isFormValid(_uiState.value.email, password)
        )
    }

    private fun toggleAuthMode() {
        clearError()
        _uiState.value = _uiState.value.copy(
            isSignUp = !_uiState.value.isSignUp
        )
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            email = "",
            password = "",
            errorMessage = null,
            isFormValid = false
        )
    }

    /**
     * Resets all loading states (used when user signs out)
     */
    private fun resetAllStates() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isGoogleSignInLoading = false,
            errorMessage = null
        )
        Log.d(TAG, "All states reset after sign-out")
    }

    /**
     * Resets only loading states (used when user signs in successfully)
     */
    private fun resetLoadingStates() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isGoogleSignInLoading = false
        )
        Log.d(TAG, "Loading states reset after successful sign-in")
    }

    /**
     * Validates form inputs without logging sensitive data
     */
    private fun isFormValid(email: String, password: String): Boolean {
        return email.isNotBlank() &&
                email.contains("@") &&
                password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Performs email/password authentication with security best practices
     */
    private fun signInWithEmail() {
        val currentState = _uiState.value

        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid email and password (minimum $MIN_PASSWORD_LENGTH characters)"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val email = currentState.email
                val password = currentState.password

                if (currentState.isSignUp) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    Log.d(TAG, "Account creation successful")
                } else {
                    auth.signInWithEmailAndPassword(email, password).await()
                    Log.d(TAG, "Email sign-in successful")
                }

                // Clear password from memory after successful authentication
                clearPasswordFromMemory()

            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Authentication failed: ${e.errorCode}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = mapFirebaseError(e),
                    isLoading = false
                )
                // Clear password from memory after failed authentication
                clearPasswordFromMemory()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected authentication error: ${e.javaClass.simpleName}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "An unexpected error occurred. Please try again.",
                    isLoading = false
                )
                // Clear password from memory after failed authentication
                clearPasswordFromMemory()
            }
        }
    }

    /**
     * Performs Google Sign-In with proper error handling
     */
    private fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGoogleSignInLoading = true,
                errorMessage = null
            )

            try {
                val result = googleSignInManager.signInWithGoogleButton()

                if (result.isSuccess) {
                    Log.d(TAG, "Google Sign-In successful")
                    // Loading state will be reset by Firebase auth state listener
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Google Sign-In failed: ${error?.javaClass?.simpleName}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Google Sign-In failed. Please try again.",
                        isGoogleSignInLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In error: ${e.javaClass.simpleName}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Google Sign-In failed. Please try again.",
                    isGoogleSignInLoading = false
                )
            }
        }
    }

    /**
     * Clears password from memory for security
     */
    private fun clearPasswordFromMemory() {
        _uiState.value = _uiState.value.copy(
            password = "",
            isLoading = false
        )
    }

    /**
     * Maps Firebase authentication errors to user-friendly messages
     * without exposing internal details
     */
    private fun mapFirebaseError(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
            "ERROR_WEAK_PASSWORD" -> "Password should be at least $MIN_PASSWORD_LENGTH characters."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
            else -> "Authentication failed. Please try again."
        }
    }

    /**
     * Sign out with proper cleanup
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                // Set loading state to indicate sign-out process
                _uiState.value = _uiState.value.copy(isGoogleSignInLoading = true)

                val result = googleSignInManager.signOut()
                if (result.isSuccess) {
                    Log.d(TAG, "Sign out successful")
                    // State will be reset by Firebase auth state listener
                } else {
                    Log.e(
                        TAG,
                        "Sign out failed: ${result.exceptionOrNull()?.javaClass?.simpleName}"
                    )
                    // Reset loading state if sign-out failed
                    _uiState.value = _uiState.value.copy(isGoogleSignInLoading = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out error: ${e.javaClass.simpleName}")
                // Reset loading state if sign-out failed
                _uiState.value = _uiState.value.copy(isGoogleSignInLoading = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove Firebase auth state listener
        auth.removeAuthStateListener(authStateListener)
        // Clear any sensitive data when ViewModel is destroyed
        clearForm()
        Log.d(TAG, "AuthViewModel cleared")
    }
}