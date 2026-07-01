package com.tcgsearch.domain.card.service

import com.tcgsearch.domain.card.dto.request.CardSearchEventRequest
import com.tcgsearch.domain.card.dto.response.IosCardDetailResponse
import com.tcgsearch.domain.card.dto.response.IosCardSearchPageResponse
import com.tcgsearch.domain.card.dto.response.IosCharacterOptionResponse
import com.tcgsearch.domain.card.dto.response.IosFilterOptionsResponse
import com.tcgsearch.domain.card.dto.response.IosIllustratorOptionResponse
import com.tcgsearch.domain.card.dto.response.IosListResponse
import com.tcgsearch.domain.card.dto.response.IosMarketplaceLinkResponse
import com.tcgsearch.domain.card.dto.response.IosPackOptionResponse
import com.tcgsearch.domain.card.dto.response.IosRelatedPrintingResponse
import com.tcgsearch.domain.card.dto.response.IosTrendingCardResponse
import com.tcgsearch.domain.card.dto.response.CardSearchPageResponse
import java.util.UUID

/**
 * 카드 프린팅 검색 유스케이스를 제공합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
interface CardSearchService {
    fun search(query: CardSearchQuery): CardSearchPageResponse

    fun searchForIos(query: CardSearchQuery): IosCardSearchPageResponse

    fun filterOptions(language: String): IosFilterOptionsResponse

    fun packs(
        language: String,
        query: String?,
        limit: Int,
    ): IosListResponse<IosPackOptionResponse>

    fun characters(
        language: String,
        query: String?,
        limit: Int,
    ): IosListResponse<IosCharacterOptionResponse>

    fun illustrators(
        query: String?,
        limit: Int,
    ): IosListResponse<IosIllustratorOptionResponse>

    fun trending(
        language: String,
        limit: Int,
        period: String,
    ): IosListResponse<IosTrendingCardResponse>

    fun detail(
        printingId: UUID,
        language: String?,
    ): IosCardDetailResponse

    fun relatedPrintings(printingId: UUID): IosListResponse<IosRelatedPrintingResponse>

    fun marketplaceLinks(printingId: UUID): IosListResponse<IosMarketplaceLinkResponse>

    fun recordSearchEvent(
        request: CardSearchEventRequest,
        userId: UUID?,
    )
}
