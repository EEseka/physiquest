package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.CameraGalleryManager

expect class CameraGalleryManagerImpl : CameraGalleryManager {
    override suspend fun hasCameraPermission(): Boolean
    override suspend fun hasGalleryPermission(): Boolean
    override suspend fun requestCameraPermission(): Boolean
    override suspend fun requestGalleryPermission(): Boolean
    override suspend fun createTempPhotoUri(): String?
    override suspend fun getMimeTypeFromUri(uriString: String): String?
    override suspend fun getExtensionFromMimeType(mimeType: String): String?
}

