package com.tcgsearch.domain.collection.entity

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
 * 사용자가 보유한 카드 인쇄본 수량을 저장하는 JPA Entity
 *
 * 컬렉션은 카드 번호가 아니라 실제 인쇄본과 상태 기준으로 수량을 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "user_collection_entries")
class UserCollectionEntry(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_printing_id", nullable = false)
    var cardPrinting: CardPrinting,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var condition: String = "unspecified",

    @Column
    var note: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
