package com.eseka.physiquest.core.domain.services

interface FileManager {
    suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String?

    // Using Any as File is platform-specific
    suspend fun createFile(fileName: String, taskToPerformAfterCreation: (Any) -> Unit): Any?
    suspend fun getAbsolutePath(fileName: String): String?
    suspend fun downloadImageFromUrl(imageUrl: String): ByteArray?
    suspend fun clearCache()
}
