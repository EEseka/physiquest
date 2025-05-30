package com.eseka.physiquest.authentication.domain.validation

import com.eseka.physiquest.core.domain.utils.FormValidationError
import com.eseka.physiquest.core.domain.validation.ValidationResult

class ValidateRepeatedPassword {

    operator fun invoke(password: String, repeatedPassword: String): ValidationResult {
        if (password != repeatedPassword) {
            return ValidationResult(false, FormValidationError.PASSWORD_MISMATCH)
        }
        return ValidationResult(true)
    }
}