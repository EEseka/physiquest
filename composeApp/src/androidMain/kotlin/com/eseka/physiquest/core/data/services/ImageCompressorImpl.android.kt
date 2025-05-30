package com.eseka.physiquest.core.data.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.net.toUri
import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.ImageCompressionError
import com.eseka.physiquest.core.domain.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.roundToInt

actual class ImageCompressorImpl : ImageCompressor {
    private var context: Context

    constructor(context: Context) {
        this.context = context
    }

    actual override suspend fun compressImage(
        imageUrl: String,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError> {
        return withContext(Dispatchers.IO) {
            val contentUri = imageUrl.toUri()
            val inputBytes = try {
                context.contentResolver.openInputStream(contentUri)?.use { it.readBytes() }
                    ?: return@withContext Result.Error(ImageCompressionError.FILE_NOT_FOUND)
            } catch (e: IOException) {
                return@withContext Result.Error(ImageCompressionError.FILE_IO_ERROR)
            }

            ensureActive()

            val mimeType = context.contentResolver.getType(contentUri)
            compressImageFromBytes(inputBytes, compressionThreshold, mimeType)
        }
    }

    actual override suspend fun compressImageFromBytes(
        imageBytes: ByteArray,
        compressionThreshold: Long,
        mimeType: String?
    ): Result<ByteArray, ImageCompressionError> {
        return withContext(Dispatchers.IO) {
            ensureActive()

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext Result.Error(ImageCompressionError.FILE_NOT_IMAGE)

            ensureActive()

            val compressFormat = determineCompressFormat(mimeType)
            return@withContext compressBitmap(bitmap, compressFormat, compressionThreshold)
        }
    }

    private fun determineCompressFormat(mimeType: String?): Bitmap.CompressFormat {
        return when (mimeType) {
            "image/png" -> Bitmap.CompressFormat.PNG
            "image/jpeg" -> Bitmap.CompressFormat.JPEG
            "image/webp" -> if (Build.VERSION.SDK_INT >= 30) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else Bitmap.CompressFormat.WEBP

            else -> Bitmap.CompressFormat.JPEG
        }
    }

    private suspend fun compressBitmap(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError> = withContext(Dispatchers.Default) {
        var outputBytes: ByteArray
        var quality = 90

        try {
            do {
                ByteArrayOutputStream().use { outputStream ->
                    bitmap.compress(compressFormat, quality, outputStream)
                    outputBytes = outputStream.toByteArray()
                    quality -= (quality * 0.1).roundToInt()
                }
            } while (isActive &&
                outputBytes.size > compressionThreshold &&
                quality > 5 &&
                compressFormat != Bitmap.CompressFormat.PNG
            )

            if (outputBytes.isEmpty()) {
                return@withContext Result.Error(ImageCompressionError.COMPRESSION_ERROR)
            }

            Result.Success(outputBytes)
        } catch (e: Exception) {
            Result.Error(ImageCompressionError.COMPRESSION_ERROR)
        }
    }
}