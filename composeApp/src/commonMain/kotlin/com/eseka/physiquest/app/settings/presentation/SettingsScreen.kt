package com.eseka.physiquest.app.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.eseka.physiquest.app.settings.presentation.components.EditableField
import com.eseka.physiquest.app.settings.presentation.components.PhotoActionButton
import com.eseka.physiquest.core.presentation.CameraAndGalleryPermissionHandler
import com.eseka.physiquest.core.presentation.components.shimmerEffect
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.account_actions
import physiquest.composeapp.generated.resources.cancel
import physiquest.composeapp.generated.resources.choose_from_gallery
import physiquest.composeapp.generated.resources.crop_image
import physiquest.composeapp.generated.resources.delete_account
import physiquest.composeapp.generated.resources.delete_account_confirmation
import physiquest.composeapp.generated.resources.display_name
import physiquest.composeapp.generated.resources.profile_photo
import physiquest.composeapp.generated.resources.sign_out
import physiquest.composeapp.generated.resources.sign_out_confirmation
import physiquest.composeapp.generated.resources.take_photo
import physiquest.composeapp.generated.resources.update_profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onDisplayNameChanged: (TextFieldValue) -> Unit,
    onUpdateProfileClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    onPhotoSelected: (String, String) -> Unit,
    onScreenLeave: () -> Unit,
    onScreenReturn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Editable Name and Pfp State restoration shenanigans
    LaunchedEffect(Unit) {
        onScreenReturn()
    }
    DisposableEffect(Unit) {
        onDispose {
            onScreenLeave()
        }
    }

    val imageCropper = rememberImageCropper()
    var isCropping by rememberSaveable { mutableStateOf(false) }
    var checkAndLaunchCamera by rememberSaveable { mutableStateOf(false) }
    var checkAndLaunchGallery by rememberSaveable { mutableStateOf(false) }

    CameraAndGalleryPermissionHandler(
        imageCropper = imageCropper,
        checkAndLaunchCamera = checkAndLaunchCamera,
        checkAndLaunchGallery = checkAndLaunchGallery,
        changeCheckAndLaunchCamera = { checkAndLaunchCamera = it },
        changeCheckAndLaunchGallery = { checkAndLaunchGallery = it },
        changeIsCropping = { isCropping = it },
        onPhotoSelected = { uriString, extension ->
            onPhotoSelected(uriString, extension)
        },
        onPermissionDenied = { message ->
            // Handle permission denial
        }
    )

    // Screen starts here
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    var isEditingDisplayName by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val displayNameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditingDisplayName) {
        if (isEditingDisplayName) {
            displayNameFocusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val cropState = imageCropper.cropState
        if (isCropping && cropState != null) {
            ImageCropperDialog(
                state = cropState,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(Res.string.crop_image)) },
                        navigationIcon = {
                            IconButton(onClick = { cropState.done(accept = false) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { cropState.reset() }) {
                                Icon(Icons.Default.Restore, null)
                            }
                            IconButton(
                                onClick = { cropState.done(accept = true) },
                                enabled = !cropState.accepted
                            ) {
                                Icon(Icons.Default.Done, null)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                dialogPadding = PaddingValues(0.dp),
            )
        }
        // Profile Section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (state.photoUrl != null) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(state.photoUrl)
                                    .crossfade(true)
                                    .build()
                            )
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (painter.state.value is AsyncImagePainter.State.Loading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shimmerEffect()
                                    )
                                }
                                Image(
                                    painter = painter,
                                    contentDescription = stringResource(Res.string.profile_photo),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PhotoActionButton(
                        icon = Icons.Rounded.CameraAlt,
                        contentDescription = stringResource(Res.string.take_photo),
                        onClick = { checkAndLaunchCamera = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp)
                    )

                    PhotoActionButton(
                        icon = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = stringResource(Res.string.choose_from_gallery),
                        onClick = { checkAndLaunchGallery = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp)
                    )
                }

                AnimatedVisibility(state.photoUrlError != null) {
                    Text(
                        text = state.photoUrlError!!.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Display Name Input
                EditableField(
                    label = stringResource(Res.string.display_name),
                    value = state.displayName,
                    isError = state.displayNameError != null,
                    supportingText = state.displayNameError?.asString(),
                    onValueChange = { onDisplayNameChanged(it) },
                    isEditing = isEditingDisplayName,
                    onEditClick = { isEditingDisplayName = true },
                    onSaveClick = { isEditingDisplayName = false },
                    isLoading = state.isProfileUpdating,
                    focusRequester = displayNameFocusRequester,
                    focusManager = focusManager
                )
                //Email (Non editable)
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.Start),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Update Profile Button
        FilledTonalButton(
            onClick = {
                onUpdateProfileClicked()
                focusManager.clearFocus()
                isEditingDisplayName = false
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProfileUpdating
        ) {
            if (state.isProfileUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(stringResource(Res.string.update_profile))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.account_actions),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSigningOut) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(stringResource(Res.string.sign_out))
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = { showDeleteAccountDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            if (state.isDeletingAccount) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(stringResource(Res.string.delete_account))
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null
                )
            },
            title = { Text(stringResource(Res.string.sign_out)) },
            text = { Text(stringResource(Res.string.sign_out_confirmation)) },
            onDismissRequest = { showSignOutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSignOutClicked()
                        showSignOutDialog = false
                    }
                ) {
                    Text(stringResource(Res.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = null
                )
            },
            title = { Text(stringResource(Res.string.delete_account)) },
            text = {
                Text(stringResource(Res.string.delete_account_confirmation))
            },
            onDismissRequest = { showDeleteAccountDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccountClicked()
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(Res.string.delete_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}