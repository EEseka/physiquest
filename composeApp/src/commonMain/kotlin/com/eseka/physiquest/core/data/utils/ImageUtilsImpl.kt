package com.eseka.physiquest.core.data.utils

import androidx.compose.ui.graphics.ImageBitmap
import com.eseka.physiquest.core.domain.utils.ImageUtils

expect class ImageUtilsImpl : ImageUtils {
    override suspend fun imageBitmapToUri(
        imageBitmap: ImageBitmap,
        originalUriString: String?
    ): String?
}