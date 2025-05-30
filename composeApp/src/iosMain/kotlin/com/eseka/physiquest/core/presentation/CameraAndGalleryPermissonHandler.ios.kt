package com.eseka.physiquest.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.crop
import com.eseka.physiquest.core.data.services.CameraGalleryManagerImpl
import com.eseka.physiquest.core.data.utils.ImageUtilsImpl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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
    val scope = rememberCoroutineScope()
    val cameraGalleryManager = remember { CameraGalleryManagerImpl() }
    val imageUtils = remember { ImageUtilsImpl() }

    var isPickerFlowActive by rememberSaveable { mutableStateOf(false) }

    fun saveImageDataToTemp(imageData: NSData, extension: String): String? {
        return try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String

            if (documentsPath != null) {
                val fileName = "temp_photo_${NSDate().timeIntervalSince1970.toLong()}.$extension"
                val filePath = "$documentsPath/$fileName"

                val success = imageData.writeToFile(filePath, atomically = true)
                if (success) {
                    Logger.d(
                        tag = "CameraGalleryHandler",
                        message = { "Saved image to: $filePath" })
                    filePath
                } else {
                    Logger.e(
                        tag = "CameraGalleryHandler",
                        message = { "Failed to write file to: $filePath" })
                    null
                }
            } else {
                Logger.e(tag = "CameraGalleryHandler", message = { "Documents path is null" })
                null
            }
        } catch (e: Exception) {
            Logger.e(
                tag = "CameraGalleryHandler",
                message = { "Error saving image data: ${e.message}" })
            null
        }
    }

    suspend fun cropImage(filePath: String, extension: String) {
        try {
            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "Starting crop process for: $filePath" })

            changeIsCropping(true)

            // Try different approaches to create a valid NSURL
            val nsUrl = NSURL.fileURLWithPath(filePath)

            Logger.d(
                tag = "CameraGalleryHandler",
                message = {
                    "NSURL created - Path: ${nsUrl.path}, " +
                            "Scheme: ${nsUrl.scheme}, " +
                            "AbsolutePath: ${nsUrl.absoluteString}, " +
                            "IsFileURL: ${nsUrl.isFileURL()}"
                })

            // Verify file exists before attempting to crop
            val fileExists =
                platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(filePath)
            if (!fileExists) {
                Logger.e(
                    tag = "CameraGalleryHandler",
                    message = { "File does not exist at path: $filePath" })
                changeIsCropping(false)
                return
            }

            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "File exists, attempting crop with NSURL" })

            val result = imageCropper.crop(nsUrl)

            Logger.i(
                tag = "CameraGalleryHandler",
                message = { "Crop result: $result" })

            when (result) {
                is CropResult.Success -> {
                    Logger.d(
                        tag = "CameraGalleryHandler",
                        message = { "Cropping successful" })

                    val croppedUri = withContext(Dispatchers.Default) {
                        imageUtils.imageBitmapToUri(result.bitmap, filePath)
                    }

                    if (!scope.isActive) return

                    if (croppedUri != null) {
                        Logger.d(
                            tag = "CameraGalleryHandler",
                            message = { "Cropped image saved to: $croppedUri" })
                        onPhotoSelected(croppedUri, extension)
                    } else {
                        Logger.e(
                            tag = "CameraGalleryHandler",
                            message = { "Failed to save cropped image" })
                    }
                }

                CropResult.Cancelled -> {
                    Logger.i(
                        tag = "CameraGalleryHandler",
                        message = { "Cropping was cancelled by user" })
                    // Keep original image
                }

                is CropError -> {
                    Logger.e(
                        tag = "CameraGalleryHandler",
                        message = { "Error cropping image: $result" })
                    // Keep original image
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            Logger.w(
                tag = "CameraGalleryHandler",
                message = { "Cropping coroutine was cancelled" },
                throwable = e
            )
        } catch (e: Exception) {
            Logger.e(
                tag = "CameraGalleryHandler",
                message = { "Exception during cropping process: ${e.message}" },
                throwable = e
            )
        } finally {
            if (scope.isActive) {
                changeIsCropping(false)
            }
        }
    }

    val imagePickerDelegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image =
                    didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                if (image == null) {
                    Logger.e(tag = "CameraGalleryHandler", message = { "No image selected" })
                    picker.dismissViewControllerAnimated(true, null)
                    isPickerFlowActive = false
                    return
                }

                val isFromCamera =
                    picker.sourceType == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

                if (isFromCamera) {
                    scope.launch {
                        try {
                            val imageData = withContext(Dispatchers.Default) {
                                UIImageJPEGRepresentation(image, 0.9)
                            }

                            if (imageData == null) {
                                Logger.e(
                                    tag = "CameraGalleryHandler",
                                    message = { "Failed to get image data" })
                                picker.dismissViewControllerAnimated(true, null)
                                isPickerFlowActive = false
                                return@launch
                            }

                            val tempFilePath = withContext(Dispatchers.Default) {
                                saveImageDataToTemp(imageData, "jpg")
                            }

                            if (tempFilePath == null) {
                                Logger.e(
                                    tag = "CameraGalleryHandler",
                                    message = { "Failed to save image to temporary file" })
                                picker.dismissViewControllerAnimated(true, null)
                                isPickerFlowActive = false
                                return@launch
                            }

                            val mimeType = cameraGalleryManager.getMimeTypeFromUri(tempFilePath)
                            val extension = mimeType?.let {
                                cameraGalleryManager.getExtensionFromMimeType(it)
                            } ?: "jpg"

                            // First show the original image
                            if (scope.isActive) {
                                onPhotoSelected(tempFilePath, extension)
                            }

                            // Dismiss picker first
                            picker.dismissViewControllerAnimated(true) {
                                if (!scope.isActive) {
                                    isPickerFlowActive = false
                                    return@dismissViewControllerAnimated
                                }

                                // Then start cropping process for camera images only
                                scope.launch(Dispatchers.Main) {
                                    cropImage(tempFilePath, extension)
                                }
                            }

                        } catch (e: kotlinx.coroutines.CancellationException) {
                            Logger.w(
                                tag = "CameraGalleryHandler",
                                message = { "Camera image processing coroutine cancelled" },
                                throwable = e
                            )
                        } catch (e: Exception) {
                            Logger.e(
                                tag = "CameraGalleryHandler",
                                message = { "Error in camera image processing: ${e.message}" },
                                throwable = e
                            )
                            if (scope.isActive) {
                                picker.dismissViewControllerAnimated(true, null)
                            }
                        } finally {
                            isPickerFlowActive = false
                            Logger.d(
                                tag = "CameraGalleryHandler",
                                message = { "Camera image processing flow finished" })
                        }
                    }
                } else { // Gallery selection - no cropping
                    picker.dismissViewControllerAnimated(true, null)
                    scope.launch(Dispatchers.Default) {
                        try {
                            val imageData = UIImageJPEGRepresentation(image, 0.9)
                            if (imageData == null) {
                                Logger.e(
                                    tag = "CameraGalleryHandler",
                                    message = { "Failed to get image data for gallery image" })
                                isPickerFlowActive = false
                                return@launch
                            }

                            val galleryTempFilePath = saveImageDataToTemp(imageData, "jpg")
                            if (galleryTempFilePath == null) {
                                Logger.e(
                                    tag = "CameraGalleryHandler",
                                    message = { "Failed to save gallery image to temporary file" })
                                isPickerFlowActive = false
                                return@launch
                            }

                            withContext(Dispatchers.Main) {
                                if (!scope.isActive) return@withContext
                                onPhotoSelected(galleryTempFilePath, "jpg")
                            }
                        } catch (e: Exception) {
                            Logger.e(
                                tag = "CameraGalleryHandler",
                                message = { "Error processing gallery image: ${e.message}" },
                                throwable = e
                            )
                        } finally {
                            isPickerFlowActive = false
                            Logger.d(
                                tag = "CameraGalleryHandler",
                                message = { "Gallery processing flow finished" })
                        }
                    }
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
                isPickerFlowActive = false
                Logger.d(tag = "CameraGalleryHandler", message = { "Image picker cancelled" })
            }
        }
    }

    fun presentImagePicker(sourceType: UIImagePickerControllerSourceType) {
        if (isPickerFlowActive) {
            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "Image picker flow already active, ignoring request" })
            return
        }
        isPickerFlowActive = true
        Logger.d(
            tag = "CameraGalleryHandler",
            message = { "Starting image picker flow" })

        val picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = imagePickerDelegate
        picker.allowsEditing = false

        if (sourceType == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera) {
            picker.showsCameraControls = true
        }

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController != null) {
            rootViewController.presentViewController(picker, animated = true, completion = null)
        } else {
            Logger.e(tag = "CameraGalleryHandler", message = { "Root view controller is null" })
            isPickerFlowActive = false
        }
    }

    suspend fun checkAndLaunch(isCamera: Boolean) {
        if (isPickerFlowActive) {
            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "Already processing image, ignoring ${if (isCamera) "camera" else "gallery"} request" })
            return
        }

        val hasPermissionAlready: Boolean
        val requestPermissionFunction: suspend () -> Boolean
        val sourceType: UIImagePickerControllerSourceType
        val typeString: String

        if (isCamera) {
            hasPermissionAlready = cameraGalleryManager.hasCameraPermission()
            requestPermissionFunction = cameraGalleryManager::requestCameraPermission
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            typeString = "Camera"
        } else {
            hasPermissionAlready = cameraGalleryManager.hasGalleryPermission()
            requestPermissionFunction = cameraGalleryManager::requestGalleryPermission
            sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            typeString = "Gallery"
        }

        Logger.d(tag = "CameraGalleryHandler", message = { "Checking $typeString permission" })
        if (hasPermissionAlready) {
            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "$typeString permission granted, launching $typeString" })
            presentImagePicker(sourceType)
        } else {
            Logger.d(
                tag = "CameraGalleryHandler",
                message = { "Requesting $typeString permission" })
            if (requestPermissionFunction()) {
                Logger.d(
                    tag = "CameraGalleryHandler",
                    message = { "$typeString permission granted after request, launching $typeString" })
                presentImagePicker(sourceType)
            } else {
                Logger.e(
                    tag = "CameraGalleryHandler",
                    message = { "$typeString permission denied" })
                onPermissionDenied("$typeString permission denied")
            }
        }
    }

    LaunchedEffect(checkAndLaunchCamera) {
        if (checkAndLaunchCamera) {
            checkAndLaunch(isCamera = true)
            changeCheckAndLaunchCamera(false)
        }
    }

    LaunchedEffect(checkAndLaunchGallery) {
        if (checkAndLaunchGallery) {
            checkAndLaunch(isCamera = false)
            changeCheckAndLaunchGallery(false)
        }
    }
}




