package com.tcgsearch.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {

    // 401(Unauthorized)
    NO_AUTHENTICATION("NO_AUTHENTICATION", "Authentication is required.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "Invalid access token.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token.", HttpStatus.UNAUTHORIZED),
    INVALID_LOGIN_CREDENTIALS("INVALID_LOGIN_CREDENTIALS", "Invalid login credentials.", HttpStatus.UNAUTHORIZED),

    // 409(Conflict)
    DUPLICATE_USER_ID("DUPLICATE_USER_ID", "User id already exists.", HttpStatus.CONFLICT),

    // 404(Not Found)
    USER_NOT_FOUND("USER_NOT_FOUND", "User was not found.", HttpStatus.NOT_FOUND),
    ;
}
