package com.eseka.physiquest.core.data.services

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.services.ImageGalleryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

actual class ImageGalleryManagerImpl(
    private val context: Context
) : ImageGalleryManager {

    actual override suspend fun hasWritePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10 and above don't need permission for scoped storage
            true
        }
    }

    actual override suspend fun requestWritePermission(): Boolean {
        // This needs to be handled at UI level, return current permission status
        return hasWritePermission()
    }

    actual override suspend fun saveImageToGallery(imageUri: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val displayName = "PhysiQuest_$timestamp.png"

                val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val imageDetails = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhysiQuest")
                    }
                }

                val contentResolver = context.contentResolver
                val savedImageUri = contentResolver.insert(imageCollection, imageDetails)
                    ?: throw IOException("Failed to create new MediaStore record.")

                contentResolver.openOutputStream(savedImageUri)?.use { outputStream ->
                    val uri = imageUri.toUri()
                    // For Firebase Storage URLs or HTTP URLs, use URL connection
                    if (uri.scheme == "https" || uri.scheme == "http") {
                        val connection = java.net.URL(imageUri).openConnection()
                        connection.connect()
                        connection.getInputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    } else {
                        // For local content URIs, use ContentResolver
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        } ?: throw IOException("Failed to open input stream")
                    }
                } ?: throw IOException("Failed to open output stream")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageDetails.clear()
                    imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(savedImageUri, imageDetails, null, null)
                }

                log.i(tag = TAG, msg = { "Image saved successfully to gallery" })
                Result.success(Unit)
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
