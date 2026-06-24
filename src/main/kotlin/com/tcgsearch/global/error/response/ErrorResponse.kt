package com.tcgsearch.global.error.response

import com.tcgsearch.global.error.exception.BaseException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.method.annotation.HandlerMethodValidationException

data class ErrorResponse(
    val code: String,
    val message: String,
    val status: HttpStatus,
) {
    companion object {
        fun of(e: BaseException) = ErrorResponse(
            code = e.errorCode.code,
            message = e.errorCode.message,
            status = e.errorCode.status,
        )

        fun of(e: BindException) = ValidationErrorResponse.of(e.fieldErrors)

        fun of(e: HandlerMethodValidationException) = ValidationErrorResponse.of(e)

        fun of(e: ConstraintViolationException) = ValidationErrorResponse.of(e)

        fun of(e: HttpMessageNotReadableException) = ErrorResponse(
            "ETC",
            e.message ?: e.localizedMessage,
            HttpStatus.BAD_REQUEST,
        )
    }
}
