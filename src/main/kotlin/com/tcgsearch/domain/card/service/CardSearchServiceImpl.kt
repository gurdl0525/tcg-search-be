package com.tcgsearch.domain.card.service

import com.tcgsearch.domain.card.dto.request.CardSearchEventRequest
import com.tcgsearch.domain.card.dto.response.CardColorSummaryResponse
import com.tcgsearch.domain.card.dto.response.CardRaritySummaryResponse
import com.tcgsearch.domain.card.dto.response.CardSearchItemResponse
import com.tcgsearch.domain.card.dto.response.CardSearchPageResponse
import com.tcgsearch.domain.card.dto.response.CardSetSummaryResponse
import com.tcgsearch.domain.card.dto.response.CardTraitSummaryResponse
import com.tcgsearch.domain.card.dto.response.IosCardDetailResponse
import com.tcgsearch.domain.card.dto.response.IosCardPackSummaryResponse
import com.tcgsearch.domain.card.dto.response.IosCardSearchItemResponse
import com.tcgsearch.domain.card.dto.response.IosCardSearchPageResponse
import com.tcgsearch.domain.card.dto.response.IosCardVariantResponse
import com.tcgsearch.domain.card.dto.response.IosCharacterOptionResponse
import com.tcgsearch.domain.card.dto.response.IosFilterOptionsResponse
import com.tcgsearch.domain.card.dto.response.IosIllustratorOptionResponse
import com.tcgsearch.domain.card.dto.response.IosIllustratorSummaryResponse
import com.tcgsearch.domain.card.dto.response.IosListResponse
import com.tcgsearch.domain.card.dto.response.IosMarketplaceLinkResponse
import com.tcgsearch.domain.card.dto.response.IosOptionResponse
import com.tcgsearch.domain.card.dto.response.IosPackOptionResponse
import com.tcgsearch.domain.card.dto.response.IosRelatedPrintingResponse
import com.tcgsearch.domain.card.dto.response.IosTrendingCardResponse
import com.tcgsearch.domain.card.entity.CardSearchEvent
import com.tcgsearch.domain.card.repository.CardColorSearchRow
import com.tcgsearch.domain.card.repository.CardPrintingRepository
import com.tcgsearch.domain.card.repository.CardPrintingSearchCondition
import com.tcgsearch.domain.card.repository.CardPrintingSearchRow
import com.tcgsearch.domain.card.repository.CardRaritySearchRow
import com.tcgsearch.domain.card.repository.CardSearchEventRepository
import com.tcgsearch.domain.card.repository.CardSearchOptionRepository
import com.tcgsearch.domain.card.repository.CardSetSearchRow
import com.tcgsearch.domain.card.repository.CardTraitSearchRow
import com.tcgsearch.domain.card.repository.CharacterOptionRow
import com.tcgsearch.domain.card.repository.IllustratorOptionRow
import com.tcgsearch.domain.card.repository.MarketplaceLinkRow
import com.tcgsearch.domain.card.repository.PackOptionRow
import com.tcgsearch.domain.card.repository.TrendingCardRow
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.global.util.ObjectStorageImageUrlResolver
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CardSearchServiceImpl(
    private val cardPrintings: CardPrintingRepository,
    private val searchOptions: CardSearchOptionRepository,
    private val searchEvents: CardSearchEventRepository,
    private val users: UserRepository,
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
            detailTags = detailTags,
            characterIds = characterIds,
            illustratorIds = illustratorIds,
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

    @Transactional(readOnly = true)
    override fun searchForIos(query: CardSearchQuery): IosCardSearchPageResponse {
        val result = cardPrintings.search(query.toCondition())
        val totalPages = result.totalElements.toTotalPages(query.size)
        val hasNext = query.page + 1 < totalPages

        return IosCardSearchPageResponse(
            content = result.rows.map { it.toIosResponse() },
            page = query.page,
            size = query.size,
            totalCount = result.totalElements,
            hasNext = hasNext,
            nextPage = if (hasNext) query.page + 1 else null,
        )
    }

    override fun filterOptions(language: String): IosFilterOptionsResponse =
        IosFilterOptionsResponse(
            languages = listOf(
                IosOptionResponse(code = "all", name = "전체"),
                IosOptionResponse(code = "ko", name = "한글"),
                IosOptionResponse(code = "en", name = "영어"),
                IosOptionResponse(code = "jp", name = "일본어"),
            ),
            cardTypes = listOf(
                IosOptionResponse(code = "CHARACTER", name = "캐릭터"),
                IosOptionResponse(code = "LEADER", name = "리더"),
                IosOptionResponse(code = "DON", name = "두웅!!"),
                IosOptionResponse(code = "EVENT", name = "이벤트"),
                IosOptionResponse(code = "STAGE", name = "스테이지"),
            ),
            rarities = listOf("C", "UC", "R", "SR", "SEC", "TR", "L", "P", "SP_CARD")
                .map { IosOptionResponse(code = it, name = it) },
            detailTags = listOf("SP", "PARALLEL", "MANGA", "PROMO")
                .map { IosOptionResponse(code = it, name = it) },
            defaultSort = "card_no_desc",
        )

    @Transactional(readOnly = true)
    override fun packs(
        language: String,
        query: String?,
        limit: Int,
    ): IosListResponse<IosPackOptionResponse> =
        IosListResponse(searchOptions.packs(language, query, limit).map { it.toResponse() })

    @Transactional(readOnly = true)
    override fun characters(
        language: String,
        query: String?,
        limit: Int,
    ): IosListResponse<IosCharacterOptionResponse> =
        IosListResponse(searchOptions.characters(language, query, limit).map { it.toResponse() })

    @Transactional(readOnly = true)
    override fun illustrators(
        query: String?,
        limit: Int,
    ): IosListResponse<IosIllustratorOptionResponse> =
        IosListResponse(searchOptions.illustrators(query, limit).map { it.toResponse() })

    @Transactional(readOnly = true)
    override fun trending(
        language: String,
        limit: Int,
        period: String,
    ): IosListResponse<IosTrendingCardResponse> {
        val trendingRows = searchOptions.trending(language, limit, period)
        val cardsByPrintingId = trendingRows
            .mapNotNull { row ->
                cardPrintings.findDetailByPrintingId(row.printingId, language.takeUnless { it == "all" })
                    ?.let { row.printingId to it.toIosResponse() }
            }
            .toMap()

        return IosListResponse(
            trendingRows.mapIndexedNotNull { index, row ->
                cardsByPrintingId[row.printingId]?.let {
                    IosTrendingCardResponse(
                        rank = index + 1,
                        searchCount = row.searchCount,
                        card = it,
                    )
                }
            },
        )
    }

    @Transactional(readOnly = true)
    override fun detail(
        printingId: UUID,
        language: String?,
    ): IosCardDetailResponse =
        cardPrintings.findDetailByPrintingId(printingId, language)
            ?.toDetailResponse()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "card printing not found")

    @Transactional(readOnly = true)
    override fun relatedPrintings(printingId: UUID): IosListResponse<IosRelatedPrintingResponse> =
        IosListResponse(
            cardPrintings.findRelatedPrintings(printingId)
                .map { it.toRelatedResponse() },
        )

    @Transactional(readOnly = true)
    override fun marketplaceLinks(printingId: UUID): IosListResponse<IosMarketplaceLinkResponse> =
        IosListResponse(cardPrintings.findMarketplaceLinks(printingId).map { it.toResponse() })

    @Transactional
    override fun recordSearchEvent(
        request: CardSearchEventRequest,
        userId: UUID?,
    ) {
        searchEvents.save(
            CardSearchEvent(
                user = userId?.let { users.findById(it) },
                eventType = request.eventType,
                query = request.query?.trim()?.takeIf { it.isNotEmpty() },
                language = request.language,
                filters = request.filters.toJsonObject(),
                resultCount = request.resultCount,
                selectedPrinting = request.selectedPrintingId?.let { cardPrintings.findById(it) },
            ),
        )
    }

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

    private fun CardPrintingSearchRow.toIosResponse() =
        IosCardSearchItemResponse(
            printingId = printingId,
            cardIdentityId = cardIdentityId,
            cardNo = cardNo,
            name = name,
            cardType = cardType,
            rarity = rarity?.code,
            language = languageCode,
            colors = colors.map { it.code },
            traits = traits.map { it.name },
            pack = IosCardPackSummaryResponse(
                code = cardSet.code,
                name = cardSet.name,
                releaseDate = releaseDate,
            ),
            variant = variantResponse(),
            imageUrl = imageUrlResolver.resolve(imageUrl),
        )

    private fun CardPrintingSearchRow.toDetailResponse() =
        IosCardDetailResponse(
            printingId = printingId,
            cardIdentityId = cardIdentityId,
            cardNo = cardNo,
            name = name,
            cardType = cardType,
            rarity = rarity?.code,
            language = languageCode,
            colors = colors.map { it.code },
            traits = traits.map { it.name },
            cost = cost,
            life = life,
            power = power,
            counter = counter,
            attribute = attribute,
            effectText = effectText,
            triggerText = triggerText,
            pack = IosCardPackSummaryResponse(
                code = cardSet.code,
                name = cardSet.name,
                releaseDate = releaseDate,
            ),
            variant = variantResponse(),
            illustrator = illustrator?.let {
                IosIllustratorSummaryResponse(
                    id = it.id,
                    name = it.name,
                )
            },
            imageUrl = imageUrlResolver.resolve(imageUrl),
            sourceUrl = sourceUrl,
        )

    private fun CardPrintingSearchRow.toRelatedResponse() =
        IosRelatedPrintingResponse(
            printingId = printingId,
            language = languageCode,
            variant = variantResponse(),
            imageUrl = imageUrlResolver.resolve(imageUrl),
        )

    private fun CardPrintingSearchRow.variantResponse() =
        IosCardVariantResponse(
            isParallel = isParallel,
            detailTags = detailTags,
            displayName = variantName,
        )

    private fun PackOptionRow.toResponse() =
        IosPackOptionResponse(
            code = code,
            name = name,
            releaseDate = releaseDate,
        )

    private fun CharacterOptionRow.toResponse() =
        IosCharacterOptionResponse(
            id = id,
            name = name,
            aliases = aliases,
            cardCount = cardCount,
        )

    private fun IllustratorOptionRow.toResponse() =
        IosIllustratorOptionResponse(
            id = id,
            name = name,
            cardCount = cardCount,
        )

    private fun MarketplaceLinkRow.toResponse() =
        IosMarketplaceLinkResponse(
            provider = provider,
            label = label,
            url = url,
            updatedAt = updatedAt,
        )

    private fun Map<String, List<String>>.toJsonObject(): String =
        entries.joinToString(prefix = "{", postfix = "}") { (key, values) ->
            val jsonValues = values.joinToString(prefix = "[", postfix = "]") { "\"${it.jsonEscaped()}\"" }
            "\"${key.jsonEscaped()}\":$jsonValues"
        }

    private fun String.jsonEscaped(): String =
        buildString {
            this@jsonEscaped.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
}
