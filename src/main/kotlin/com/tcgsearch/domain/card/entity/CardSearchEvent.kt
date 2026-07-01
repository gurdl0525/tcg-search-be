package com.tcgsearch.domain.card.entity

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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * 카드 검색과 카드 열람 이벤트를 저장하는 JPA Entity
 *
 * 홈의 최근 많이 찾는 카드와 향후 개인화 추천의 원천 데이터로 사용합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Entity
@Table(name = "card_search_events")
class CardSearchEvent(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(name = "event_type", nullable = false)
    var eventType: String,

    @Column
    var query: String? = null,

    @Column(nullable = false)
    var language: String = "all",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var filters: String = "{}",

    @Column(name = "result_count")
    var resultCount: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_printing_id")
    var selectedPrinting: CardPrinting? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
