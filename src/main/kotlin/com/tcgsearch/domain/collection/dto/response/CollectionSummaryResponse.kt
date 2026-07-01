package com.tcgsearch.domain.collection.dto.response

import java.util.UUID

data class CollectionSummaryResponse(
    val totalCards: Long,
    val uniqueIdentities: Long,
    val parallelCards: Long,
    val recentCards: List<CollectionRecentCardResponse>,
)

data class CollectionRecentCardResponse(
    val printingId: UUID,
    val cardNo: String,
    val name: String,
    val imageUrl: String?,
)
