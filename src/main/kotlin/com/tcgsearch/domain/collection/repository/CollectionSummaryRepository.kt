package com.tcgsearch.domain.collection.repository

import java.util.UUID

interface CollectionSummaryRepository {
    fun summary(userId: UUID): CollectionSummaryRow
}

data class CollectionSummaryRow(
    val totalCards: Long,
    val uniqueIdentities: Long,
    val parallelCards: Long,
    val recentCards: List<CollectionRecentCardRow>,
)

data class CollectionRecentCardRow(
    val printingId: UUID,
    val cardNo: String,
    val name: String,
    val imageUrl: String?,
)
