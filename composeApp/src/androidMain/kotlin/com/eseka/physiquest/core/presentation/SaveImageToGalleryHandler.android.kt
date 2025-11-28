package com.eseka.physiquest.core.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
    var requestPermissionTrigger by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, now perform the save
            scope.launch {
                onSaveStarted()
                galleryManager.saveImageToGallery(imageUri)
                    .onSuccess { onSaveComplete() }
                    .onFailure { e -> onError(e.message ?: "Unknown error saving image") }
            }
        } else {
            onError("Permission denied to save image.")
        }
    }

    IconButton(
        onClick = {
            val hasPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                scope.launch {
                    onSaveStarted()
                    galleryManager.saveImageToGallery(imageUri)
                        .onSuccess { onSaveComplete() }
                        .onFailure { e -> onError(e.message ?: "Unknown error saving image") }
                }
            } else {
                // Trigger the permission request
                requestPermissionTrigger = true
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = saveIcon,
            contentDescription = "Save Image", // Added for accessibility
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }

    // A side-effect to launch the permission request when triggered
    if (requestPermissionTrigger) {
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissionTrigger = false // Reset trigger
        }
    }
}