package com.eseka.physiquest.core.data.services

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.services.FileManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSArray
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.data
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.Foundation.writeToURL

actual class FileManagerImpl : FileManager {
    private val fileManager = NSFileManager.defaultManager
    private val cacheDirectory: NSURL? by lazy {
        val urls = fileManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        urls.firstOrNull() as? NSURL
    }

    actual override suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String? {
        return withContext(Dispatchers.Default) {
            try {
                val cacheDir = cacheDirectory ?: return@withContext null
                val fileURL = cacheDir.URLByAppendingPathComponent(fileName)
                    ?: return@withContext null

                val nsData = bytes.toNSData()
                if (nsData.writeToURL(fileURL, true)) {
                    fileURL.absoluteString
                } else {
                    null
                }
            } catch (e: Exception) {
                Logger.e(
                    tag = TAG,
                    message = { "Error saving image to cache: ${e.message ?: "Unknown error"}" })
                null
            }
        }
    }

    actual override suspend fun createFile(
        fileName: String,
        taskToPerformAfterCreation: (Any) -> Unit
    ): Any? {
        return withContext(Dispatchers.Default) {
            try {
                val cacheDir = cacheDirectory ?: return@withContext null
                val fileURL = cacheDir.URLByAppendingPathComponent(fileName)
                    ?: return@withContext null

                // Create empty file
                NSData.data().writeToURL(fileURL, true)

                // Call the task with the NSURL
                taskToPerformAfterCreation(fileURL)

                return@withContext fileURL
            } catch (e: Exception) {
                Logger.e(
                    tag = TAG,
                    message = { "Error creating file: ${e.message ?: "Unknown error"}" })
                null
            }
        }
    }

    actual override suspend fun getAbsolutePath(fileName: String): String? {
        return withContext(Dispatchers.Default) {
            try {
                val cacheDir = cacheDirectory ?: return@withContext null
                val fileURL = cacheDir.URLByAppendingPathComponent(fileName)
                fileURL?.path
            } catch (e: Exception) {
                Logger.e(tag = TAG, message = { "Error getting absolute path" }, throwable = e)
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun clearCache() {
        withContext(Dispatchers.Default) {
            try {
                val cacheDir = cacheDirectory?.path ?: return@withContext
                val fileManager = NSFileManager.defaultManager

                val contents = fileManager.contentsOfDirectoryAtPath(cacheDir, null)
                        as? NSArray ?: return@withContext

                for (i in 0 until contents.count.toInt()) {
                    val fileName = contents.objectAtIndex(i.toULong()) as? NSString ?: continue
                    val filePath = "$cacheDir/$fileName"
                    try {
                        fileManager.removeItemAtPath(filePath, null)
                    } catch (e: Exception) {
                        Logger.e(tag = TAG, message = { "Failed to delete file: $filePath" })
                    }
                }
            } catch (e: Exception) {
                Logger.e(
                    tag = TAG,
                    message = { "Error clearing cache: ${e.message ?: "Unknown error"}" })
            }
        }
    }

    actual override suspend fun downloadImageFromUrl(imageUrl: String): ByteArray? {
        return withContext(Dispatchers.Default) {
            try {
                val url = NSURL(string = imageUrl)
                val data = NSData.dataWithContentsOfURL(url) ?: return@withContext null
                data.toByteArray()
            } catch (e: Exception) {
                Logger.e(
                    tag = TAG,
                    message = { "Error downloading image: ${e.message ?: "Unknown error"}" })
                null
            }
        }
    }

    private companion object {
        private const val TAG = "FileManager"
    }
}

// Extension functions to convert between ByteArray and NSData
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