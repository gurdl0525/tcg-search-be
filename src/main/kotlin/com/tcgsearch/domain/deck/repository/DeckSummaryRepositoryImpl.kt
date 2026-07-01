package com.tcgsearch.domain.deck.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.tcgsearch.domain.deck.entity.QDeck.deck
import com.tcgsearch.domain.deck.entity.QDeckCard.deckCard
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class DeckSummaryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : DeckSummaryRepository {

    override fun summary(userId: UUID): DeckSummaryRow {
        val deckRows = queryFactory
            .selectFrom(deck)
            .leftJoin(deck.leaderPrinting).fetchJoin()
            .leftJoin(deck.leaderIdentity).fetchJoin()
            .where(deck.user.id.eq(userId))
            .orderBy(deck.updatedAt.desc())
            .limit(10)
            .fetch()

        return DeckSummaryRow(
            deckCount = queryFactory
                .select(deck.count())
                .from(deck)
                .where(deck.user.id.eq(userId))
                .fetchOne() ?: 0L,
            recentDecks = deckRows.map { deckEntity ->
                val deckId = requireNotNull(deckEntity.id)
                RecentDeckRow(
                    deckId = deckId,
                    name = deckEntity.name,
                    cardCount = cardCount(deckId),
                    leader = DeckLeaderRow(
                        printingId = deckEntity.leaderPrinting?.id,
                        name = deckEntity.leaderIdentity.name,
                        imageUrl = deckEntity.leaderPrinting?.imageUrl,
                    ),
                    updatedAt = deckEntity.updatedAt,
                )
            },
        )
    }

    private fun cardCount(deckId: UUID): Int =
        queryFactory
            .select(deckCard.quantity.sum())
            .from(deckCard)
            .where(deckCard.deck.id.eq(deckId))
            .fetchOne() ?: 0
}
