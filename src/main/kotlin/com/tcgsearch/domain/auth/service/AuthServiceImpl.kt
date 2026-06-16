package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.global.property.SecurityJwtProperties
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.global.util.RefreshTokenHasher
import com.tcgsearch.global.property.RefreshTokenProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Date

@Service
@EnableConfigurationProperties(RefreshTokenProperties::class)
class AuthServiceImpl(
    private val jwtProperties: SecurityJwtProperties,
    private val refreshTokenProperties: RefreshTokenProperties,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val refreshTokens: RefreshTokenRepository,
): AuthService {
    private val secureRandom = SecureRandom()

    /**
     * 리프레쉬 토큰으로 새 토큰 발급
     * @param rawRefreshToken [String]
     * @return [TokenResponse]
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = [Exception::class])
    override fun rotateRefreshToken(rawRefreshToken: String): TokenResponse {
        val now = Instant.now()

        val current = refreshTokens.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken))
            ?: throw invalidRefreshToken()

        current.takeIf { it.isUsable(now) }?.apply {
            this.revokedAt = now
            this.rotatedAt = now
            this.updatedAt = now
        } ?: {
            refreshTokens.revokeFamily(current.tokenFamilyId, now)
            throw invalidRefreshToken()
        }

        return issueTokenPair(
            user = current.user,
            deviceId = current.deviceId,
            tokenFamilyId = current.tokenFamilyId,
            now = now,
        )
    }

    /**
     * 리프레쉬 토큰 만료하기
     * @param rawRefreshToken [String]
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = [Exception::class])
    override fun revokeRefreshToken(rawRefreshToken: String) {
        val now = Instant.now()
        val refreshToken = refreshTokens.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken)) ?: return

        refreshToken.takeIf{ it.revokedAt == null }?.apply {
            this.revokedAt = now
            this.updatedAt = now
        }
    }

    /**
     * 토큰 생성 Pair(access, refresh)
     * @param user [User]
     * @param deviceId [String]
     * @param tokenFamilyId [UUID]
     * @param now [Instant]
     * @return [TokenResponse]
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    override fun issueTokenPair(
        user: User,
        deviceId: String,
        tokenFamilyId: UUID,
        now: Instant,
    ): TokenResponse {
        val rawRefreshToken = generateRefreshToken()

        refreshTokens.save(
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
        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret)), Jwts.SIG.HS256)
        .issuer(jwtProperties.issuer)
        .issuedAt(Date.from(now))
        .subject(user.id.toString())
        .claim("email", user.email)
        .claim("role", user.role)
        .expiration(Date.from(now.plus(jwtProperties.accessTokenTtl)))
        .compact()

    /**
     * 리프레쉬 토큰 생성
     * @return [String]
     */
    private fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun invalidRefreshToken(): ResponseStatusException {
        return ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
    }
}
