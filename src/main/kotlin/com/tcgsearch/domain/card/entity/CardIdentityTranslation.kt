package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * 카드 식별자의 언어별 표시 텍스트를 저장하는 JPA Entity
 *
 * 카드 이름, 효과, 트리거 텍스트처럼 언어마다 달라지는 값을 관리합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@Entity
@Table(name = "card_identity_translations")
class CardIdentityTranslation(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @Column(name = "language_code", nullable = false)
    var languageCode: String,

    @Column(nullable = false)
    var name: String,

    @Column(name = "effect_text")
    var effectText: String? = null,

    @Column(name = "trigger_text")
    var triggerText: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
