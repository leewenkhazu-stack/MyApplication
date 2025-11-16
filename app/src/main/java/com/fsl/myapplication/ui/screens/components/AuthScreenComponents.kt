package com.fsl.myapplication.ui.screens.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fsl.myapplication.R

/**
 * Animated title that switches between "Sign in" and "Create Account"
 * Single responsibility: render animated title based on auth mode
 */
@Composable
fun AuthTitle(
    isSignUp: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSignUp,
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
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    contentDescription =
                        if (targetIsSignUp) "Create Account screen" else "Sign in screen"
                }
            )
        }
    }
}

/**
 * Email input field with animation and accessibility
 * Single responsibility: render email input with validation feedback
 */
@Composable
fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Email input field" },
        enabled = isEnabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,

        // Floating label
        label = {
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium
            )
        },

        // Placeholder
        placeholder = {
            Text(
                text = "Enter your email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
        },

        // Leading icon (same family as visibility)
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = "Email icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                modifier = Modifier.size(22.dp)
            )
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),

        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
        )
    )
}




/**
 * Password input field with show/hide toggle
 * Single responsibility: render password input with visibility control
 */
@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityToggle: (Boolean) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onDoneAction: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    // Track visibility icon hover/press state
    val visibilityInteraction = remember { MutableInteractionSource() }
    val isPressed = visibilityInteraction.collectIsPressedAsState().value
    val isHovered = visibilityInteraction.collectIsHoveredAsState().value

    // Alpha overlay animation (same style as your buttons)
    val iconOverlayAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.22f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 120),
        label = "visibilityIconAlpha"
    )

    // NEW Material3 ripple (no deprecation)
    val rippleIndication = ripple(
        bounded = false,
        radius = 40.dp, // slightly larger → feels longer
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f)
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .semantics { contentDescription = "Password input field" },

        enabled = isEnabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,

        // Floating label
        label = {
            Text(
                text = "Password",
                style = MaterialTheme.typography.bodyMedium
            )
        },

        // Placeholder
        placeholder = {
            Text(
                text = "Enter your password",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
        },

        // Leading lock icon
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Password icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                modifier = Modifier.size(22.dp)
            )
        },

        visualTransformation =
            if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),

        keyboardActions = KeyboardActions(
            onDone = { onDoneAction?.invoke() }
        ),

        trailingIcon = {
            if (isFocused || password.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp) // larger hit area for better UX
                        .clickable(
                            interactionSource = visibilityInteraction,
                            indication = rippleIndication
                        ) {
                            onPasswordVisibilityToggle(!isPasswordVisible)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                        modifier = Modifier
                            .size(22.dp)
                            .alpha(1f - iconOverlayAlpha) // hover/press fade
                    )
                }
            }
        }
    )
}



/**
 * Email sign-in/up button with animated loading state
 * Single responsibility: render authentication button with loading indicator
 *
 * MODIFIED: Removed AnimatedContent inside buttons (performance fix)
 * + ADDED ultra-smooth scale + alpha animation (minimal)
 */
@Composable
fun AuthenticationButton(
    isLoading: Boolean,
    isSignUp: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    buttonScale: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val isPressed = interactionSource.collectIsPressedAsState().value
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val smoothScale by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = spring(
            dampingRatio = 1.0f,
            stiffness = 800f
        ),
        label = "smoothButtonScale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.22f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(120),
        label = "overlayAlpha"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(smoothScale)
            .semantics {
                contentDescription =
                    if (isSignUp) "Create account button" else "Sign in button"
            },
        enabled = isEnabled,
        interactionSource = interactionSource,
        border = BorderStroke(
            1.dp,
            if (isEnabled)
                MaterialTheme.colorScheme.primary
            else
                Color(0xFFDADCE0)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .alpha(
                    if (!isEnabled) 0.88f
                    else 1f - overlayAlpha
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
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
                    Text("Signing you in...", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Text(
                    text = if (isSignUp) "Sign up" else "Sign in",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Google sign-in button with animated loading state
 * Single responsibility: render Google authentication button
 *
 * MODIFIED: Removed AnimatedContent inside buttons (performance fix)
 * + ADDED ultra-smooth scale + alpha animation (minimal)
 */
@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    buttonScale: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val isPressed = interactionSource.collectIsPressedAsState().value
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val smoothScale by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = spring(
            dampingRatio = 1.0f,
            stiffness = 800f
        ),
        label = "smoothButtonScale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.22f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(120),
        label = "overlayAlphaGoogle"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(smoothScale)
            .semantics {
                contentDescription = "Continue with Google button"
            },
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF3C4043)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .alpha(1f - overlayAlpha),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
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
                    Text("Signing in with Google...", style = MaterialTheme.typography.labelLarge)
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
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Divider between auth methods
 * Single responsibility: render horizontal divider with "or"
 */
@Composable
fun AuthMethodDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/**
 * Toggle between sign-in and sign-up mode
 * Single responsibility: render auth mode toggle button
 *
 * ADDED: smooth scale + alpha (minimal)
 */
@Composable
fun AuthModeToggle(
    isSignUp: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean,
    buttonScale: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val isPressed = interactionSource.collectIsPressedAsState().value
    val isHovered = interactionSource.collectIsHoveredAsState().value

    val smoothScale by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = spring(
            dampingRatio = 1.0f,
            stiffness = 420f
        ),
        label = "smoothButtonScale"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.22f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(120),
        label = "toggleOverlayAlpha"
    )

    Row(
        modifier = modifier
            .height(32.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) "Already have an account?" else "Don't have an account?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = onToggle,
            modifier = Modifier
                .scale(smoothScale)
                .alpha(1f - overlayAlpha)
                .semantics {
                    contentDescription =
                        if (isSignUp) "Switch to sign in" else "Switch to sign up"
                },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            enabled = isEnabled
        ) {
            Text(
                text = if (isSignUp) "Sign in" else "Sign up",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Animated error message card
 * Single responsibility: display error messages with dismiss action
 * This is the ONLY place AnimatedVisibility is correct (state-based)
 */
@Composable
fun ErrorMessageCard(
    errorMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = errorMessage != null,
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
        errorMessage?.let { error ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable { onDismiss() }
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
