package com.fsl.myapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating AuthViewModel with dependencies
 */
class AuthViewModelFactory(
    private val googleSignInManager: ModernGoogleSignInManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(googleSignInManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}