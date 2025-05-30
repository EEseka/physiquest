package com.eseka.physiquest.core.presentation.utils

import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.FirebaseFirestoreError
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.presentation.UiText
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.account_not_found
import physiquest.composeapp.generated.resources.error_aborted
import physiquest.composeapp.generated.resources.error_account_exists_with_different_credential
import physiquest.composeapp.generated.resources.error_already_exists
import physiquest.composeapp.generated.resources.error_bucket_not_found
import physiquest.composeapp.generated.resources.error_canceled
import physiquest.composeapp.generated.resources.error_cancelled
import physiquest.composeapp.generated.resources.error_credential_already_in_use
import physiquest.composeapp.generated.resources.error_credential_creation_error
import physiquest.composeapp.generated.resources.error_credential_fetching_error
import physiquest.composeapp.generated.resources.error_data_loss
import physiquest.composeapp.generated.resources.error_deadline_exceeded
import physiquest.composeapp.generated.resources.error_email_already_in_use
import physiquest.composeapp.generated.resources.error_failed_precondition
import physiquest.composeapp.generated.resources.error_failed_reauthentication
import physiquest.composeapp.generated.resources.error_google_sign_in_failed
import physiquest.composeapp.generated.resources.error_image_delete_failed
import physiquest.composeapp.generated.resources.error_image_upload_failed
import physiquest.composeapp.generated.resources.error_internal
import physiquest.composeapp.generated.resources.error_invalid_argument
import physiquest.composeapp.generated.resources.error_invalid_checksum
import physiquest.composeapp.generated.resources.error_invalid_credential
import physiquest.composeapp.generated.resources.error_invalid_email
import physiquest.composeapp.generated.resources.error_invalid_link
import physiquest.composeapp.generated.resources.error_invalid_photo_url
import physiquest.composeapp.generated.resources.error_invalid_user_token
import physiquest.composeapp.generated.resources.error_io_error
import physiquest.composeapp.generated.resources.error_network_error
import physiquest.composeapp.generated.resources.error_not_found
import physiquest.composeapp.generated.resources.error_object_not_found
import physiquest.composeapp.generated.resources.error_operation_not_allowed
import physiquest.composeapp.generated.resources.error_out_of_range
import physiquest.composeapp.generated.resources.error_permission_denied
import physiquest.composeapp.generated.resources.error_project_not_found
import physiquest.composeapp.generated.resources.error_quota_exceeded
import physiquest.composeapp.generated.resources.error_requires_recent_login
import physiquest.composeapp.generated.resources.error_resource_exhausted
import physiquest.composeapp.generated.resources.error_retry_limit_exceeded
import physiquest.composeapp.generated.resources.error_session_expired
import physiquest.composeapp.generated.resources.error_too_many_requests_for_firebase
import physiquest.composeapp.generated.resources.error_unauthenticated
import physiquest.composeapp.generated.resources.error_unauthorized_domain
import physiquest.composeapp.generated.resources.error_unavailable
import physiquest.composeapp.generated.resources.error_unimplemented
import physiquest.composeapp.generated.resources.error_unknown
import physiquest.composeapp.generated.resources.error_user_disabled
import physiquest.composeapp.generated.resources.error_user_not_signed_in
import physiquest.composeapp.generated.resources.error_user_token_expired
import physiquest.composeapp.generated.resources.error_weak_password
import physiquest.composeapp.generated.resources.error_wrong_password

fun FirebaseAuthError.toUiText(): UiText {
    val stringRes = when (this) {
        FirebaseAuthError.INVALID_EMAIL -> Res.string.error_invalid_email
        FirebaseAuthError.USER_NOT_FOUND -> Res.string.account_not_found
        FirebaseAuthError.WRONG_PASSWORD -> Res.string.error_wrong_password
        FirebaseAuthError.EMAIL_ALREADY_IN_USE -> Res.string.error_email_already_in_use
        FirebaseAuthError.WEAK_PASSWORD -> Res.string.error_weak_password
        FirebaseAuthError.NETWORK_ERROR -> Res.string.error_network_error
        FirebaseAuthError.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests_for_firebase
        FirebaseAuthError.UNKNOWN -> Res.string.error_unknown
        FirebaseAuthError.OPERATION_NOT_ALLOWED -> Res.string.error_operation_not_allowed
        FirebaseAuthError.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL -> Res.string.error_account_exists_with_different_credential
        FirebaseAuthError.CREDENTIAL_ALREADY_IN_USE -> Res.string.error_credential_already_in_use
        FirebaseAuthError.INVALID_CREDENTIAL -> Res.string.error_invalid_credential
        FirebaseAuthError.USER_DISABLED -> Res.string.error_user_disabled
        FirebaseAuthError.REQUIRES_RECENT_LOGIN -> Res.string.error_requires_recent_login
        FirebaseAuthError.INVALID_LINK -> Res.string.error_invalid_link
        FirebaseAuthError.USER_TOKEN_EXPIRED -> Res.string.error_user_token_expired
        FirebaseAuthError.INVALID_USER_TOKEN -> Res.string.error_invalid_user_token
        FirebaseAuthError.USER_NOT_SIGNED_IN -> Res.string.error_user_not_signed_in
        FirebaseAuthError.SESSION_EXPIRED -> Res.string.error_session_expired
        FirebaseAuthError.QUOTA_EXCEEDED -> Res.string.error_quota_exceeded
        FirebaseAuthError.UNAUTHORIZED_DOMAIN -> Res.string.error_unauthorized_domain
        FirebaseAuthError.INVALID_PHOTO_URL -> Res.string.error_invalid_photo_url
        FirebaseAuthError.FAILED_REAUTHENTICATION -> Res.string.error_failed_reauthentication
        FirebaseAuthError.GOOGLE_SIGN_IN_FAILED -> Res.string.error_google_sign_in_failed
        FirebaseAuthError.CREDENTIAL_CREATION_ERROR -> Res.string.error_credential_creation_error
        FirebaseAuthError.CREDENTIAL_FETCHING_ERROR -> Res.string.error_credential_fetching_error
        FirebaseAuthError.IMAGE_UPLOAD_FAILED -> Res.string.error_image_upload_failed
        FirebaseAuthError.IMAGE_DELETE_FAILED -> Res.string.error_image_delete_failed
    }
    return UiText.StringResourceId(stringRes)
}

fun FirebaseFirestoreError.toUiText(): UiText {
    val stringRes = when (this) {
        FirebaseFirestoreError.CANCELLED -> Res.string.error_cancelled
        FirebaseFirestoreError.UNKNOWN -> Res.string.error_unknown
        FirebaseFirestoreError.INVALID_ARGUMENT -> Res.string.error_invalid_argument
        FirebaseFirestoreError.DEADLINE_EXCEEDED -> Res.string.error_deadline_exceeded
        FirebaseFirestoreError.NETWORK_ERROR -> Res.string.error_network_error
        FirebaseFirestoreError.NOT_FOUND -> Res.string.error_not_found
        FirebaseFirestoreError.ALREADY_EXISTS -> Res.string.error_already_exists
        FirebaseFirestoreError.PERMISSION_DENIED -> Res.string.error_permission_denied
        FirebaseFirestoreError.RESOURCE_EXHAUSTED -> Res.string.error_resource_exhausted
        FirebaseFirestoreError.FAILED_PRECONDITION -> Res.string.error_failed_precondition
        FirebaseFirestoreError.ABORTED -> Res.string.error_aborted
        FirebaseFirestoreError.OUT_OF_RANGE -> Res.string.error_out_of_range
        FirebaseFirestoreError.UNIMPLEMENTED -> Res.string.error_unimplemented
        FirebaseFirestoreError.INTERNAL -> Res.string.error_internal
        FirebaseFirestoreError.UNAVAILABLE -> Res.string.error_unavailable
        FirebaseFirestoreError.DATA_LOSS -> Res.string.error_data_loss
        FirebaseFirestoreError.UNAUTHENTICATED -> Res.string.error_unauthenticated
    }
    return UiText.StringResourceId(stringRes)
}

fun FirebaseStorageError.toUiText(): UiText {
    val stringRes = when (this) {
        FirebaseStorageError.UNKNOWN -> Res.string.error_unknown
        FirebaseStorageError.OBJECT_NOT_FOUND -> Res.string.error_object_not_found
        FirebaseStorageError.BUCKET_NOT_FOUND -> Res.string.error_bucket_not_found
        FirebaseStorageError.PROJECT_NOT_FOUND -> Res.string.error_project_not_found
        FirebaseStorageError.QUOTA_EXCEEDED -> Res.string.error_quota_exceeded
        FirebaseStorageError.NOT_AUTHENTICATED -> Res.string.error_unauthenticated
        FirebaseStorageError.NOT_AUTHORIZED -> Res.string.error_permission_denied
        FirebaseStorageError.RETRY_LIMIT_EXCEEDED -> Res.string.error_retry_limit_exceeded
        FirebaseStorageError.INVALID_CHECKSUM -> Res.string.error_invalid_checksum
        FirebaseStorageError.CANCELED -> Res.string.error_canceled
        FirebaseStorageError.NETWORK_ERROR -> Res.string.error_network_error
        FirebaseStorageError.IO_ERROR -> Res.string.error_io_error
    }
    return UiText.StringResourceId(stringRes)
}