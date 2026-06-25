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
 * 카드 세트의 언어별 표시 이름을 저장하는 JPA Entity
 *
 * 같은 세트 코드에 대해 일본어, 영어, 한국어 표시명을 분리해 관리합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@Entity
@Table(name = "card_set_translations")
class CardSetTranslation(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_set_id", nullable = false)
    var cardSet: CardSet,

    @Column(name = "language_code", nullable = false)
    var languageCode: String,

    @Column(nullable = false)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
