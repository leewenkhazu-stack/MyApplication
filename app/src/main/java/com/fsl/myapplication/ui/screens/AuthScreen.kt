package com.fsl.myapplication.ui.screens

import com.fsl.myapplication.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsl.myapplication.auth.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun AuthScreen() {
    // Create dependencies
    val googleSignInManager = rememberModernGoogleSignInManager()
    val viewModelFactory = remember(googleSignInManager) {
        AuthViewModelFactory(googleSignInManager)
    }

    // Get ViewModel instance
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    // Password visibility state
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Focus & keyboard controllers
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // IME / keyboard insets handling
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeBottomPx = imeInsets.getBottom(density)
    val imeVisible = imeBottomPx > 0
    val imeHeightDp = with(density) { imeBottomPx.toDp() }

    // Animated keyboard offset
    val keyboardOffset by animateDpAsState(
        targetValue = if (imeVisible) imeHeightDp * 0.35f else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyboardOffset"
    )

    // Remembered animation specs (avoiding re-allocation)
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

    // Remembered interaction sources (avoiding re-allocation)
    val emailButtonInteraction = remember { MutableInteractionSource() }
    val googleButtonInteraction = remember { MutableInteractionSource() }
    val toggleButtonInteraction = remember { MutableInteractionSource() }

    val emailButtonPressed by emailButtonInteraction.collectIsPressedAsState()
    val googleButtonPressed by googleButtonInteraction.collectIsPressedAsState()
    val toggleButtonPressed by toggleButtonInteraction.collectIsPressedAsState()

    // Animated scales for buttons
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

    // Helper function to dismiss keyboard
    val dismissKeyboard = remember {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

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
        // Title with smooth animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState.isSignUp,
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                        )
                    ) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                        )
                    )).togetherWith(
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleOut(
                            targetScale = 0.85f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                    )
                },
                label = "titleAnimation"
            ) { targetIsSignUp ->
                Text(
                    text = if (targetIsSignUp) "Create Account" else "Sign in",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription =
                            if (targetIsSignUp) "Create Account screen" else "Sign in screen"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Email field with improved accessibility
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(500, delayMillis = 100, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(500, delayMillis = 100, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ) { it / 4 },
            label = "emailFieldAnimation"
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.handleAction(AuthAction.UpdateEmail(it)) },
                label = { Text("Email", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Email input field"
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                enabled = !uiState.isLoading && !uiState.isGoogleSignInLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password field with show/hide functionality using Material 3 icons
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(500, delayMillis = 200, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(500, delayMillis = 200, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ) { it / 4 },
            label = "passwordFieldAnimation"
        ) {
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.handleAction(AuthAction.UpdatePassword(it)) },
                label = { Text("Password", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Password input field"
                    },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        dismissKeyboard()
                        if (uiState.isFormValid) {
                            viewModel.handleAction(AuthAction.SignInWithEmail)
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                enabled = !uiState.isLoading && !uiState.isGoogleSignInLoading
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Email authentication button
        Button(
            onClick = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.SignInWithEmail)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(emailButtonScale)
                .semantics {
                    contentDescription =
                        if (uiState.isSignUp) "Create account button" else "Sign in button"
                },
            enabled = uiState.isFormValid && !uiState.isLoading && !uiState.isGoogleSignInLoading,
            interactionSource = emailButtonInteraction,
            border = BorderStroke(
                1.dp,
                if (uiState.isFormValid && !uiState.isLoading && !uiState.isGoogleSignInLoading)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            ),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                containerColor = if (emailButtonPressed && uiState.isFormValid)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(250, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                scaleOut(targetScale = 0.92f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                    },
                    label = "emailButtonContent"
                ) { loading ->
                    if (loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text(
                            text = if (uiState.isSignUp) "Sign up" else "Sign in",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with "or"
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing)
            ) + expandHorizontally(
                animationSpec = tween(
                    600,
                    delayMillis = 300,
                    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                ),
                expandFrom = Alignment.CenterHorizontally
            ),
            label = "dividerAnimation"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google sign-in button with improved accessibility
        Button(
            onClick = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.SignInWithGoogle)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(googleButtonScale)
                .semantics {
                    contentDescription = "Continue with Google button"
                },
            enabled = !uiState.isLoading && !uiState.isGoogleSignInLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF3C4043)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            interactionSource = googleButtonInteraction,
            border = BorderStroke(1.dp, Color(0xFFDADCE0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.isGoogleSignInLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(250, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                scaleOut(targetScale = 0.92f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                    },
                    label = "googleButtonContent"
                ) { loading ->
                    if (loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing in...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.google_logo),
                                contentDescription = "Google logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle sign up / sign in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(700, delayMillis = 400, easing = FastOutSlowInEasing)
            ),
            label = "toggleButtonAnimation"
        ) {
            Row(
                modifier = Modifier.height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.isSignUp) "Already have an account?" else "Don't have an account?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        dismissKeyboard()
                        viewModel.handleAction(AuthAction.ToggleAuthMode)
                    },
                    modifier = Modifier
                        .scale(toggleButtonScale)
                        .semantics {
                            contentDescription =
                                if (uiState.isSignUp) "Switch to sign in" else "Switch to sign up"
                        },
                    interactionSource = toggleButtonInteraction,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    enabled = !uiState.isLoading && !uiState.isGoogleSignInLoading
                ) {
                    Text(
                        text = if (uiState.isSignUp) "Sign in" else "Sign up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error message with improved UX
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn(
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ) + slideInVertically(
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
            ) { -it / 2 } + expandVertically(
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + slideOutVertically(
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) { -it / 2 } + shrinkVertically(
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top
            ),
            label = "errorAnimation"
        ) {
            uiState.errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clickable { viewModel.handleAction(AuthAction.ClearError) }
                            .semantics {
                                contentDescription = "Error message: $error. Tap to dismiss."
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}