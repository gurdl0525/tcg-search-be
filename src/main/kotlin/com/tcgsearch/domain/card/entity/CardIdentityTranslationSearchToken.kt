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
 * 카드 번역 검색용 파생 토큰을 저장하는 JPA Entity
 *
 * 크롤링 시점에 생성한 정규화, prefix, 초성, n-gram 토큰을 저장해 짧은 검색어도 인덱스로 조회합니다.
 *
 * @author gurdl0525
 * @since 29-06-2026
 */
@Entity
@Table(name = "card_identity_translation_search_tokens")
class CardIdentityTranslationSearchToken(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_translation_id", nullable = false)
    var cardIdentityTranslation: CardIdentityTranslation,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @Column(name = "language_code", nullable = false)
    var languageCode: String,

    @Column(name = "source_field", nullable = false)
    var sourceField: String,

    @Column(name = "token_type", nullable = false)
    var tokenType: String,

    @Column(nullable = false)
    var token: String,

    @Column(nullable = false)
    var weight: Short = 1,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
