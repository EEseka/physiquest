package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.ImageCompressionError
import com.eseka.physiquest.core.domain.utils.Result
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import kotlin.math.roundToInt

actual class ImageCompressorImpl : ImageCompressor {

    actual override suspend fun compressImage(
        imageUrl: String,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError> {
        return withContext(Dispatchers.Default) {
            try {
                val url = NSURL.fileURLWithPath(imageUrl)
                val imageData = NSData.dataWithContentsOfURL(url)
                    ?: return@withContext Result.Error(ImageCompressionError.FILE_NOT_FOUND)

                val imageBytes = imageData.toByteArray()
                compressImageFromBytes(
                    imageBytes,
                    compressionThreshold,
                    determineMimeType(imageUrl)
                )
            } catch (e: Exception) {
                Result.Error(ImageCompressionError.FILE_IO_ERROR)
            }
        }
    }

    actual override suspend fun compressImageFromBytes(
        imageBytes: ByteArray,
        compressionThreshold: Long,
        mimeType: String?
    ): Result<ByteArray, ImageCompressionError> {
        return withContext(Dispatchers.Default) {
            try {
                ensureActive()

                val nsData = imageBytes.toNSData()
                val uiImage = UIImage.imageWithData(nsData)
                    ?: return@withContext Result.Error(ImageCompressionError.FILE_NOT_IMAGE)

                ensureActive()

                val compressionType = determineCompressionType(mimeType)
                return@withContext compressUIImage(uiImage, compressionType, compressionThreshold)
            } catch (e: Exception) {
                Result.Error(ImageCompressionError.COMPRESSION_ERROR)
            }
        }
    }

    private fun determineMimeType(imageUri: String): String? {
        return when {
            imageUri.contains(".png", ignoreCase = true) -> "image/png"
            imageUri.contains(".jpg", ignoreCase = true) ||
                    imageUri.contains(".jpeg", ignoreCase = true) -> "image/jpeg"

            imageUri.contains(".webp", ignoreCase = true) -> "image/webp"
            imageUri.contains(".heic", ignoreCase = true) -> "image/heic"
            else -> null
        }
    }

    private fun determineCompressionType(mimeType: String?): CompressionType {
        return when (mimeType) {
            "image/png" -> CompressionType.PNG
            "image/jpeg" -> CompressionType.JPEG
            "image/heic" -> CompressionType.JPEG // HEIC gets converted to JPEG
            else -> CompressionType.JPEG // Default to JPEG for better compression
        }
    }

    private suspend fun compressUIImage(
        uiImage: UIImage,
        compressionType: CompressionType,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError> = withContext(Dispatchers.Default) {
        var outputBytes: ByteArray
        var quality = 0.9 // Start with 90% quality like Android

        try {
            do {
                ensureActive()

                val nsData = when (compressionType) {
                    CompressionType.PNG -> UIImagePNGRepresentation(uiImage)
                    CompressionType.JPEG -> UIImageJPEGRepresentation(uiImage, quality)
                } ?: return@withContext Result.Error(ImageCompressionError.COMPRESSION_ERROR)

                outputBytes = nsData.toByteArray()

                // Match Android's exact logic
                val qualityInt = (quality * 100).toInt()
                val reduction = (qualityInt * 0.1).roundToInt()
                quality = (qualityInt - reduction) / 100.0

            } while (isActive &&
                outputBytes.size > compressionThreshold &&
                quality > 0.05 &&
                compressionType != CompressionType.PNG
            )

            if (outputBytes.isEmpty()) {
                return@withContext Result.Error(ImageCompressionError.COMPRESSION_ERROR)
            }

            Result.Success(outputBytes)
        } catch (e: Exception) {
            Result.Error(ImageCompressionError.COMPRESSION_ERROR)
        }
    }

    private enum class CompressionType {
        PNG, JPEG
    }
}

// Extension functions for iOS NSData/ByteArray conversion
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        this.getBytes(pinned.addressOf(0))
    }
    return bytes
}