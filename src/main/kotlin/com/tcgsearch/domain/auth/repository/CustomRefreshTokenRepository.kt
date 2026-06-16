package com.tcgsearch.domain.auth.repository

import java.time.Instant
import java.util.*

interface CustomRefreshTokenRepository {
    fun revokeFamily(tokenFamilyId: UUID, revokedAt: Instant): Boolean
}