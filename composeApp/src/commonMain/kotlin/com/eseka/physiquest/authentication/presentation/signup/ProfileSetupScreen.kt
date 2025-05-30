package com.eseka.physiquest.authentication.presentation.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Person2
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.eseka.physiquest.authentication.presentation.components.PhotoActionButton
import com.eseka.physiquest.core.presentation.CameraAndGalleryPermissionHandler
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.choose_from_gallery
import physiquest.composeapp.generated.resources.crop_image
import physiquest.composeapp.generated.resources.display_name
import physiquest.composeapp.generated.resources.profile_photo
import physiquest.composeapp.generated.resources.save_profile
import physiquest.composeapp.generated.resources.setup_your_profile
import physiquest.composeapp.generated.resources.take_photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    state: SignUpState,
    onDisplayNameChanged: (String) -> Unit,
    onPhotoSelected: (String, String) -> Unit,
    onSaveProfileClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                        ),
                    )
                },
                dialogPadding = PaddingValues(0.dp)
            )
        }

        Text(
            text = stringResource(Res.string.setup_your_profile),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(state.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(Res.string.profile_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
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

        AnimatedVisibility(state.photoUriError != null) {
            Text(
                text = state.photoUriError!!.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = { onDisplayNameChanged(it) },
            label = { Text(stringResource(Res.string.display_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.displayNameError != null,
            supportingText = state.displayNameError?.let {
                { Text(it.asString()) }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Person2,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSaveProfileClicked()
                }
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSaveProfileClicked()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.displayName.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(stringResource(Res.string.save_profile))
        }
    }
}
// TODO: "Pressing done on the keyboard in the name text field launches camera"