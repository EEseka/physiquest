package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.ImageGalleryManager

expect class ImageGalleryManagerImpl : ImageGalleryManager {
    override suspend fun saveImageToGallery(imageUri: String): Result<Unit>
    override suspend fun hasWritePermission(): Boolean
    override suspend fun requestWritePermission(): Boolean
}
