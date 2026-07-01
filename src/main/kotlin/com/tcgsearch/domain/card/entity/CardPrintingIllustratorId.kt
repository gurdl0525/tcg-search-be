package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
class CardPrintingIllustratorId(
    @Column(name = "card_printing_id")
    var cardPrintingId: UUID? = null,

    @Column(name = "illustrator_id")
    var illustratorId: UUID? = null,
) : Serializable
