package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

/**
 * 카드와 타입/소속 조인 테이블의 복합 키를 표현합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Embeddable
data class CardIdentityTraitId(
    @Column(name = "card_identity_id")
    var cardIdentityId: UUID? = null,

    @Column(name = "trait_id")
    var traitId: UUID? = null,
) : Serializable
