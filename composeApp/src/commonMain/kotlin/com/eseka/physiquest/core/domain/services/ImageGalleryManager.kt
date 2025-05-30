package com.eseka.physiquest.core.domain.services

interface ImageGalleryManager {
    suspend fun saveImageToGallery(imageUri: String): Result<Unit>
    suspend fun hasWritePermission(): Boolean
    suspend fun requestWritePermission(): Boolean
}
