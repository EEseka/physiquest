package com.eseka.physiquest.core.domain.utils

enum class ImageCompressionError : Error {
    FILE_NOT_FOUND,
    FILE_IO_ERROR,
    FILE_NOT_IMAGE,
    COMPRESSION_ERROR
}
