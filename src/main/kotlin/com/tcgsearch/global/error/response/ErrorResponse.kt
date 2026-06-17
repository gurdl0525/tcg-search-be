package com.tcgsearch.global.error.response

import com.tcgsearch.global.error.exception.BaseException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException

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

        fun of(e: HttpMessageNotReadableException) = ErrorResponse(
            "ETC",
            e.message ?: e.localizedMessage,
            HttpStatus.BAD_REQUEST,
        )
    }
}
