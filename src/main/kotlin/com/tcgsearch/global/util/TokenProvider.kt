package com.tcgsearch.global.util

import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.global.error.ErrorCode
import com.tcgsearch.global.error.exception.BaseException
import com.tcgsearch.global.property.JwtProperties
import com.tcgsearch.global.property.RefreshTokenProperties
import com.tcgsearch.global.util.dto.JwtUser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class TokenProvider(
    private val jwtProperties: JwtProperties,
    private val refreshTokenProperties: RefreshTokenProperties,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private val secureRandom = SecureRandom()
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    /**
     * 토큰 생성 Pair(access, refresh)
     * @param user [User]
     * @param deviceId [String]
     * @param tokenFamilyId [UUID]
     * @param now [Instant]
     * @return [TokenResponse]
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

    /**
     * 어쎄스 토큰 생성
     * @param user [User]
     * @param now [Instant]
     */
    private fun createAccessToken(user: User, now: Instant) = Jwts.builder()
        .signWith(secretKey, Jwts.SIG.HS256)
        .issuer(jwtProperties.issuer)
        .issuedAt(Date.from(now))
        .subject(user.id.toString())
        .claim("email", user.email)
        .claim("role", user.role)
        .expiration(Date.from(now.plus(jwtProperties.accessTokenTtl)))
        .compact()

    /**
     * 어쎄스 토큰 파싱
     * @param token [String]
     * @return [JwtUser]
     */
    fun parseAccessToken(token: String): JwtUser {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(jwtProperties.issuer)
            .build()
            .parseSignedClaims(token)
            .payload

        return JwtUser(
            userId = claims.subject,
            role = claims["role"]?.toString()
        )
    }

    /**
     * 리프레쉬 토큰 생성
     * @return [String]
     */
    private fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * 토큰 파싱
     * @param request [HttpServletRequest]
     * @return [String]
     */
    fun resolveToken(request: HttpServletRequest): String? = request.getHeader("Authorization")
        .takeIf { it.startsWith("Bearer ") }?.substring(7)
        ?: throw BaseException(ErrorCode.NO_AUTHENTICATION)
}