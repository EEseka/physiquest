package com.eseka.physiquest.core.presentation.utils

import com.eseka.physiquest.core.domain.utils.FormValidationError
import com.eseka.physiquest.core.presentation.UiText
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.empty_name
import physiquest.composeapp.generated.resources.error_email_empty
import physiquest.composeapp.generated.resources.error_email_invalid
import physiquest.composeapp.generated.resources.error_invalid_name
import physiquest.composeapp.generated.resources.error_name_too_long
import physiquest.composeapp.generated.resources.error_name_too_short
import physiquest.composeapp.generated.resources.error_password_empty
import physiquest.composeapp.generated.resources.error_password_no_digit
import physiquest.composeapp.generated.resources.error_password_no_letter_lower
import physiquest.composeapp.generated.resources.error_password_no_letter_upper
import physiquest.composeapp.generated.resources.error_password_no_special_char
import physiquest.composeapp.generated.resources.error_password_too_long
import physiquest.composeapp.generated.resources.error_password_too_short
import physiquest.composeapp.generated.resources.error_passwords_do_not_match

fun FormValidationError.toUiText(): UiText {
    val stringRes = when (this) {
        FormValidationError.EMPTY_EMAIL -> Res.string.error_email_empty
        FormValidationError.INVALID_EMAIL -> Res.string.error_email_invalid
        FormValidationError.EMPTY_PASSWORD -> Res.string.error_password_empty
        FormValidationError.SHORT_PASSWORD -> Res.string.error_password_too_short
        FormValidationError.PASSWORD_TOO_LONG -> Res.string.error_password_too_long
        FormValidationError.PASSWORD_MISMATCH -> Res.string.error_passwords_do_not_match
        FormValidationError.NO_LOWER_LETTER_IN_PASSWORD -> Res.string.error_password_no_letter_lower
        FormValidationError.NO_UPPER_LETTER_IN_PASSWORD -> Res.string.error_password_no_letter_upper
        FormValidationError.NO_DIGIT_IN_PASSWORD -> Res.string.error_password_no_digit
        FormValidationError.NO_SPECIAL_CHAR_IN_PASSWORD -> Res.string.error_password_no_special_char
        FormValidationError.EMPTY_NAME -> Res.string.empty_name
        FormValidationError.NAME_TOO_LONG -> Res.string.error_name_too_long
        FormValidationError.NAME_TOO_SHORT -> Res.string.error_name_too_short
        FormValidationError.INVALID_NAME -> Res.string.error_invalid_name
    }
    return UiText.StringResourceId(stringRes)
}