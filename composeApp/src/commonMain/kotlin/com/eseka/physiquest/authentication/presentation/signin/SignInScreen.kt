package com.eseka.physiquest.authentication.presentation.signin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.authentication.presentation.components.EmailInputField
import com.eseka.physiquest.authentication.presentation.components.GoogleSignInButton
import com.eseka.physiquest.authentication.presentation.components.PasswordInputField
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.cancel
import physiquest.composeapp.generated.resources.dont_have_account
import physiquest.composeapp.generated.resources.enter_email_for_reset
import physiquest.composeapp.generated.resources.forgot_password
import physiquest.composeapp.generated.resources.or
import physiquest.composeapp.generated.resources.password
import physiquest.composeapp.generated.resources.send_reset_link
import physiquest.composeapp.generated.resources.sign_in
import physiquest.composeapp.generated.resources.sign_in_to_continue
import physiquest.composeapp.generated.resources.sign_up
import physiquest.composeapp.generated.resources.welcome_back

@Composable
fun SignInScreen(
    state: SignInState,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onSignInClicked: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPasswordEmailValueChange: (String) -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onDismissForgotPasswordDialog: () -> Unit,
    onNavigateToResetPasswordEmail: () -> Unit,
    onContinueWithGoogleClicked: (String?, String?) -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                onDismissForgotPasswordDialog()
            },
            title = {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(Res.string.enter_email_for_reset),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EmailInputField(
                        value = state.forgotPasswordEmail,
                        onValueChange = { onForgotPasswordEmailValueChange(it) },
                        isError = state.forgotPasswordEmailError != null,
                        emailError = state.forgotPasswordEmailError?.asString(),
                        emailFocusRequester = null,
                        imeAction = ImeAction.Done,
                        onKeyboardDoneClicked = onForgotPasswordClicked,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onForgotPasswordClicked,
                    enabled = !state.isLoading && state.forgotPasswordEmail.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.send_reset_link),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        onDismissForgotPasswordDialog()
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    // Handle successful reset email sent
    LaunchedEffect(state.forgotPasswordEmailSent) {
        if (state.forgotPasswordEmailSent) {
            showForgotPasswordDialog = false
            onNavigateToResetPasswordEmail()
        }
    }

    // Automatically focus on the first error field
    LaunchedEffect(state.emailError, state.passwordError) {
        when {
            state.emailError != null -> emailFocusRequester.requestFocus()
            state.passwordError != null -> passwordFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.sign_in_to_continue),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        EmailInputField(
            value = state.email,
            onValueChange = { onEmailValueChange(it) },
            isError = state.emailError != null,
            emailError = state.emailError?.asString(),
            emailFocusRequester = emailFocusRequester,
            imeAction = ImeAction.Next,
            onKeyboardNextClicked = { passwordFocusRequester.requestFocus() },
        )
        PasswordInputField(
            value = state.password,
            onValueChange = { onPasswordValueChange(it) },
            label = stringResource(Res.string.password),
            passwordNotEmpty = state.password.isNotBlank(),
            onVisibilityIconClicked = { passwordVisible = !passwordVisible },
            passwordVisible = passwordVisible,
            isError = state.passwordError != null,
            passwordError = state.passwordError?.asString(),
            passwordFocusRequester = passwordFocusRequester,
            imeAction = ImeAction.Done,
            onKeyboardDoneClicked = {
                onSignInClicked()
                focusManager.clearFocus()
            }
        )
        Text(
            text = stringResource(Res.string.forgot_password),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { showForgotPasswordDialog = true },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSignInClicked()
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = stringResource(Res.string.sign_in),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.dont_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.sign_up),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onNavigateToSignUp() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = stringResource(Res.string.or),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleButtonUiContainer(onGoogleSignInResult = { googleUser ->
            onContinueWithGoogleClicked(googleUser?.idToken, googleUser?.accessToken)
        }) {
            GoogleSignInButton(
                onClick = { this.onClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
        }
    }
}