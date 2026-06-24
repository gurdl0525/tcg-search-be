package com.tcgsearch.domain.card.service

import com.tcgsearch.domain.card.dto.response.CardColorSummaryResponse
import com.tcgsearch.domain.card.dto.response.CardRaritySummaryResponse
import com.tcgsearch.domain.card.dto.response.CardSearchItemResponse
import com.tcgsearch.domain.card.dto.response.CardSearchPageResponse
import com.tcgsearch.domain.card.dto.response.CardSetSummaryResponse
import com.tcgsearch.domain.card.dto.response.CardTraitSummaryResponse
import com.tcgsearch.domain.card.repository.CardColorSearchRow
import com.tcgsearch.domain.card.repository.CardPrintingRepository
import com.tcgsearch.domain.card.repository.CardPrintingSearchCondition
import com.tcgsearch.domain.card.repository.CardPrintingSearchRow
import com.tcgsearch.domain.card.repository.CardRaritySearchRow
import com.tcgsearch.domain.card.repository.CardSetSearchRow
import com.tcgsearch.domain.card.repository.CardTraitSearchRow
import com.tcgsearch.global.util.ObjectStorageImageUrlResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardSearchServiceImpl(
    private val cardPrintings: CardPrintingRepository,
    private val imageUrlResolver: ObjectStorageImageUrlResolver,
) : CardSearchService {

    @Transactional(readOnly = true)
    override fun search(query: CardSearchQuery): CardSearchPageResponse {
        val result = cardPrintings.search(query.toCondition())
        val totalPages = result.totalElements.toTotalPages(query.size)

        return CardSearchPageResponse(
            content = result.rows.map { it.toResponse() },
            page = query.page,
            size = query.size,
            totalElements = result.totalElements,
            totalPages = totalPages,
            hasNext = query.page + 1 < totalPages,
        )
    }

    private fun CardSearchQuery.toCondition() =
        CardPrintingSearchCondition(
            page = page,
            size = size,
            searchWord = searchWord,
            sortBy = sortBy,
            sort = sort,
            cardTypes = cardTypes,
            colors = colors,
            rarities = rarities,
            cardSets = cardSets,
            traits = traits,
            attributes = attributes,
            isParallel = isParallel,
            languageCode = languageCode,
            regionCode = regionCode,
            illustrationTypes = illustrationTypes,
            foilTreatments = foilTreatments,
            blockNo = blockNo,
        )

    private fun Long.toTotalPages(size: Int): Int =
        if (this == 0L) {
            0
        } else {
            ((this + size - 1) / size).toInt()
        }

    private fun CardPrintingSearchRow.toResponse() =
        CardSearchItemResponse(
            printingId = printingId,
            cardIdentityId = cardIdentityId,
            cardNo = cardNo,
            name = name,
            cardType = cardType,
            cost = cost,
            life = life,
            power = power,
            counter = counter,
            attribute = attribute,
            effectText = effectText,
            triggerText = triggerText,
            blockNo = blockNo,
            colors = colors.map { it.toResponse() },
            traits = traits.map { it.toResponse() },
            cardSet = cardSet.toResponse(),
            rarity = rarity?.toResponse(),
            languageCode = languageCode,
            regionCode = regionCode,
            variantName = variantName,
            isParallel = isParallel,
            foilTreatment = foilTreatment,
            illustrationType = illustrationType,
            imageUrl = imageUrlResolver.resolve(imageUrl),
            sourceUrl = sourceUrl,
        )

    private fun CardColorSearchRow.toResponse() =
        CardColorSummaryResponse(
            code = code,
            name = name,
        )

    private fun CardTraitSearchRow.toResponse() =
        CardTraitSummaryResponse(name = name)

    private fun CardSetSearchRow.toResponse() =
        CardSetSummaryResponse(
            code = code,
            name = name,
        )

    private fun CardRaritySearchRow.toResponse() =
        CardRaritySummaryResponse(
            code = code,
            name = name,
        )

}
