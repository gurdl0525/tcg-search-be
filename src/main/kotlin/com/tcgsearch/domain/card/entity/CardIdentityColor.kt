package com.tcgsearch.domain.card.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

/**
 * 카드와 색상 사이의 다대다 관계를 저장하는 JPA Entity
 *
 * 다색 카드 검색과 리더 색상 기반 덱 검증에 사용됩니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "card_identity_colors")
class CardIdentityColor(
    @EmbeddedId
    var id: CardIdentityColorId,

    @MapsId("cardIdentityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @MapsId("colorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id", nullable = false)
    var color: CardColor,
)
