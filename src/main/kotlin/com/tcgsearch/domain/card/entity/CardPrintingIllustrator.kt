package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.time.Instant

/**
 * 카드 프린팅과 일러스트레이터의 연결을 저장하는 JPA Entity
 *
 * 하나의 인쇄본에 여러 작가가 연결될 수 있도록 join table을 명시적으로 매핑합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Entity
@Table(name = "card_printing_illustrators")
class CardPrintingIllustrator(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("cardPrintingId")
    @JoinColumn(name = "card_printing_id", nullable = false)
    var cardPrinting: CardPrinting,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("illustratorId")
    @JoinColumn(name = "illustrator_id", nullable = false)
    var illustrator: Illustrator,
) {
    @EmbeddedId
    var id: CardPrintingIllustratorId = CardPrintingIllustratorId(
        cardPrintingId = cardPrinting.id,
        illustratorId = illustrator.id,
    )

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
