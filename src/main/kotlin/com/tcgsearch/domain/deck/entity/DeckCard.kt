package com.tcgsearch.domain.deck.entity

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
 * 덱에 포함된 카드와 수량을 저장하는 JPA Entity
 *
 * 덱 제한은 카드 번호 기준으로 적용하고, 선호 인쇄본은 선택 값으로 보관합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "deck_cards")
class DeckCard(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    var deck: Deck,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    var cardIdentity: CardIdentity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_printing_id")
    var preferredPrinting: CardPrinting? = null,

    @Column(nullable = false)
    var quantity: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
