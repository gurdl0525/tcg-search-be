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
 * 카드 번호 기준의 게임상 동일 카드를 저장하는 JPA Entity
 *
 * 인쇄본이 달라도 덱 제한과 검색 기준이 되는 카드 고유 정보를 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "card_identities")
class CardIdentity(
    @Column(name = "card_no", nullable = false, unique = true)
    var cardNo: String,

    @Column(nullable = false)
    var name: String,

    @Column(name = "card_type", nullable = false)
    var cardType: String,

    @Column
    var cost: Short? = null,

    @Column
    var life: Short? = null,

    @Column
    var power: Int? = null,

    @Column
    var counter: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    var attribute: CardAttribute? = null,

    @Column(name = "effect_text")
    var effectText: String? = null,

    @Column(name = "trigger_text")
    var triggerText: String? = null,

    @Column(name = "block_no")
    var blockNo: Int? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
