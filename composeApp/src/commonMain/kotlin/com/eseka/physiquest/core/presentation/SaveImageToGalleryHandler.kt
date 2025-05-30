package com.eseka.physiquest.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun SaveImageToGalleryHandler(
    imageUri: String,
    saveIcon: ImageVector,
    modifier: Modifier = Modifier,
    onSaveStarted: () -> Unit = {},
    onSaveComplete: () -> Unit = {},
    onError: (String) -> Unit = {}
)