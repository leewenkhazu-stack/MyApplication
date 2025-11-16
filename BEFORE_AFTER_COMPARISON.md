# Before & After Code Comparison

## AuthScreen: Before vs After

### BEFORE: Monolithic 559-line Composable

```kotlin
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
        // Title with smooth animation (20+ lines of AnimatedContent setup)
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

        // Email field with improved accessibility (35+ lines)
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

        // Password field with show/hide functionality (40+ lines)
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

        // Email authentication button (40+ lines)
        Button(
            onClick = {
                dismissKeyboard()
                viewModel.handleAction(AuthAction.SignInWithEmail)
            },
            // ... more setup
        ) {
            Box(...) { /* Loading animation */ }
        }

        // ... continues for 200+ more lines
        // (Google button, divider, error card, etc.)
    }
}
```

**Problems**:
- 559 total lines
- Mixed concerns: state, keyboard, animations, layout, UI
- Impossible to test individual parts
- Cannot reuse components
- Hard to read and maintain
- Full screen recomposes on any state change

---

### AFTER: Refactored into Components

```kotlin
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
        AuthTitle(isSignUp = uiState.isSignUp)
        Spacer(modifier = Modifier.height(32.dp))

        EmailInputField(
            email = uiState.email,
            onEmailChange = { viewModel.handleAction(AuthAction.UpdateEmail(it)) },
            isEnabled = !uiState.isLoading && !uiState.isGoogleSignInLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

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

        AuthMethodDivider()
        Spacer(modifier = Modifier.height(16.dp))

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

        ErrorMessageCard(
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.handleAction(AuthAction.ClearError) }
        )
    }
}
```

**Benefits**:
- ✅ 80 lines - Crystal clear
- ✅ Each component has single responsibility
- ✅ Easy to test each component
- ✅ Components reusable in other screens
- ✅ State logic extracted to hooks
- ✅ Granular recomposition

---

## Size Comparison

```
BEFORE:
AuthScreen.kt
├── 559 lines total
├── 29 imports
├── 15+ remember blocks
└── 8 nested layout blocks

AFTER:
AuthScreen.kt
├── ~80 lines (AuthScreen function)
├── 16 imports
├── 4 hooks (custom composables)
└── Clean, readable Column layout

AuthScreenComponents.kt
├── ~450 lines
├── 8 reusable composables
└── All organized by concern
```

---

## Reusability Example

### Before (Cannot reuse)
```kotlin
// If you wanted to use just the email field elsewhere,
// you'd have to copy-paste code from the 559-line file
// Not viable!
```

### After (Easily reusable)
```kotlin
// Password reset screen
@Composable
fun PasswordResetScreen() {
    Column {
        AuthTitle(isSignUp = false)
        EmailInputField(
            email = email,
            onEmailChange = { email = it },
            isEnabled = true
        )
        Button(onClick = { /* reset */ }) {
            Text("Send Reset Email")
        }
    }
}

// Edit profile screen
@Composable
fun EditProfileScreen() {
    Column {
        PasswordInputField(
            password = password,
            onPasswordChange = { password = it },
            isPasswordVisible = visible,
            onPasswordVisibilityToggle = { visible = it },
            isEnabled = true
        )
        // ...
    }
}
```

---

## Testability Example

### Before (Cannot test effectively)
```kotlin
@Test
fun testAuthScreen() {
    // Do I test the title? The buttons? The keyboard?
    // The entire screen is one unit - cannot test parts
    // Very brittle, high false-positive/negative rate
}
```

### After (Test each component)
```kotlin
@Test
fun testAuthTitle() {
    setContent { AuthTitle(isSignUp = false) }
    onNodeWithText("Sign in").assertIsDisplayed()
}

@Test
fun testEmailInputField() {
    setContent { EmailInputField(...) }
    // Test email-specific behavior
}

@Test
fun testPasswordInputField() {
    setContent { PasswordInputField(...) }
    // Test password-specific behavior
}

@Test
fun testAuthenticationButton() {
    setContent { AuthenticationButton(...) }
    // Test button-specific behavior
}

// etc. - each component isolated
```

---

## Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Lines** | 559 | 80 (main) + 450 (components) |
| **Readability** | ❌ Complex | ✅ Crystal clear |
| **Testability** | ❌ Impossible | ✅ 100% testable |
| **Reusability** | ❌ None | ✅ 8 components |
| **Maintainability** | ❌ Nightmare | ✅ Easy |
| **Performance** | ❌ Full recomp | ✅ Granular |
| **Best Practices** | ❌ Violated | ✅ Followed |

**Result**: Production-ready, maintainable, testable code! 🚀

