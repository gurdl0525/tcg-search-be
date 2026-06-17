package com.tcgsearch.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * 애플리케이션 사용자를 저장하기 위한 JPA Entity
 *
 * 인증 제공자와 제공자 내 식별자를 함께 저장해 이메일 변경과 외부 인증 제공자 확장에 대비합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
@Entity
@Table(name = "app_users")
class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "auth_provider", nullable = false)
    var authProvider: String,

    @Column(name = "provider_subject", nullable = false)
    var providerSubject: String,

    @Column(name = "password_hash")
    var passwordHash: String? = null,

    @Column(nullable = false)
    var role: String = "USER",

    @Column(nullable = false)
    var enabled: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
