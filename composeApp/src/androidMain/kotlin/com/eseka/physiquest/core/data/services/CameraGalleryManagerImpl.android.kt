package com.eseka.physiquest.core.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.services.CameraGalleryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class CameraGalleryManagerImpl(
    private val context: Context
) : CameraGalleryManager {

    actual override suspend fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun hasGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses photo picker, no permission needed
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    actual override suspend fun requestCameraPermission(): Boolean {
        return hasCameraPermission()
    }

    actual override suspend fun requestGalleryPermission(): Boolean {
        return hasGalleryPermission()
    }

    actual override suspend fun createTempPhotoUri(): String? = withContext(Dispatchers.IO) {
        try {
            // Ensure cache directory exists
            if (!context.cacheDir.exists()) {
                context.cacheDir.mkdirs()
            }

            val tempFile = File.createTempFile(
                "profile_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )

            Logger.d(tag = TAG, message = { "Created temp file: ${tempFile.absolutePath}" })

            val authority = "${context.packageName}.fileprovider"
            Logger.d(tag = TAG, message = { "Using authority: $authority" })

            val uri = FileProvider.getUriForFile(
                context,
                authority,
                tempFile
            )

            Logger.d(tag = TAG, message = { "Created URI: $uri" })
            uri.toString()
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error creating temp photo URI: ${e.message}" })
            Logger.e(tag = TAG, throwable = e, message = { "Stack trace:" })
            null
        }
    }

    actual override suspend fun getMimeTypeFromUri(uriString: String): String? {
        return try {
            val uri = uriString.toUri()
            val mimeType = context.contentResolver.getType(uri)
            Logger.d(tag = TAG, message = { "MIME type for $uriString: $mimeType" })
            mimeType
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error getting MIME type: ${e.message}" })
            null
        }
    }

    actual override suspend fun getExtensionFromMimeType(mimeType: String): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    private companion object {
        private const val TAG = "CameraGalleryManager"
    }
}