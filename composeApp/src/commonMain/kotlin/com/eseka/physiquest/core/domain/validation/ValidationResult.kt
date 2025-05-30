package com.eseka.physiquest.core.domain.validation

import com.eseka.physiquest.core.domain.utils.FormValidationError

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: FormValidationError? = null
)