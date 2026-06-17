package com.tcgsearch.domain.user.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class CurrentUserResponse(
    val id: UUID,
    val email: String,

    @get:JsonProperty("display_name")
    val displayName: String,

    val role: String,
    val enabled: Boolean,
)
