package com.eseka.physiquest.core.data.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.utils.ImageUtils
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGBitmapContextGetData
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation

@OptIn(ExperimentalForeignApi::class)
actual class ImageUtilsImpl : ImageUtils {

    actual override suspend fun imageBitmapToUri(
        imageBitmap: ImageBitmap,
        originalUriString: String?
    ): String? = withContext(Dispatchers.Default) {
        try {
            // Convert ImageBitmap to UIImage
            val uiImage = imageBitmap.toUIImage()

            // Determine file format based on original URI or default to JPEG
            val (data, extension) = when {
                originalUriString?.contains(".png", ignoreCase = true) == true -> {
                    Pair(UIImagePNGRepresentation(uiImage), "png")
                }

                originalUriString?.contains(".heic", ignoreCase = true) == true -> {
                    Pair(UIImageJPEGRepresentation(uiImage, 0.9), "jpg") // Convert HEIC to JPEG
                }

                else -> {
                    Pair(UIImageJPEGRepresentation(uiImage, 0.9), "jpg")
                }
            }

            if (data == null) {
                log.e(tag = TAG, msg = { "Failed to convert UIImage to data" })
                return@withContext null
            }

            // Create temporary file path
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String

            if (documentsPath == null) {
                log.e(tag = TAG, msg = { "Failed to get documents directory" })
                return@withContext null
            }

            val fileName = "cropped_image_${NSDate().timeIntervalSince1970.toLong()}.$extension"
            val filePath = "$documentsPath/$fileName"

            // Write data to file
            val success = data.writeToFile(filePath, atomically = true)

            if (success) {
                log.d(tag = TAG, msg = { "Successfully saved cropped image to: $filePath" })
                filePath
            } else {
                log.e(tag = TAG, msg = { "Failed to write image data to file" })
                null
            }
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Error converting ImageBitmap to URI: ${e.message}" })
            null
        }
    }

    private fun ImageBitmap.toUIImage(): UIImage {
        val pixelMap = this.toPixelMap()
        val width = this.width
        val height = this.height

        // Create a bitmap context
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value

        val bytesPerPixel = 4
        val bytesPerRow = bytesPerPixel * width

        val context = CGBitmapContextCreate(
            null,
            width.toULong(),
            height.toULong(),
            8u,
            bytesPerRow.toULong(),
            colorSpace,
            bitmapInfo
        )

        if (context == null) {
            throw RuntimeException("Failed to create bitmap context")
        }

        // Get the data pointer and fill it with pixel data
        val data = CGBitmapContextGetData(context)?.reinterpret<UByteVar>()

        if (data != null) {
            // Create a ByteArray to hold all pixel data
            val pixelData = ByteArray(width * height * bytesPerPixel)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = pixelMap[x, y]
                    val index = (y * width + x) * bytesPerPixel

                    // Convert to RGBA format (premultiplied alpha)
                    val alpha = (pixel.alpha * 255).toInt().toByte()
                    val red = (pixel.red * pixel.alpha * 255).toInt().toByte()
                    val green = (pixel.green * pixel.alpha * 255).toInt().toByte()
                    val blue = (pixel.blue * pixel.alpha * 255).toInt().toByte()

                    pixelData[index] = red
                    pixelData[index + 1] = green
                    pixelData[index + 2] = blue
                    pixelData[index + 3] = alpha
                }
            }

            // Copy the pixel data to the context
            pixelData.usePinned { pinned ->
                for (i in pixelData.indices) {
                    data[i] = pinned.get()[i].toUByte()
                }
            }
        }

        // Create CGImage from context
        val cgImage = CGBitmapContextCreateImage(context)
            ?: throw RuntimeException("Failed to create CGImage")

        // Create UIImage from CGImage
        return UIImage.imageWithCGImage(cgImage)
    }

    private companion object {
        private const val TAG = "ImageUtils"
        val log = logging()
    }
}