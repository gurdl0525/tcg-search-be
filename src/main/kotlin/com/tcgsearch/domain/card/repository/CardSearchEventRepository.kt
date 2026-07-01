package com.tcgsearch.domain.card.repository

import com.tcgsearch.domain.card.entity.CardSearchEvent
import java.util.UUID
import org.springframework.data.repository.Repository

@org.springframework.stereotype.Repository
interface CardSearchEventRepository : Repository<CardSearchEvent, UUID> {
    fun save(event: CardSearchEvent): CardSearchEvent
}
