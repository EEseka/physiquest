package com.eseka.physiquest.authentication.domain.validation

import com.eseka.physiquest.core.domain.utils.FormValidationError
import com.eseka.physiquest.core.domain.validation.ValidationResult

class ValidateEmail {

    // Basic but effective email regex for multiplatform use
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, FormValidationError.EMPTY_EMAIL)
        }
        if (!emailRegex.matches(email)) {
            return ValidationResult(false, FormValidationError.INVALID_EMAIL)
        }
        return ValidationResult(true)
    }
}