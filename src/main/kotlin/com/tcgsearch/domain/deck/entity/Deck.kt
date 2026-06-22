package com.tcgsearch.domain.deck.entity

import com.tcgsearch.domain.card.entity.CardIdentity
import com.tcgsearch.domain.card.entity.CardPrinting
import com.tcgsearch.domain.user.entity.User
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
 * 사용자가 작성한 덱 정보를 저장하는 JPA Entity
 *
 * 리더는 카드 번호 기준으로 검증하고, 사용자가 선택한 인쇄본은 선택 값으로 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "decks")
class Deck(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_identity_id", nullable = false)
    var leaderIdentity: CardIdentity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_printing_id")
    var leaderPrinting: CardPrinting? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var visibility: String = "private",
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
