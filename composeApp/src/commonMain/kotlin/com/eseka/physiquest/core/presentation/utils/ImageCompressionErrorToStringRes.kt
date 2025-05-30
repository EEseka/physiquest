package com.eseka.physiquest.core.presentation.utils

import com.eseka.physiquest.core.domain.utils.ImageCompressionError
import com.eseka.physiquest.core.presentation.UiText
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.error_compression_error
import physiquest.composeapp.generated.resources.error_file_io_error
import physiquest.composeapp.generated.resources.error_file_not_found
import physiquest.composeapp.generated.resources.error_file_not_image

fun ImageCompressionError.toUiText(): UiText {
    val stringRes = when (this) {
        ImageCompressionError.FILE_NOT_FOUND -> Res.string.error_file_not_found
        ImageCompressionError.FILE_IO_ERROR -> Res.string.error_file_io_error
        ImageCompressionError.FILE_NOT_IMAGE -> Res.string.error_file_not_image
        ImageCompressionError.COMPRESSION_ERROR -> Res.string.error_compression_error
    }
    return UiText.StringResourceId(stringRes)
}