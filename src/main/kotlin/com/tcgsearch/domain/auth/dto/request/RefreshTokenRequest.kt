package com.tcgsearch.domain.auth.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @param:JsonProperty("refresh_token")
    @get:JsonProperty("refresh_token")
    @field:NotBlank(message = "can not be blank.")
    val refreshToken: String?,
)
