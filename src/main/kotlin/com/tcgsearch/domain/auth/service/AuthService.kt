package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.request.LoginRequest
import com.tcgsearch.domain.auth.dto.request.SignUpRequest
import com.tcgsearch.domain.auth.dto.response.TokenResponse

/**
 * 인증 토큰 발급과 refresh token 생명주기 유스케이스를 정의합니다.
 *
 * local credential 인증과 refresh token rotation/revoke 정책을 같은 경계에서 다룹니다.
 *
 * @author gurdl0525
 * @since 18-06-2026
 */
interface AuthService {
    /**
     * local 사용자를 생성하고 토큰 쌍을 발급합니다.
     *
     * @param request local id, password, device id
     * @return 새 access token과 refresh token
     */
    fun signUp(request: SignUpRequest): TokenResponse

    /**
     * local credential을 검증하고 토큰 쌍을 발급합니다.
     *
     * @param request local id, password, device id
     * @return 새 access token과 refresh token
     */
    fun login(request: LoginRequest): TokenResponse

    fun rotateRefreshToken(rawRefreshToken: String): TokenResponse

    fun revokeRefreshToken(rawRefreshToken: String)
}
