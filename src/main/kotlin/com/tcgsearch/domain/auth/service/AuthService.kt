package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.response.TokenResponse

interface AuthService {
    fun rotateRefreshToken(rawRefreshToken: String): TokenResponse

    fun revokeRefreshToken(rawRefreshToken: String)
}