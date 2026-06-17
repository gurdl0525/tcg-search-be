package com.tcgsearch.domain.auth.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * local 로그인 요청입니다.
 *
 * 성공하면 새 refresh token family를 시작하는 토큰 쌍을 발급합니다.
 *
 * @author gurdl0525
 * @since 18-06-2026
 */
data class LoginRequest(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(max = 64, message = "must be 64 characters or less.")
    val id: String?,

    @param:JsonProperty("password")
    @get:JsonProperty("password")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(min = 8, max = 72, message = "must be between 8 and 72 characters.")
    val password: String?,

    @param:JsonProperty("device_id")
    @get:JsonProperty("device_id")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(max = 128, message = "must be 128 characters or less.")
    val deviceId: String?,
)
