package com.fsl.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsl.myapplication.auth.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // Create dependencies and ViewModel for proper state management
    val googleSignInManager = rememberModernGoogleSignInManager()
    val viewModelFactory = remember(googleSignInManager) {
        AuthViewModelFactory(googleSignInManager)
    }
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        user?.let { currentUser ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Information",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Name: ${currentUser.displayName ?: "Not available"}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Email: ${currentUser.email ?: "Not available"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Don't display the full User ID for security reasons
                    Text(
                        text = "User ID: ${currentUser.uid.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "Email Verified: ${if (currentUser.isEmailVerified) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Show provider information
                    val providers =
                        currentUser.providerData.map { it.providerId }.filter { it != "firebase" }
                    if (providers.isNotEmpty()) {
                        Text(
                            text = "Sign-in providers: ${providers.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Use ViewModel for sign-out to ensure proper state management
        Button(
            onClick = {
                authViewModel.signOut()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isGoogleSignInLoading && !uiState.isLoading
        ) {
            if (uiState.isGoogleSignInLoading || uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Signing out...")
            } else {
                Text("Sign Out")
            }
        }

        // Show error message if sign-out fails
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}