package com.tcgsearch.domain.collection.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.tcgsearch.domain.collection.entity.QUserCollectionEntry.userCollectionEntry
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CollectionSummaryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CollectionSummaryRepository {

    override fun summary(userId: UUID): CollectionSummaryRow {
        val entries = queryFactory
            .selectFrom(userCollectionEntry)
            .join(userCollectionEntry.cardPrinting).fetchJoin()
            .join(userCollectionEntry.cardPrinting.cardIdentity).fetchJoin()
            .where(userCollectionEntry.user.id.eq(userId))
            .orderBy(userCollectionEntry.updatedAt.desc())
            .fetch()

        return CollectionSummaryRow(
            totalCards = entries.sumOf { it.quantity.toLong() },
            uniqueIdentities = entries.mapNotNull { it.cardPrinting.cardIdentity.id }.toSet().size.toLong(),
            parallelCards = entries.filter { it.cardPrinting.isParallel }.sumOf { it.quantity.toLong() },
            recentCards = entries.take(10).map { entry ->
                val printing = entry.cardPrinting
                val identity = printing.cardIdentity
                CollectionRecentCardRow(
                    printingId = requireNotNull(printing.id),
                    cardNo = identity.cardNo,
                    name = identity.name,
                    imageUrl = printing.imageUrl,
                )
            },
        )
    }
}
