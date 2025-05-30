package com.eseka.physiquest.authentication.domain.validation

import com.eseka.physiquest.core.domain.utils.FormValidationError
import com.eseka.physiquest.core.domain.validation.ValidationResult

class ValidateSignInPassword {

    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, FormValidationError.EMPTY_PASSWORD)
        }
        return ValidationResult(true)
    }
}
