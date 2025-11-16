package com.fsl.myapplication.auth

/**
 * Represents the UI state for authentication screen
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGoogleSignInLoading: Boolean = false,
    val isSignUp: Boolean = false,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    // Pending nonce stored in ViewModel state so it survives configuration changes
    val pendingNonce: String? = null
) {
    companion object {
        fun initial() = AuthUiState()
    }
}

/**
 * Represents different authentication events/actions
 */
sealed class AuthAction {
    data class UpdateEmail(val email: String) : AuthAction()
    data class UpdatePassword(val password: String) : AuthAction()
    object ToggleAuthMode : AuthAction()
    object SignInWithEmail : AuthAction()
    object SignInWithGoogle : AuthAction()
    object ClearError : AuthAction()
    object ClearForm : AuthAction()
    object ResetState : AuthAction() // Added for proper sign-out handling
}
