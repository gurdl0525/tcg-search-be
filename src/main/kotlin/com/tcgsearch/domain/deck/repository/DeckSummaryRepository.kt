package com.tcgsearch.domain.deck.repository

import java.time.Instant
import java.util.UUID

interface DeckSummaryRepository {
    fun summary(userId: UUID): DeckSummaryRow
}

data class DeckSummaryRow(
    val deckCount: Long,
    val recentDecks: List<RecentDeckRow>,
)

data class RecentDeckRow(
    val deckId: UUID,
    val name: String,
    val cardCount: Int,
    val leader: DeckLeaderRow?,
    val updatedAt: Instant,
)

data class DeckLeaderRow(
    val printingId: UUID?,
    val name: String,
    val imageUrl: String?,
)
