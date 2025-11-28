package com.eseka.physiquest.core.data.services

import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.services.ImageGalleryManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
                if (continuation.isActive) {
                    continuation.resume(granted)
                }
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun saveImageToGallery(imageUri: String): Result<Unit> =
        withContext(Dispatchers.IO) { // Switch to a background thread
            try {
                val url = NSURL(string = imageUri)
                // This is a blocking network call, so it MUST be on a background thread.
                val data = NSData.dataWithContentsOfURL(url)
                    ?: return@withContext Result.failure(Exception("Failed to download image from URL"))

                val image = UIImage.imageWithData(data)
                    ?: return@withContext Result.failure(Exception("Failed to create image from data"))

                // Bridge the callback-based Photos API to coroutines
                suspendCancellableCoroutine { continuation ->
                    PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                        PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                    }, completionHandler = { success, error ->
                        if (success) {
                            log.i(tag = TAG, msg = { "Image saved successfully to Photos" })
                            if (continuation.isActive) continuation.resume(Result.success(Unit))
                        } else {
                            val errorMessage = error?.localizedDescription() ?: "Unknown error"
                            log.e(
                                tag = TAG,
                                msg = { "Error saving image to Photos: $errorMessage" })
                            if (continuation.isActive) continuation.resume(
                                Result.failure(
                                    Exception(
                                        errorMessage
                                    )
                                )
                            )
                        }
                    })
                }
            } catch (e: Exception) {
                log.e(tag = TAG, msg = { "Error saving image to gallery: ${e.message}" })
                Result.failure(e)
            }
        }

    private companion object {
        private const val TAG = "ImageGalleryManager"
        val log = logging()
    }
}