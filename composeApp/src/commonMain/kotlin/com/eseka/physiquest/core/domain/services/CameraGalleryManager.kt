package com.eseka.physiquest.core.domain.services

interface CameraGalleryManager {
    suspend fun hasCameraPermission(): Boolean
    suspend fun hasGalleryPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
    suspend fun requestGalleryPermission(): Boolean
    suspend fun createTempPhotoUri(): String?
    suspend fun getMimeTypeFromUri(uriString: String): String?
    suspend fun getExtensionFromMimeType(mimeType: String): String?
}