package com.eseka.physiquest.core.data.services

import android.content.Context
import android.net.Uri
import co.touchlab.kermit.Logger
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
                Logger.e(tag = TAG, message = { "Error saving image to cache" }, throwable = e)
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
                Logger.e(tag = TAG, message = { "Error creating file" }, throwable = e)
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
                Logger.e(tag = TAG, message = { "Error getting absolute path" }, throwable = e)
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
                Logger.e(tag = TAG, message = { "Error clearing cache" }, throwable = e)
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
                Logger.e(tag = TAG, message = { "Error downloading image" }, throwable = e)
                null
            }
        }
    }

    private companion object {
        private const val TAG = "FileManager"
    }
}