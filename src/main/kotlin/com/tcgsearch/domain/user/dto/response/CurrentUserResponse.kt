package com.tcgsearch.domain.user.dto.response

import java.util.UUID

data class CurrentUserResponse(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: String,
    val enabled: Boolean,
)