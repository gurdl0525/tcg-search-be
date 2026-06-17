package com.tcgsearch.global.util

import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.global.property.JwtProperties
import com.tcgsearch.global.property.RefreshTokenProperties
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 토큰 쌍을 발급합니다.
 *
 * Access token은 Spring Security `JwtEncoder`로 발급하고 refresh token은 해시로 저장합니다.
 *
 * @author gurdl0525
 * @since 18-06-2026
 */
@Component
class TokenIssuer(
    private val jwtProperties: JwtProperties,
    private val refreshTokenProperties: RefreshTokenProperties,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtEncoder: JwtEncoder,
) {
    private val secureRandom = SecureRandom()

    /**
     * 토큰 쌍을 발급하고 refresh token 해시를 저장합니다.
     *
     * @param user 발급 대상 사용자
     * @param deviceId refresh token이 묶일 클라이언트 장치 식별자
     * @param tokenFamilyId refresh token rotation family 식별자
     * @param now 발급 기준 시각
     * @return access token과 원문 refresh token
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    fun createTokenPair(
        user: User,
        deviceId: String,
        tokenFamilyId: UUID,
        now: Instant,
    ): TokenResponse {
        val rawRefreshToken = generateRefreshToken()

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                deviceId = deviceId,
                tokenHash = refreshTokenHasher.hash(rawRefreshToken),
                tokenFamilyId = tokenFamilyId,
                expiresAt = now.plus(refreshTokenProperties.ttl),
            ),
        )

        return TokenResponse(
            accessToken = createAccessToken(user, now),
            refreshToken = rawRefreshToken,
            expiresIn = jwtProperties.accessTokenTtl.seconds,
        )
    }

    private fun createAccessToken(user: User, now: Instant): String {
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.accessTokenTtl))
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("role", user.role)
            .build()

        val headers = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder
            .encode(JwtEncoderParameters.from(headers, claims))
            .tokenValue
    }

    private fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
