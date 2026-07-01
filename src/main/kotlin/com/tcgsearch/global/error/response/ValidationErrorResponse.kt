package com.tcgsearch.global.error.response

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.method.ParameterValidationResult
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.annotation.HandlerMethodValidationException

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

        fun of(ex: HandlerMethodValidationException) = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            fieldError = ex.parameterValidationResults
                .groupBy { it.requestParameterName() }
                .mapValues { (_, results) ->
                    results.flatMap { result ->
                        result.resolvableErrors.map { FieldValidationError.of(it) }
                    }
                },
        )

        fun of(ex: ConstraintViolationException) = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            fieldError = ex.constraintViolations
                .groupBy { it.requestParameterName() }
                .mapValues { (_, violations) -> violations.map { FieldValidationError.of(it) } },
        )

        private fun ParameterValidationResult.requestParameterName(): String {
            val requestParam = methodParameter.getParameterAnnotation(RequestParam::class.java)
            return requestParam?.name?.takeIf { it.isNotBlank() }
                ?: requestParam?.value?.takeIf { it.isNotBlank() }
                ?: methodParameter.parameterName
                ?: "request"
        }

        private fun ConstraintViolation<*>.requestParameterName(): String =
            propertyPath
                .map { it.name }
                .lastOrNull()
                ?.toRequestParameterName()
                ?: "request"

        private fun String.toRequestParameterName(): String =
            when (this) {
                "searchWord" -> "search_word"
                "sortBy" -> "sort_by"
                "cardTypes" -> "card_type"
                "cardSets" -> "card_set"
                "isParallel" -> "is_parallel"
                "languageCode" -> "language_code"
                "regionCode" -> "region_code"
                "illustrationTypes" -> "illustration_type"
                "foilTreatments" -> "foil_treatment"
                "blockNo" -> "block_no"
                else -> replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
            }
    }
}