package com.eseka.physiquest.core.domain.validation

import com.eseka.physiquest.core.domain.utils.FormValidationError

class ValidateDisplayName {

    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, FormValidationError.EMPTY_NAME)
        }
        if (name.length < 3) {
            return ValidationResult(false, FormValidationError.NAME_TOO_SHORT)
        }
        if (name.length > 30) {
            return ValidationResult(false, FormValidationError.NAME_TOO_LONG)
        }
        if (!name.matches(Regex("^(?!.*\\s{2,})[\\p{L}0-9._'\\-\\s]*$"))) {
            return ValidationResult(false, FormValidationError.INVALID_NAME)
        }
        return ValidationResult(true)
    }
}