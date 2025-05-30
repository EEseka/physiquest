package com.eseka.physiquest.core.data.firebase

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseStorageCall
import com.eseka.physiquest.core.data.firebase.utils.toFirebaseFile
import com.eseka.physiquest.core.domain.MediaStorage
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.storage.FirebaseStorage

class FirebaseMediaStorage(private val storage: FirebaseStorage) : MediaStorage {
    override suspend fun uploadPicture(
        storagePath: String,
        imageUrl: String
    ): Result<String, FirebaseStorageError> {
        if (storagePath.isBlank() || !storagePath.contains('/')) {
            Logger.e(tag = TAG, message = { "Invalid storage path provided: $storagePath" })
            return Result.Error(FirebaseStorageError.INVALID_CHECKSUM)
        }
        return safeFirebaseStorageCall {
            val storageRef = storage.reference.child(storagePath)

            storageRef.putFile(imageUrl.toFirebaseFile())
            Logger.i(tag = TAG, message = { "Successfully uploaded to $storagePath" })

            val downloadUrl = storageRef.getDownloadUrl()
            Logger.i(
                tag = TAG,
                message = { "Successfully retrieved download URL for $storagePath" })
            downloadUrl.toString()
        }
    }

    override suspend fun getPicture(storagePath: String): Result<String, FirebaseStorageError> {
        if (storagePath.isBlank() || !storagePath.contains('/')) {
            Logger.e(tag = TAG, message = { "Invalid storage path provided: $storagePath" })
            return Result.Error(FirebaseStorageError.INVALID_CHECKSUM)
        }
        return safeFirebaseStorageCall {
            val storageRef = storage.reference.child(storagePath)
            val downloadUrl = storageRef.getDownloadUrl()
            Logger.i(
                tag = TAG,
                message = { "Successfully retrieved download URL for $storagePath" })
            downloadUrl.toString()
        }
    }

    override suspend fun deletePicture(storagePath: String): Result<Unit, FirebaseStorageError> {
        if (storagePath.isBlank()) {
            Logger.e(
                tag = TAG,
                message = { "Invalid storage path provided for deletion: $storagePath" })
            return Result.Error(FirebaseStorageError.INVALID_CHECKSUM)
        }
        return safeFirebaseStorageCall {
            val storageRef = storage.reference.child(storagePath)
            storageRef.delete()
            Logger.i(tag = TAG, message = { "Successfully deleted $storagePath" })
        }
    }

    override suspend fun deleteAllPictures(storagePath: String): Result<Unit, FirebaseStorageError> {
        if (storagePath.isBlank()) {
            Logger.e(
                tag = TAG,
                message = { "No storage paths provided for deletion: $storagePath" })
            return Result.Error(FirebaseStorageError.INVALID_CHECKSUM)
        }
        return safeFirebaseStorageCall {
            val storageRef = storage.reference.child(storagePath)
            storageRef.listAll().items.forEach { item ->
                item.delete()
                Logger.i(tag = TAG, message = { "Successfully deleted: ${item.path}" })
            }
            Logger.i(tag = TAG, message = { "Successfully deleted all images under $storagePath" })
        }
    }

    companion object {
        private const val TAG = "FirebaseMediaStorage"
    }
}
