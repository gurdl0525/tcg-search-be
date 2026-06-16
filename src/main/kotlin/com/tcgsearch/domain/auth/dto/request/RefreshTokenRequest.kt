package com.tcgsearch.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "can not be blank.")
    val refreshToken: String?,
)