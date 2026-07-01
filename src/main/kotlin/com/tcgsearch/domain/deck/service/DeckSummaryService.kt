package com.tcgsearch.domain.deck.service

import com.tcgsearch.domain.deck.dto.response.DeckLeaderResponse
import com.tcgsearch.domain.deck.dto.response.DeckSummaryResponse
import com.tcgsearch.domain.deck.dto.response.RecentDeckResponse
import com.tcgsearch.domain.deck.repository.DeckLeaderRow
import com.tcgsearch.domain.deck.repository.DeckSummaryRepository
import com.tcgsearch.domain.deck.repository.DeckSummaryRow
import com.tcgsearch.domain.deck.repository.RecentDeckRow
import com.tcgsearch.global.util.ObjectStorageImageUrlResolver
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 덱 요약 조회 유스케이스를 제공합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Service
class DeckSummaryService(
    private val deckSummaries: DeckSummaryRepository,
    private val imageUrlResolver: ObjectStorageImageUrlResolver,
) {

    @Transactional(readOnly = true)
    fun summary(userId: UUID): DeckSummaryResponse =
        deckSummaries.summary(userId).toResponse()

    private fun DeckSummaryRow.toResponse() =
        DeckSummaryResponse(
            deckCount = deckCount,
            recentDecks = recentDecks.map { it.toResponse() },
        )

    private fun RecentDeckRow.toResponse() =
        RecentDeckResponse(
            deckId = deckId,
            name = name,
            cardCount = cardCount,
            leader = leader?.toResponse(),
            updatedAt = updatedAt,
        )

    private fun DeckLeaderRow.toResponse() =
        DeckLeaderResponse(
            printingId = printingId,
            name = name,
            imageUrl = imageUrlResolver.resolve(imageUrl),
        )
}
