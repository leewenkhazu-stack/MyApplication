package com.fsl.myapplication.ui.screens.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
            .height(72.dp),
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
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(500, delayMillis = 100, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(500, delayMillis = 100, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
        ) { it / 4 },
        label = "emailFieldAnimation"
    ) {
        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email", fontSize = 16.sp) },
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Email input field"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            enabled = isEnabled
        )
    }
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
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password", fontSize = 16.sp) },
            modifier = modifier
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
                onDone = { onDoneAction?.invoke() }
            ),
            trailingIcon = {
                IconButton(
                    onClick = { onPasswordVisibilityToggle(!isPasswordVisible) }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            enabled = isEnabled
        )
    }
}

/**
 * Email sign-in/up button with animated loading state
 * Single responsibility: render authentication button with loading indicator
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
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .semantics {
                contentDescription =
                    if (isSignUp) "Create account button" else "Sign in button"
            },
        enabled = isEnabled,
        interactionSource = interactionSource,
        border = BorderStroke(
            1.dp,
            if (isEnabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isLoading,
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
                        text = if (isSignUp) "Sign up" else "Sign in",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Google sign-in button with animated loading state
 * Single responsibility: render Google authentication button
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
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
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
        border = BorderStroke(1.dp, Color(0xFFDADCE0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isLoading,
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
}

/**
 * Animated divider with centered "or" text
 * Single responsibility: render separator between authentication methods
 */
@Composable
fun AuthMethodDivider(modifier: Modifier = Modifier) {
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
            modifier = modifier.fillMaxWidth(),
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
}

/**
 * Toggle between sign-in and sign-up mode
 * Single responsibility: render auth mode toggle button
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
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(700, delayMillis = 400, easing = FastOutSlowInEasing)
        ),
        label = "toggleButtonAnimation"
    ) {
        Row(
            modifier = modifier
                .height(32.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onToggle,
                modifier = Modifier
                    .scale(buttonScale)
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Animated error message card
 * Single responsibility: display error messages with dismiss action
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
                    .height(72.dp),
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
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

