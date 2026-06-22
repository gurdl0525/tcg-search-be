package com.tcgsearch.domain.card.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

/**
 * 카드와 타입/소속 사이의 다대다 관계를 저장하는 JPA Entity
 *
 * 복수 타입을 가진 카드의 검색 조건과 필터링에 사용됩니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "card_identity_traits")
class CardIdentityTrait(
    @EmbeddedId
    var id: CardIdentityTraitId,

    @MapsId("cardIdentityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @MapsId("traitId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trait_id", nullable = false)
    var trait: CardTrait,
)
