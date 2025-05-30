package com.eseka.physiquest.core.data.services

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.services.CameraGalleryManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

actual class CameraGalleryManagerImpl : CameraGalleryManager {

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun hasCameraPermission(): Boolean {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> true
            else -> false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun hasGalleryPermission(): Boolean {
        return when (PHPhotoLibrary.authorizationStatus()) {
            PHAuthorizationStatusAuthorized,
            PHAuthorizationStatusLimited -> true

            else -> false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestCameraPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                continuation.resume(granted)
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestGalleryPermission(): Boolean =
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

    actual override suspend fun createTempPhotoUri(): String? {
        try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String

            if (documentsPath != null) {
                val fileName = "profile_photo_${NSDate().timeIntervalSince1970.toLong()}.jpg"
                return "$documentsPath/$fileName"
            }
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error creating temp photo URI: ${e.message}" })
        }
        return null
    }

    actual override suspend fun getMimeTypeFromUri(uriString: String): String? {
        return try {
            val url = NSURL(string = uriString)
            val pathExtension = url.pathExtension

            when (pathExtension?.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "heic" -> "image/heic"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error getting MIME type: ${e.message}" })
            "image/jpeg"
        }
    }

    actual override suspend fun getExtensionFromMimeType(mimeType: String): String? {
        return when (mimeType.lowercase()) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/heic" -> "heic"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }

    private companion object {
        private const val TAG = "CameraGalleryManager"
    }
}