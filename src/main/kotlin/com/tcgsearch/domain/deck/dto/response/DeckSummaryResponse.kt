package com.tcgsearch.domain.deck.dto.response

import java.time.Instant
import java.util.UUID

data class DeckSummaryResponse(
    val deckCount: Long,
    val recentDecks: List<RecentDeckResponse>,
)

data class RecentDeckResponse(
    val deckId: UUID,
    val name: String,
    val cardCount: Int,
    val leader: DeckLeaderResponse?,
    val updatedAt: Instant,
)

data class DeckLeaderResponse(
    val printingId: UUID?,
    val name: String,
    val imageUrl: String?,
)
