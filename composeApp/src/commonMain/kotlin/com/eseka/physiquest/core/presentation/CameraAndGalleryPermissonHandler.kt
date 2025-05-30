package com.eseka.physiquest.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.attafitamim.krop.core.crop.ImageCropper

@Composable
expect fun CameraAndGalleryPermissionHandler(
    imageCropper: ImageCropper,
    checkAndLaunchCamera: Boolean,
    checkAndLaunchGallery: Boolean,
    changeCheckAndLaunchCamera: (Boolean) -> Unit,
    changeCheckAndLaunchGallery: (Boolean) -> Unit,
    changeIsCropping: (Boolean) -> Unit,
    onPhotoSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onPermissionDenied: (String) -> Unit = {}
)