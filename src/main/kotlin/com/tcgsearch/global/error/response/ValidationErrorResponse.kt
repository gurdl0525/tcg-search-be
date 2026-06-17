package com.tcgsearch.global.error.response

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError

data class ValidationErrorResponse(
    val status: HttpStatus,
    @get:JsonProperty("field_error")
    val fieldError: Map<String, List<FieldValidationError>>,
    val code: String = "VALIDATION_ERROR",
) {
    companion object {
        fun of(fieldErrors: List<FieldError>) = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            fieldError = fieldErrors
                .groupBy { it.field }
                .mapValues { (_, errors) -> errors.map { FieldValidationError.of(it) } },
        )
    }
}

data class FieldValidationError(
    val message: String?,
    val exception: String,
) {
    companion object {
        fun of(error: FieldError) = FieldValidationError(
            message = error.defaultMessage,
            exception = error.exceptionName(),
        )

        private fun FieldError.exceptionName(): String =
            when (code) {
                "Pattern" -> "RegexException"
                null -> "ValidationException"
                else -> "${code}Exception"
            }
    }
}
