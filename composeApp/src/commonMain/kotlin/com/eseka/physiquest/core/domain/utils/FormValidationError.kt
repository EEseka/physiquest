package com.eseka.physiquest.core.domain.utils

enum class FormValidationError: Error {
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    NO_LOWER_LETTER_IN_PASSWORD,
    NO_UPPER_LETTER_IN_PASSWORD,
    NO_DIGIT_IN_PASSWORD,
    NO_SPECIAL_CHAR_IN_PASSWORD,
    SHORT_PASSWORD,
    PASSWORD_TOO_LONG,
    PASSWORD_MISMATCH,
    EMPTY_NAME,
    NAME_TOO_LONG,
    NAME_TOO_SHORT,
    INVALID_NAME
}