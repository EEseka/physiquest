package com.eseka.physiquest.core.data.services

import android.content.Context
import android.net.Uri
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.services.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL

actual class FileManagerImpl : FileManager {
    private var context: Context

    // Secondary constructor to initialize with context
    constructor(context: Context) {
        this.context = context
    }

    actual override suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, fileName)
                file.outputStream().use { outputStream ->
                    outputStream.write(bytes)
                }
                Uri.fromFile(file).toString()  // Return the URI of the saved file as string
            } catch (e: IOException) {
                log.e(tag = TAG, msg = { "Error saving image to cache" }, err = e)
                null
            }
        }
    }

    actual override suspend fun createFile(
        fileName: String,
        taskToPerformAfterCreation: (Any) -> Unit
    ): Any? {
        return withContext(Dispatchers.IO) {
            try {
                File(context.cacheDir, fileName).also {
                    @Suppress("UNCHECKED_CAST")
                    (taskToPerformAfterCreation as (File) -> Unit)(it)
                }
            } catch (e: IOException) {
                log.e(tag = TAG, msg = { "Error creating file" }, err = e)
                null
            }
        }
    }

    actual override suspend fun getAbsolutePath(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, fileName)
                file.absolutePath
            } catch (e: Exception) {
                log.e(tag = TAG, msg = { "Error getting absolute path" }, err = e)
                null
            }
        }
    }

    actual override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                cacheDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                log.e(tag = TAG, msg = { "Error clearing cache" }, err = e)
            }
        }
    }

    actual override suspend fun downloadImageFromUrl(imageUrl: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                URL(imageUrl).openStream().use { input ->
                    input.readBytes()
                }
            } catch (e: Exception) {
                log.e(tag = TAG, msg = { "Error downloading image" }, err = e)
                null
            }
        }
    }

    private companion object {
        private const val TAG = "FileManager"
        val log = logging()
    }
}