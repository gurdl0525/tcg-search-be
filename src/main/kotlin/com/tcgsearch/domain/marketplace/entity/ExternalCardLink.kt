package com.tcgsearch.domain.marketplace.entity

import com.tcgsearch.domain.card.entity.CardIdentity
import com.tcgsearch.domain.card.entity.CardPrinting
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
 * 카드와 외부 판매처 URL 사이의 연결을 저장하는 JPA Entity
 *
 * 링크 대상은 카드 번호 기준 또는 인쇄본 기준 중 정확히 하나만 가집니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "external_card_links")
class ExternalCardLink(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marketplace_id", nullable = false)
    var marketplace: ExternalMarketplace,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_identity_id")
    var cardIdentity: CardIdentity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_printing_id")
    var cardPrinting: CardPrinting? = null,

    @Column(nullable = false)
    var url: String,

    @Column(name = "url_type", nullable = false)
    var urlType: String = "manual",

    @Column(nullable = false)
    var priority: Int = 100,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
