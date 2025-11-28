package com.eseka.physiquest.authentication.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diamondedge.logging.logging
import com.eseka.physiquest.authentication.presentation.AuthEvent
import com.eseka.physiquest.authentication.presentation.AuthEventBus
import com.eseka.physiquest.authentication.presentation.signin.ResetPasswordEmailSentScreen
import com.eseka.physiquest.authentication.presentation.signin.SignInEvents
import com.eseka.physiquest.authentication.presentation.signin.SignInScreen
import com.eseka.physiquest.authentication.presentation.signin.SignInViewModel
import com.eseka.physiquest.authentication.presentation.signup.EmailVerificationScreen
import com.eseka.physiquest.authentication.presentation.signup.ProfileSetupScreen
import com.eseka.physiquest.authentication.presentation.signup.SignUpEvents
import com.eseka.physiquest.authentication.presentation.signup.SignUpScreen
import com.eseka.physiquest.authentication.presentation.signup.SignUpViewModel
import com.eseka.physiquest.authentication.presentation.welcome.ErrorScreen
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeScreen
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeUiState
import com.eseka.physiquest.authentication.presentation.welcome.WelcomeViewModel
import com.eseka.physiquest.core.navigation.util.navigateAndClear
import com.eseka.physiquest.core.presentation.UiText
import com.eseka.physiquest.core.presentation.utils.ObserveAsEvents
import com.eseka.physiquest.core.presentation.utils.toUiText
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.email_verified_successfully
import physiquest.composeapp.generated.resources.profile_setup_complete

private const val TAG = "AuthNavigation"

@Composable
fun AuthNavigation(
    welcomeState: WelcomeUiState,
    welcomeViewModel: WelcomeViewModel,
    navController: NavHostController = rememberNavController(),
    signUpViewModel: SignUpViewModel = koinViewModel(),
    signInViewModel: SignInViewModel = koinViewModel(),
    authEventBus: AuthEventBus = koinInject(),
    onNavigateToHome: () -> Unit
) {
    val log = logging()

    val signUpState by signUpViewModel.state.collectAsStateWithLifecycle()
    val signInState by signInViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var authErrorToShow by remember { mutableStateOf<UiText?>(null) }

    val emailVerifiedSuccessfullyString =
        UiText.StringResourceId(Res.string.email_verified_successfully).asString()
    val profileSetupCompleteString =
        UiText.StringResourceId(Res.string.profile_setup_complete).asString()

    authErrorToShow?.let { uiText ->
        val errorMessage = uiText.asString()
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            authErrorToShow = null
        }
    }

    ObserveAsEvents(events = authEventBus.events) { event ->
        when (event) {
            is AuthEvent.Error -> {
                authErrorToShow = event.error.toUiText()
            }

            AuthEvent.SignUpSuccess -> {
                navController.navigate(
                    AuthNavDestinations.EmailVerification.createRoute(AuthNavDestinations.SignUp.route)
                )
            }

            AuthEvent.SignInSuccess -> {
                val isEmailVerified = signInState.isEmailVerified
                if (isEmailVerified) {
                    onNavigateToHome()
                } else {
                    signInViewModel.onEvent(SignInEvents.OnResendVerificationEmailClicked)
                    navController.navigate(
                        AuthNavDestinations.EmailVerification.createRoute(AuthNavDestinations.SignIn.route)
                    )
                }
            }

            AuthEvent.EmailVerified -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = emailVerifiedSuccessfullyString,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            AuthEvent.ProfileSetupComplete -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = profileSetupCompleteString,
                        duration = SnackbarDuration.Short
                    )
                }
                onNavigateToHome()
            }
        }
    }

    val startDestination = rememberSaveable {
        when (welcomeState) {
            WelcomeUiState.Onboarding -> AuthNavDestinations.Welcome.route
            WelcomeUiState.NotAuthenticated -> AuthNavDestinations.SignIn.route
            WelcomeUiState.Initial -> {
                // This state should be rare, so we add some logging just in case
                log.w(tag = TAG, msg = { "Unexpected Initial state in Splash screen" })
                AuthNavDestinations.Welcome.route
            }

            WelcomeUiState.Error -> AuthNavDestinations.Error.route
            WelcomeUiState.Authenticated -> {
                // Can Never be reached, but we need to handle all cases
                AuthNavDestinations.SignIn.route
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(AuthNavDestinations.Welcome.route) {
                WelcomeScreen(
                    onSignInClicked = {
                        welcomeViewModel.completeOnboarding()
                        navController.navigate(AuthNavDestinations.SignIn.route)
                    },
                    onSignUpClicked = {
                        welcomeViewModel.completeOnboarding()
                        navController.navigate(AuthNavDestinations.SignUp.route)
                    }
                )
            }

            composable(AuthNavDestinations.SignUp.route) {
                SignUpScreen(
                    state = signUpState,
                    onEmailValueChange = {
                        signUpViewModel.onEvent(SignUpEvents.OnEmailChanged(it))
                    },
                    onPasswordValueChange = {
                        signUpViewModel.onEvent(SignUpEvents.OnPasswordChanged(it))
                    },
                    onRepeatedPasswordValueChange = {
                        signUpViewModel.onEvent(SignUpEvents.OnRepeatedPasswordChanged(it))
                    },
                    onSignUpClicked = {
                        signUpViewModel.onEvent(SignUpEvents.OnSignUpClicked)
                    },
                    onNavigateToSignIn = {
                        navController.navigate(AuthNavDestinations.SignIn.route) {
                            popUpTo(AuthNavDestinations.SignUp.route) { inclusive = true }
                        }
                    },
                    onContinueWithGoogleClicked = { idToken, accessToken ->
                        signUpViewModel.onEvent(
                            SignUpEvents.OnSignInWithGoogleClicked(
                                idToken,
                                accessToken
                            )
                        )
                    }
                )
            }

            composable(AuthNavDestinations.EmailVerification.route) { backStackEntry ->
                val screen = backStackEntry.arguments?.getString("screen")!!

                if (screen == AuthNavDestinations.SignUp.route) {
                    EmailVerificationScreen(
                        state = signUpState,
                        clearEmailVerificationError = {
                            signUpViewModel.onEvent(SignUpEvents.ClearEmailVerificationError)
                        },
                        onCheckVerificationClicked = {
                            signUpViewModel.onEvent(SignUpEvents.OnEmailVerifiedClicked)
                        },
                        onVerificationComplete = {
                            navController.navigate(AuthNavDestinations.ProfileSetup.route) {
                                popUpTo(AuthNavDestinations.EmailVerification.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                } else if (screen == AuthNavDestinations.SignIn.route) {
                    com.eseka.physiquest.authentication.presentation.signin.EmailVerificationScreen(
                        state = signInState,
                        clearEmailVerificationError = {
                            signInViewModel.onEvent(SignInEvents.ClearEmailVerificationError)
                        },
                        onCheckVerificationClicked = {
                            signInViewModel.onEvent(SignInEvents.OnEmailVerifiedClicked)
                        },
                        onVerificationComplete = {
                            onNavigateToHome()
                        }
                    )
                }
            }

            composable(AuthNavDestinations.Error.route) {
                ErrorScreen(
                    onRetryClicked = {
                        welcomeViewModel.resetAppState()
                        navigateAndClear(navController, AuthNavDestinations.Welcome.route)
                    }
                )
            }

            composable(AuthNavDestinations.SignIn.route) {
                SignInScreen(
                    state = signInState,
                    onEmailValueChange = {
                        signInViewModel.onEvent(SignInEvents.OnEmailChanged(it))
                    },
                    onPasswordValueChange = {
                        signInViewModel.onEvent(SignInEvents.OnPasswordChanged(it))
                    },
                    onSignInClicked = {
                        signInViewModel.onEvent(SignInEvents.OnSignInClicked)
                    },
                    onNavigateToSignUp = {
                        navController.navigate(AuthNavDestinations.SignUp.route) {
                            popUpTo(AuthNavDestinations.SignIn.route) { inclusive = true }
                        }
                    },
                    onForgotPasswordEmailValueChange = {
                        signInViewModel.onEvent(SignInEvents.OnForgotPasswordEmailChanged(it))
                    },
                    onForgotPasswordClicked = {
                        signInViewModel.onEvent(SignInEvents.OnSendPasswordResetClicked)
                    },
                    onDismissForgotPasswordDialog = {
                        signInViewModel.onEvent(SignInEvents.ClearForgotPasswordError)
                    },
                    onNavigateToResetPasswordEmail = {
                        navController.navigate(AuthNavDestinations.ResetPasswordEmail.route)
                        signInViewModel.onEvent(SignInEvents.ClearForgotPasswordEmailSent)
                    },
                    onContinueWithGoogleClicked = { idToken, accessToken ->
                        signInViewModel.onEvent(
                            SignInEvents.OnSignInWithGoogleClicked(
                                idToken,
                                accessToken
                            )
                        )
                    }
                )
            }

            composable(AuthNavDestinations.ResetPasswordEmail.route) {
                ResetPasswordEmailSentScreen(
                    email = signInState.forgotPasswordEmail,
                    onBackToSignIn = {
                        navController.navigate(AuthNavDestinations.SignIn.route) {
                            popUpTo(AuthNavDestinations.ResetPasswordEmail.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(AuthNavDestinations.ProfileSetup.route) {
                ProfileSetupScreen(
                    state = signUpState,
                    onDisplayNameChanged = {
                        signUpViewModel.onEvent(SignUpEvents.OnDisplayNameChanged(it))
                    },
                    onPhotoSelected = { uri, extension ->
                        signUpViewModel.onEvent(SignUpEvents.OnPhotoSelected(uri, extension))
                    },
                    onSaveProfileClicked = {
                        signUpViewModel.onEvent(SignUpEvents.OnSaveProfileClicked)
                    }
                )
            }
        }
    }
}