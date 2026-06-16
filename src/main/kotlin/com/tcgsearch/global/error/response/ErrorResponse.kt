package com.tcgsearch.global.error.response

import com.tcgsearch.global.error.exception.BaseException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError

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

        fun of(e: BindException) = ValidationErrorResponse(
            HttpStatus.BAD_REQUEST,
            e.fieldErrors.map { it: FieldError -> mapOf(Pair(it.field, it.defaultMessage)) }
        )

        fun of(e: HttpMessageNotReadableException) = ErrorResponse(
            "ETC",
            e.message ?: e.localizedMessage,
            HttpStatus.BAD_REQUEST,
        )
    }
}
