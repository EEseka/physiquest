package com.eseka.physiquest.core.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.eseka.physiquest.core.data.services.ImageGalleryManagerImpl
import kotlinx.coroutines.launch
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.error_saving_image
import physiquest.composeapp.generated.resources.image_saved_successfully
import physiquest.composeapp.generated.resources.permission_required_to_save_image

@Composable
actual fun SaveImageToGalleryHandler(
    imageUri: String,
    saveIcon: ImageVector,
    modifier: Modifier,
    onSaveStarted: () -> Unit,
    onSaveComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val galleryManager = remember { ImageGalleryManagerImpl(context) }
    var savePermissionRequested by rememberSaveable { mutableStateOf(false) }

    val imageSavedSuccessfullyString =
        UiText.StringResourceId(Res.string.image_saved_successfully).asString()
    val errorSavingImageString = UiText.StringResourceId(Res.string.error_saving_image).asString()
    val permissionRequiredString =
        UiText.StringResourceId(Res.string.permission_required_to_save_image).asString()

    // Permission launcher for Android 9 and below
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        savePermissionRequested = false
        if (isGranted) {
            scope.launch {
                onSaveStarted()
                galleryManager.saveImageToGallery(imageUri)
                    .onSuccess {
                        Toast.makeText(
                            context,
                            imageSavedSuccessfullyString,
                            Toast.LENGTH_SHORT
                        ).show()
                        onSaveComplete()
                    }
                    .onFailure { e ->
                        Toast.makeText(context, errorSavingImageString, Toast.LENGTH_SHORT).show()
                        onError(e.message ?: "Unknown error")
                        onSaveComplete()
                    }
            }
        } else {
            Toast.makeText(
                context,
                permissionRequiredString,
                Toast.LENGTH_SHORT
            ).show()
            onError("Permission denied")
            onSaveComplete()
        }
    }

    suspend fun performSave() {
        onSaveStarted()
        galleryManager.saveImageToGallery(imageUri)
            .onSuccess {
                Toast.makeText(
                    context,
                    imageSavedSuccessfullyString,
                    Toast.LENGTH_SHORT
                ).show()
                onSaveComplete()
            }
            .onFailure { e ->
                Toast.makeText(context, errorSavingImageString, Toast.LENGTH_SHORT).show()
                onError(e.message ?: "Unknown error")
                onSaveComplete()
            }
    }

    IconButton(
        onClick = {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) -> {
                        scope.launch { performSave() }
                    }

                    else -> {
                        savePermissionRequested = true
                    }
                }
            } else {
                // Android 10 and above don't need permission
                scope.launch { performSave() }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = saveIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }

    // Permission Request Trigger
    LaunchedEffect(savePermissionRequested) {
        if (savePermissionRequested) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
