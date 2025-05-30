package com.eseka.physiquest.core.domain.utils

import androidx.compose.ui.graphics.ImageBitmap

interface ImageUtils {
    suspend fun imageBitmapToUri(
        imageBitmap: ImageBitmap,
        originalUriString: String?
    ): String?
}