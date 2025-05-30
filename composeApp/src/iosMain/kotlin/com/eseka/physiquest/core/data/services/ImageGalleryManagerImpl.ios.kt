package com.eseka.physiquest.core.data.services

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.services.ImageGalleryManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

actual class ImageGalleryManagerImpl : ImageGalleryManager {

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun hasWritePermission(): Boolean {
        return when (PHPhotoLibrary.authorizationStatus()) {
            PHAuthorizationStatusAuthorized,
            PHAuthorizationStatusLimited -> true

            else -> false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestWritePermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorization { status ->
                val granted = when (status) {
                    PHAuthorizationStatusAuthorized,
                    PHAuthorizationStatusLimited -> true

                    else -> false
                }
                continuation.resume(granted)
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun saveImageToGallery(imageUri: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            try {
                // Download image data
                val url = NSURL(string = imageUri)
                val data = NSData.dataWithContentsOfURL(url)

                if (data == null) {
                    continuation.resume(Result.failure(Exception("Failed to download image from URL")))
                    return@suspendCancellableCoroutine
                }

                val image = UIImage.imageWithData(data)
                if (image == null) {
                    continuation.resume(Result.failure(Exception("Failed to create image from data")))
                    return@suspendCancellableCoroutine
                }

                // Save to Photos
                PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                    changeBlock = {
                        PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                    },
                    completionHandler = { success, error ->
                        if (success) {
                            Logger.i(tag = TAG, message = { "Image saved successfully to Photos" })
                            continuation.resume(Result.success(Unit))
                        } else {
                            val errorMessage = error?.localizedDescription() ?: "Unknown error"
                            Logger.e(
                                tag = TAG,
                                message = { "Error saving image to Photos: $errorMessage" })
                            continuation.resume(Result.failure(Exception(errorMessage)))
                        }
                    }
                )
            } catch (e: Exception) {
                Logger.e(tag = TAG, message = { "Error saving image to gallery: ${e.message}" })
                continuation.resume(Result.failure(e))
            }
        }

    private companion object {
        private const val TAG = "ImageGalleryManager"
    }
}