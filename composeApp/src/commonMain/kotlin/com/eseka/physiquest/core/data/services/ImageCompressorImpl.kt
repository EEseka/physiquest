package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.ImageCompressionError
import com.eseka.physiquest.core.domain.utils.Result

expect class ImageCompressorImpl : ImageCompressor {
    override suspend fun compressImage(
        imageUrl: String,
        compressionThreshold: Long
    ): Result<ByteArray, ImageCompressionError>

    override suspend fun compressImageFromBytes(
        imageBytes: ByteArray,
        compressionThreshold: Long,
        mimeType: String?
    ): Result<ByteArray, ImageCompressionError>
}