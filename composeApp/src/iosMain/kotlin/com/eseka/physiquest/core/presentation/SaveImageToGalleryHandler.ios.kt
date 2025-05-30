package com.eseka.physiquest.core.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    val galleryManager = remember { ImageGalleryManagerImpl() }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                onSaveStarted()

                if (!galleryManager.hasWritePermission()) {
                    val permissionGranted = galleryManager.requestWritePermission()
                    if (!permissionGranted) {
                        onError("Photo library permission denied")
                        onSaveComplete()
                        return@launch
                    }
                }

                galleryManager.saveImageToGallery(imageUri)
                    .onSuccess {
                        onSaveComplete()
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to save image")
                        onSaveComplete()
                    }
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
}