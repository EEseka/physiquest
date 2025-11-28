package com.eseka.physiquest.core.presentation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.crop
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.data.services.CameraGalleryManagerImpl
import com.eseka.physiquest.core.data.utils.ImageUtilsImpl
import kotlinx.coroutines.launch
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.camera_permission_denied
import physiquest.composeapp.generated.resources.gallery_permission_denied

@Composable
actual fun CameraAndGalleryPermissionHandler(
    imageCropper: ImageCropper,
    checkAndLaunchCamera: Boolean,
    checkAndLaunchGallery: Boolean,
    changeCheckAndLaunchCamera: (Boolean) -> Unit,
    changeCheckAndLaunchGallery: (Boolean) -> Unit,
    changeIsCropping: (Boolean) -> Unit,
    onPhotoSelected: (String, String) -> Unit,
    modifier: Modifier,
    onPermissionDenied: (String) -> Unit
) {
    val log = logging()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraGalleryManager = remember { CameraGalleryManagerImpl(context) }
    val imageUtils = remember { ImageUtilsImpl(context) }

    var tempPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var galleryPermissionRequested by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionDeniedString =
        UiText.StringResourceId(Res.string.camera_permission_denied).asString()
    val galleryPermissionDeniedString =
        UiText.StringResourceId(Res.string.gallery_permission_denied).asString()

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        cameraPermissionRequested = false

        if (success && tempPhotoUri != null) {
            scope.launch {
                // First show un-cropped version
                val mimeType = cameraGalleryManager.getMimeTypeFromUri(tempPhotoUri!!)
                val extension = mimeType?.let {
                    cameraGalleryManager.getExtensionFromMimeType(it)
                }

                if (extension != null) {
                    onPhotoSelected(tempPhotoUri!!, extension)
                }

                // Then attempt to crop
                changeIsCropping(true)
                try {
                    val uri = tempPhotoUri!!.toUri()
                    val result = imageCropper.crop(uri, context)
                    when (result) {
                        CropResult.Cancelled -> {
                            changeIsCropping(false)
                        }

                        is CropError -> {
                            changeIsCropping(false)
                            log.e(
                                tag = "CameraGalleryHandler",
                                msg = { "Error cropping image: $result" }
                            )
                        }

                        is CropResult.Success -> {
                            val croppedUri = imageUtils.imageBitmapToUri(
                                result.bitmap,
                                tempPhotoUri!!
                            )
                            if (croppedUri != null) {
                                tempPhotoUri = croppedUri
                                // Update with cropped version
                                if (extension != null) {
                                    onPhotoSelected(croppedUri, extension)
                                }
                            }
                            changeIsCropping(false)
                        }
                    }
                } catch (e: Exception) {
                    changeIsCropping(false)
                    log.e(
                        tag = "CameraGalleryHandler",
                        msg = { "Error during cropping process: ${e.message}" }
                    )
                }
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { contentUri ->
        galleryPermissionRequested = false
        contentUri?.let { uri ->
            scope.launch {
                val mimeType = cameraGalleryManager.getMimeTypeFromUri(uri.toString())
                val extension = mimeType?.let {
                    cameraGalleryManager.getExtensionFromMimeType(it)
                }
                if (extension != null) {
                    onPhotoSelected(uri.toString(), extension)
                }
            }
        }
    }

    suspend fun checkAndLaunchCamera() {
        if (cameraGalleryManager.hasCameraPermission()) {
            val uri = cameraGalleryManager.createTempPhotoUri()
            if (uri != null) {
                tempPhotoUri = uri
                cameraLauncher.launch(uri.toUri())
            }
        } else {
            cameraPermissionRequested = true
        }
    }

    suspend fun checkAndLaunchGallery() {
        if (cameraGalleryManager.hasGalleryPermission()) {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            galleryPermissionRequested = true
        }
    }

    // Permission handler for camera and gallery
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle camera permission result
        permissions[Manifest.permission.CAMERA]?.let { granted ->
            if (granted) {
                if (cameraPermissionRequested) {
                    scope.launch {
                        val uri = cameraGalleryManager.createTempPhotoUri()
                        if (uri != null) {
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri.toUri())
                        }
                    }
                }
            } else if (cameraPermissionRequested) {
                cameraPermissionRequested = false
                Toast.makeText(
                    context,
                    cameraPermissionDeniedString,
                    Toast.LENGTH_SHORT
                ).show()
                onPermissionDenied("Camera permission denied")
            }
        }

        // Handle gallery permission result
        val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES]
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
        }

        galleryPermission?.let { granted ->
            if (granted) {
                if (galleryPermissionRequested) {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            } else if (galleryPermissionRequested) {
                galleryPermissionRequested = false
                Toast.makeText(
                    context,
                    galleryPermissionDeniedString,
                    Toast.LENGTH_SHORT
                ).show()
                onPermissionDenied("Gallery permission denied")
            }
        }
    }

    // Permission Request Trigger
    LaunchedEffect(cameraPermissionRequested, galleryPermissionRequested) {
        if (cameraPermissionRequested || galleryPermissionRequested) {
            val permissions = mutableListOf<String>()

            if (cameraPermissionRequested) {
                permissions.add(Manifest.permission.CAMERA)
            }

            if (galleryPermissionRequested) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            if (permissions.isNotEmpty()) {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    LaunchedEffect(checkAndLaunchCamera) {
        if (checkAndLaunchCamera) {
            checkAndLaunchCamera()
            changeCheckAndLaunchCamera(false)
        }
    }

    LaunchedEffect(checkAndLaunchGallery) {
        if (checkAndLaunchGallery) {
            checkAndLaunchGallery()
            changeCheckAndLaunchGallery(false)
        }
    }
}