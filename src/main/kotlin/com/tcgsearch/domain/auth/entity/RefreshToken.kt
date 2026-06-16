package com.tcgsearch.domain.auth.entity

import com.tcgsearch.domain.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID
import java.time.Instant

/**
 * Native App 장기 인증을 위한 refresh token 저장 JPA Entity
 *
 * 원문 refresh token은 저장하지 않고 해시만 저장합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
@Entity
@Table(name = "user_refresh_tokens")
class RefreshToken(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "device_id", nullable = false)
    var deviceId: String,

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String,

    @Column(name = "token_family_id", nullable = false)
    var tokenFamilyId: UUID,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
) {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null

    @Column(name = "rotated_at")
    var rotatedAt: Instant? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    /**
     * revoke 여부
     * @return [Boolean]
     */
    private fun isRevoked(): Boolean = this.revokedAt != null

    /**
     * 로테이션 여부
     * @return [Boolean]
     */
    private fun isRotated(): Boolean = this.rotatedAt != null

    /**
     * 만료 여부
     * @param now [Instant]
     * @return [Boolean]
     */
    private fun isExpired(now: Instant) = !expiresAt.isAfter(now)

    /**
     * 사용 가능 여부
     * @param now [Instant]
     * @return [Boolean]
     */
    fun isUsable(now: Instant) = !isRevoked() && !isRotated() && !isExpired(now)
}