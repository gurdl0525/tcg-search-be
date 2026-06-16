package com.tcgsearch.global.error.handler

import com.tcgsearch.global.error.exception.BaseException
import com.tcgsearch.global.error.response.ErrorResponse
import jakarta.xml.bind.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(BaseException::class)
    private fun handleBaseException(ex: BaseException) = ResponseEntity
        .status(ex.errorCode.status)
        .body(ErrorResponse.of(ex))

    @ExceptionHandler(BindException::class)
    private fun handleBindException(ex: BaseException) = ResponseEntity
        .status(ex.errorCode.status)
        .body(ErrorResponse.of(ex))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    private fun handleMethodArgNotValid(ex: MethodArgumentNotValidException) = ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ErrorResponse.of(ex))

    @ExceptionHandler(ValidationException::class)
    private fun handleValidationException(ex: ValidationException) = ResponseEntity
        .badRequest()
        .body(ex.message)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    private fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException) = ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(ex))
}