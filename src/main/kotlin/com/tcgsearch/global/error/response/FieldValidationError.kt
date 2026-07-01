package com.tcgsearch.global.error.response

import jakarta.validation.ConstraintViolation
import org.springframework.context.MessageSourceResolvable
import org.springframework.validation.FieldError

data class FieldValidationError(
    val message: String?,
    val exception: String,
) {
    companion object {
        fun of(error: FieldError) = FieldValidationError(
            message = error.defaultMessage,
            exception = error.exceptionName(),
        )

        fun of(error: MessageSourceResolvable) = FieldValidationError(
            message = error.defaultMessage,
            exception = error.exceptionName(),
        )

        fun of(error: ConstraintViolation<*>) = FieldValidationError(
            message = error.message,
            exception = error.exceptionName(),
        )

        private fun FieldError.exceptionName(): String =
            when (code) {
                "Pattern" -> "RegexException"
                null -> "ValidationException"
                else -> "${code}Exception"
            }

        private fun MessageSourceResolvable.exceptionName(): String {
            val code = codes?.firstOrNull()?.substringBefore(".")
            return when (code) {
                "Pattern" -> "RegexException"
                null -> "ValidationException"
                else -> "${code}Exception"
            }
        }

        private fun ConstraintViolation<*>.exceptionName(): String {
            val name = constraintDescriptor.annotation.annotationClass.simpleName
            return when (name) {
                "Pattern" -> "RegexException"
                null -> "ValidationException"
                else -> "${name}Exception"
            }
        }
    }
}
