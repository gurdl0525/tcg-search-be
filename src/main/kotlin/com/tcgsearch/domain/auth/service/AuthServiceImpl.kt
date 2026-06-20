package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.request.LoginRequest
import com.tcgsearch.domain.auth.dto.request.SignUpRequest
import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.global.error.ErrorCode
import com.tcgsearch.global.error.exception.BaseException
import com.tcgsearch.global.property.jwt.RefreshTokenProperties
import com.tcgsearch.global.util.RefreshTokenHasher
import com.tcgsearch.global.util.TokenIssuer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@EnableConfigurationProperties(RefreshTokenProperties::class)
class AuthServiceImpl(
    private val refreshTokenHasher: RefreshTokenHasher,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenIssuer: TokenIssuer,
): AuthService {

    @Transactional(rollbackFor = [Exception::class])
    override fun signUp(request: SignUpRequest): TokenResponse {
        val loginId = request.id!!.trim()

        if (
            users.existsByAuthProviderAndProviderSubject(LOCAL_AUTH_PROVIDER, loginId) ||
            users.existsByEmail(loginId)
        ) {
            throw BaseException(ErrorCode.DUPLICATE_USER_ID)
        }

        val user = users.save(
            User(
                email = loginId,
                displayName = loginId,
                authProvider = LOCAL_AUTH_PROVIDER,
                providerSubject = loginId,
                passwordHash = passwordEncoder.encode(request.password!!),
            ),
        )

        return tokenIssuer.createTokenPair(
            user = user,
            deviceId = request.deviceId!!.trim(),
            tokenFamilyId = UUID.randomUUID(),
            now = Instant.now(),
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun login(request: LoginRequest): TokenResponse {
        val user = users.findByAuthProviderAndProviderSubject(
            authProvider = LOCAL_AUTH_PROVIDER,
            providerSubject = request.id!!.trim(),
        ) ?: throw BaseException(ErrorCode.INVALID_LOGIN_CREDENTIALS)

        val passwordHash = user.passwordHash ?: throw BaseException(ErrorCode.INVALID_LOGIN_CREDENTIALS)

        if (!user.enabled || !passwordEncoder.matches(request.password!!, passwordHash)) {
            throw BaseException(ErrorCode.INVALID_LOGIN_CREDENTIALS)
        }

        return tokenIssuer.createTokenPair(
            user = user,
            deviceId = request.deviceId!!.trim(),
            tokenFamilyId = UUID.randomUUID(),
            now = Instant.now(),
        )
    }

    /**
     * 리프레쉬 토큰으로 새 토큰 발급
     * @param rawRefreshToken [String]
     * @return [TokenResponse]
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = [Exception::class])
    override fun rotateRefreshToken(rawRefreshToken: String): TokenResponse {
        val now = Instant.now()

        val current = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken))
            ?: throw BaseException(ErrorCode.INVALID_REFRESH_TOKEN)

        if (!current.isUsable(now)) {
            refreshTokenRepository.revokeFamily(current.tokenFamilyId, now)
            throw BaseException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        current.apply {
            revokedAt = now
            rotatedAt = now
            updatedAt = now
        }

        return tokenIssuer.createTokenPair(
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
        val refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken)) ?: return

        refreshToken.takeIf{ it.revokedAt == null }?.apply {
            this.revokedAt = now
            this.updatedAt = now
        }
    }

    private companion object {
        const val LOCAL_AUTH_PROVIDER = "local"
    }
}
