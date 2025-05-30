package com.eseka.physiquest.core.domain.services

import com.eseka.physiquest.core.domain.utils.ImageCompressionError
import com.eseka.physiquest.core.domain.utils.Result

interface ImageCompressor {
    suspend fun compressImage(
        imageUrl: String,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError>

    suspend fun compressImageFromBytes(
        imageBytes: ByteArray,
        compressionThreshold: Long,
        mimeType: String? = null
    ): Result<ByteArray, ImageCompressionError>
}
