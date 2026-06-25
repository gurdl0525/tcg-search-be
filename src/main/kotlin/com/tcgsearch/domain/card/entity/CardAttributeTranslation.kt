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
 * 카드 속성의 언어별 표시 이름을 저장하는 JPA Entity
 *
 * Slash, Strike 같은 canonical 속성에 대해 요청 언어별 표시명을 제공합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@Entity
@Table(name = "attribute_translations")
class CardAttributeTranslation(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    var attribute: CardAttribute,

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
