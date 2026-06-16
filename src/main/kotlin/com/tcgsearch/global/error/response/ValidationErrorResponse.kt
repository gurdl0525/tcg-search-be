package com.tcgsearch.global.error.response

import org.springframework.http.HttpStatus

data class ValidationErrorResponse(
    val status: HttpStatus,
    val fieldError: List<Map<String, String?>>,
    val code: String = "VALIDATION_ERROR"
)
