package com.fsl.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsl.myapplication.auth.*
import com.fsl.myapplication.ui.screens.components.*

@Composable
fun AuthScreen() {
    // ViewModel and dependencies
    val googleSignInManager = rememberModernGoogleSignInManager()
    val viewModelFactory = remember(googleSignInManager) {
        AuthViewModelFactory(googleSignInManager)
    }
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    // Local state
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Keyboard and animation management
    val (keyboardOffset, dismissKeyboard) = useKeyboardHandling()
    val (premiumSpring, subtleSpring) = useAnimationSpecs()
    val (emailButtonInteraction, googleButtonInteraction, toggleButtonInteraction) = useInteractionSources()
    val (emailButtonScale, googleButtonScale, toggleButtonScale) = useButtonAnimations(
        premiumSpring = premiumSpring,
        subtleSpring = subtleSpring,
        uiState = uiState,
        emailButtonPressed = emailButtonInteraction.collectIsPressedAsState().value,
        googleButtonPressed = googleButtonInteraction.collectIsPressedAsState().value,
        toggleButtonPressed = toggleButtonInteraction.collectIsPressedAsState().value
    )

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(y = -keyboardOffset)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { dismissKeyboard() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        AuthTitle(isSignUp = uiState.isSignUp)
        Spacer(modifier = Modifier.height(32.dp))

        // Email input
        EmailInputField(
            email = uiState.email,
            onEmailChange = { viewModel.handleAction(AuthAction.UpdateEmail(it)) },
            isEnabled = !uiState.isLoading && !uiState.isGoogleSignInLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password input
        PasswordInputField(
            password = uiState.password,
            onPasswordChange = { viewModel.handleAction(AuthAction.UpdatePassword(it)) },
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityToggle = { isPasswordVisible = it },
            isEnabled = !uiState.isLoading && !uiState.isGoogleSignInLoading,
            onDoneAction = {
                dismissKeyboard()
                if (uiState.isFormValid) {
                    viewModel.handleAction(AuthAction.SignInWithEmail)
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Email auth button
        AuthenticationButton(
            isLoading = uiState.isLoading,
            isSignUp = uiState.isSignUp,
            isEnabled = uiState.isFormValid && !uiState.isLoading && !uiState.isGoogleSignInLoading,
            onClick = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.SignInWithEmail)
            },
            buttonScale = emailButtonScale,
            interactionSource = emailButtonInteraction
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        AuthMethodDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Google button
        GoogleSignInButton(
            isLoading = uiState.isGoogleSignInLoading,
            isEnabled = !uiState.isLoading && !uiState.isGoogleSignInLoading,
            onClick = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.SignInWithGoogle)
            },
            buttonScale = googleButtonScale,
            interactionSource = googleButtonInteraction
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Toggle mode
        AuthModeToggle(
            isSignUp = uiState.isSignUp,
            onToggle = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.ToggleAuthMode)
            },
            isEnabled = !uiState.isLoading && !uiState.isGoogleSignInLoading,
            buttonScale = toggleButtonScale,
            interactionSource = toggleButtonInteraction
        )

        // Error message
        ErrorMessageCard(
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.handleAction(AuthAction.ClearError) }
        )
    }
}

/**
 * Keyboard handling hook - manages IME visibility and animated offset
 * Returns: Pair of (keyboardOffset, dismissKeyboard lambda)
 */
@Composable
private fun useKeyboardHandling(): Pair<Dp, () -> Unit> {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    val imeInsets = WindowInsets.ime
    val imeBottomPx = imeInsets.getBottom(density)
    val imeVisible = imeBottomPx > 0
    val imeHeightDp = with(density) { imeBottomPx.toDp() }

    val keyboardOffset by animateDpAsState(
        targetValue = if (imeVisible) imeHeightDp * 0.35f else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyboardOffset"
    )

    val dismissKeyboard = remember {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
            Unit
        }
    }

    return Pair(keyboardOffset, dismissKeyboard)
}

/**
 * Animation specs hook - memorizes spring animation specs
 * Returns: Pair of (premiumSpring, subtleSpring)
 */
@Composable
private fun useAnimationSpecs(): Pair<SpringSpec<Float>, SpringSpec<Float>> {
    val premiumSpring = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    val subtleSpring = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }
    return Pair(premiumSpring, subtleSpring)
}

/**
 * Interaction sources hook - creates memoized interaction sources for button press states
 * Returns: Triple of (emailButtonInteraction, googleButtonInteraction, toggleButtonInteraction)
 */
@Composable
private fun useInteractionSources(): Triple<
    MutableInteractionSource,
    MutableInteractionSource,
    MutableInteractionSource
> {
    val emailButtonInteraction = remember { MutableInteractionSource() }
    val googleButtonInteraction = remember { MutableInteractionSource() }
    val toggleButtonInteraction = remember { MutableInteractionSource() }
    return Triple(emailButtonInteraction, googleButtonInteraction, toggleButtonInteraction)
}

/**
 * Button animations hook - computes button scale animations based on press and loading states
 * Returns: Triple of (emailButtonScale, googleButtonScale, toggleButtonScale)
 */
@Composable
private fun useButtonAnimations(
    premiumSpring: SpringSpec<Float>,
    subtleSpring: SpringSpec<Float>,
    uiState: AuthUiState,
    emailButtonPressed: Boolean,
    googleButtonPressed: Boolean,
    toggleButtonPressed: Boolean
): Triple<Float, Float, Float> {
    val emailButtonScale by animateFloatAsState(
        targetValue = when {
            uiState.isLoading -> 0.96f
            emailButtonPressed -> 0.94f
            else -> 1f
        },
        animationSpec = premiumSpring,
        label = "emailButtonScale"
    )

    val googleButtonScale by animateFloatAsState(
        targetValue = when {
            uiState.isGoogleSignInLoading -> 0.96f
            googleButtonPressed -> 0.94f
            else -> 1f
        },
        animationSpec = premiumSpring,
        label = "googleButtonScale"
    )

    val toggleButtonScale by animateFloatAsState(
        targetValue = if (toggleButtonPressed) 0.96f else 1f,
        animationSpec = subtleSpring,
        label = "toggleButtonScale"
    )

    return Triple(emailButtonScale, googleButtonScale, toggleButtonScale)
}
