package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.user.entity.User
import java.time.Instant
import java.util.UUID

interface AuthService {
    fun rotateRefreshToken(rawRefreshToken: String): TokenResponse

    fun revokeRefreshToken(rawRefreshToken: String)

    fun issueTokenPair(
        user: User,
        deviceId: String,
        tokenFamilyId: UUID = UUID.randomUUID(),
        now: Instant = Instant.now(),
    ): TokenResponse
}