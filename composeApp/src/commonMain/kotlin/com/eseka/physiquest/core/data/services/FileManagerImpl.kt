package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.FileManager

// Platform-specific dependencies
expect class FileManagerImpl : FileManager {
    override suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String?
    override suspend fun createFile(
        fileName: String,
        taskToPerformAfterCreation: (Any) -> Unit
    ): Any?
    override suspend fun getAbsolutePath(fileName: String): String?
    override suspend fun downloadImageFromUrl(imageUrl: String): ByteArray?
    override suspend fun clearCache()
}