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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                // First, check if we already have permission. If not, request it.
                val hasPermission =
                    galleryManager.hasWritePermission() || galleryManager.requestWritePermission()

                if (hasPermission) {
                    galleryManager.saveImageToGallery(imageUri)
                        .onSuccess {
                            // Ensure UI updates are on the main thread
                            withContext(Dispatchers.Main) {
                                onSaveComplete()
                            }
                        }
                        .onFailure { e ->
                            withContext(Dispatchers.Main) {
                                onError(e.message ?: "Failed to save image")
                            }
                        }
                } else {
                    // Handle the case where permission is denied
                    withContext(Dispatchers.Main) {
                        onError("Photo library permission denied.")
                    }
                }
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
}