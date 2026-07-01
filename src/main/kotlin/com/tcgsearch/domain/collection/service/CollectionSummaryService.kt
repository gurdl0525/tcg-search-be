package com.tcgsearch.domain.collection.service

import com.tcgsearch.domain.collection.dto.response.CollectionRecentCardResponse
import com.tcgsearch.domain.collection.dto.response.CollectionSummaryResponse
import com.tcgsearch.domain.collection.repository.CollectionRecentCardRow
import com.tcgsearch.domain.collection.repository.CollectionSummaryRepository
import com.tcgsearch.domain.collection.repository.CollectionSummaryRow
import com.tcgsearch.global.util.ObjectStorageImageUrlResolver
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 컬렉션 요약 조회 유스케이스를 제공합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Service
class CollectionSummaryService(
    private val collectionSummaries: CollectionSummaryRepository,
    private val imageUrlResolver: ObjectStorageImageUrlResolver,
) {

    @Transactional(readOnly = true)
    fun summary(userId: UUID): CollectionSummaryResponse =
        collectionSummaries.summary(userId).toResponse()

    private fun CollectionSummaryRow.toResponse() =
        CollectionSummaryResponse(
            totalCards = totalCards,
            uniqueIdentities = uniqueIdentities,
            parallelCards = parallelCards,
            recentCards = recentCards.map { it.toResponse() },
        )

    private fun CollectionRecentCardRow.toResponse() =
        CollectionRecentCardResponse(
            printingId = printingId,
            cardNo = cardNo,
            name = name,
            imageUrl = imageUrlResolver.resolve(imageUrl),
        )
}
