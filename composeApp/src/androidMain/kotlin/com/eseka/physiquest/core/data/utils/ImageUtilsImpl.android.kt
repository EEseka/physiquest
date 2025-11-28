package com.eseka.physiquest.core.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.webkit.MimeTypeMap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class ImageUtilsImpl(
    private val context: Context
) : ImageUtils {

    actual override suspend fun imageBitmapToUri(
        imageBitmap: ImageBitmap,
        originalUriString: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = imageBitmap.asAndroidBitmap()

            // Get MIME type from the original URI or default to "image/jpeg"
            val mimeType = originalUriString?.let { uriString ->
                try {
                    val uri = uriString.toUri()
                    context.contentResolver.getType(uri)
                } catch (e: Exception) {
                    null
                }
            } ?: "image/jpeg"

            // Map MIME type to file extension, default to "jpg" if not found
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            // Determine the compression format based on the file extension
            val format = when (extension) {
                "png" -> Bitmap.CompressFormat.PNG
                "webp" -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            // Create a temporary file in the cache directory with the appropriate extension
            val file =
                File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.$extension")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(format, 100, outputStream)
            }

            // Return a content URI for the created file
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            uri.toString()
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Error converting ImageBitmap to URI: ${e.message}" })
            null
        }
    }

    private companion object {
        private const val TAG = "ImageUtils"
        val log = logging()
    }
}