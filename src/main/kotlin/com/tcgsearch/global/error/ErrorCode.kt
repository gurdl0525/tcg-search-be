package com.tcgsearch.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {

    // 401(Unauthorized)
    NO_AUTHENTICATION("NO_AUTHENTICATION", "Authentication is required.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token.", HttpStatus.UNAUTHORIZED),
    ;
}