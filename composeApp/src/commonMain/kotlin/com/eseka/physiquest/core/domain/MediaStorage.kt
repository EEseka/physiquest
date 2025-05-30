package com.eseka.physiquest.core.domain

import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result

interface MediaStorage {
    suspend fun uploadPicture(
        storagePath: String,
        imageUrl: String
    ): Result<String, FirebaseStorageError>

    suspend fun getPicture(storagePath: String): Result<String, FirebaseStorageError>
    suspend fun deletePicture(storagePath: String): Result<Unit, FirebaseStorageError>
    suspend fun deleteAllPictures(storagePath: String): Result<Unit, FirebaseStorageError>
}