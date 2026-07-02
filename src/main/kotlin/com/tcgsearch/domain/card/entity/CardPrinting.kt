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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * 카드의 실제 인쇄본을 저장하는 JPA Entity
 *
 * 같은 카드 번호라도 세트, 레어도, 언어, 패러렐 여부가 다른 수집 단위를 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "card_printings")
class CardPrinting(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_set_id", nullable = false)
    var cardSet: CardSet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity_id")
    var rarity: CardRarity? = null,

    @Column(name = "language_code", nullable = false)
    var languageCode: String,

    @Column(name = "region_code")
    var regionCode: String? = null,

    @Column(name = "variant_name")
    var variantName: String? = null,

    @Column(name = "is_parallel", nullable = false)
    var isParallel: Boolean = false,

    @Column(name = "foil_treatment")
    var foilTreatment: String? = null,

    @Column(name = "illustration_type")
    var illustrationType: String? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "detail_tags", nullable = false, columnDefinition = "text[]")
    var detailTags: Array<String> = emptyArray(),

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "source_url")
    var sourceUrl: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
