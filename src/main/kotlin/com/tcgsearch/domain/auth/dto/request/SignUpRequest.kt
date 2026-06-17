package com.tcgsearch.domain.auth.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * local 회원가입 요청입니다.
 *
 * `id`는 현재 local provider의 provider subject로 저장됩니다.
 *
 * @author gurdl0525
 * @since 18-06-2026
 */
data class SignUpRequest(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(min = 4, max = 20, message = "must be between 4 and 20 characters.")
    @field:Pattern(
        regexp = LOCAL_ID_PATTERN,
        message = "must be 4 to 20 lowercase letters, numbers, dots, or underscores and start/end with a letter or number.",
    )
    val id: String?,

    @param:JsonProperty("password")
    @get:JsonProperty("password")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(min = 8, max = 72, message = "must be between 8 and 72 characters.")
    @field:Pattern(
        regexp = PASSWORD_PATTERN,
        message = "must include letters, numbers, special characters and must not contain whitespace.",
    )
    val password: String?,

    @param:JsonProperty("device_id")
    @get:JsonProperty("device_id")
    @field:NotBlank(message = "can not be blank.")
    @field:Size(max = 128, message = "must be 128 characters or less.")
    val deviceId: String?,
)

private const val LOCAL_ID_PATTERN = """^(?!.*[._]{2})[a-z0-9](?:[a-z0-9._]{2,18}[a-z0-9])$"""
private const val PASSWORD_PATTERN =
    """^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?])(?!.*\s).{8,72}$"""
