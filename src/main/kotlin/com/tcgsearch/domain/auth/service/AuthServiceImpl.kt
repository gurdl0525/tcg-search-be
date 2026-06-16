package com.tcgsearch.domain.auth.service

import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.global.error.ErrorCode
import com.tcgsearch.global.error.exception.BaseException
import com.tcgsearch.global.property.RefreshTokenProperties
import com.tcgsearch.global.util.RefreshTokenHasher
import com.tcgsearch.global.util.TokenProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@EnableConfigurationProperties(RefreshTokenProperties::class)
class AuthServiceImpl(
    private val refreshTokenHasher: RefreshTokenHasher,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: TokenProvider,
): AuthService {

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

        current.takeIf { it.isUsable(now) }?.apply {
            this.revokedAt = now
            this.rotatedAt = now
            this.updatedAt = now
        } ?: {
            refreshTokenRepository.revokeFamily(current.tokenFamilyId, now)
            throw BaseException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        return jwtProvider.createTokenPair(
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
}
