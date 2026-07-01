package com.tcgsearch.domain.card.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.Tuple
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.tcgsearch.domain.card.entity.CardPrinting
import com.tcgsearch.domain.card.entity.QCardAttributeTranslation
import com.tcgsearch.domain.card.entity.QCardAttributeTranslation.cardAttributeTranslation
import com.tcgsearch.domain.card.entity.QCardIdentity.cardIdentity
import com.tcgsearch.domain.card.entity.QCardIdentityColor.cardIdentityColor
import com.tcgsearch.domain.card.entity.QCardIdentityTrait.cardIdentityTrait
import com.tcgsearch.domain.card.entity.QCardIdentityTranslation
import com.tcgsearch.domain.card.entity.QCardIdentityTranslation.cardIdentityTranslation
import com.tcgsearch.domain.card.entity.QCardIdentityTranslationSearchToken.cardIdentityTranslationSearchToken
import com.tcgsearch.domain.card.entity.QCardPrintingIllustrator.cardPrintingIllustrator
import com.tcgsearch.domain.card.entity.QCardPrinting.cardPrinting
import com.tcgsearch.domain.card.entity.QCardSetTranslation
import com.tcgsearch.domain.card.entity.QCardSetTranslation.cardSetTranslation
import com.tcgsearch.domain.card.entity.QCardTraitTranslation
import com.tcgsearch.domain.card.entity.QCardTraitTranslation.cardTraitTranslation
import com.tcgsearch.domain.marketplace.entity.QExternalCardLink.externalCardLink
import com.tcgsearch.domain.marketplace.entity.QExternalMarketplace.externalMarketplace
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class CustomCardPrintingRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomCardPrintingRepository {

    override fun search(condition: CardPrintingSearchCondition): CardPrintingSearchResult {
        val predicate = predicate(condition)
        val totalElements = count(predicate)
        val printings = contentQuery(predicate)
            .orderBy(*orderSpecifiers(condition))
            .offset((condition.page * condition.size).toLong())
            .limit(condition.size.toLong())
            .fetch()

        return CardPrintingSearchResult(
            rows = printings.toRows(condition.languageCode),
            totalElements = totalElements,
        )
    }

    override fun findDetailByPrintingId(
        printingId: UUID,
        languageCode: String?,
    ): CardPrintingSearchRow? {
        val predicate = BooleanBuilder(cardPrinting.id.eq(printingId))

        return contentQuery(predicate)
            .fetch()
            .toRows(languageCode)
            .firstOrNull()
    }

    override fun findRelatedPrintings(printingId: UUID): List<CardPrintingSearchRow> {
        val identityId = queryFactory
            .select(cardIdentity.id)
            .from(cardPrinting)
            .join(cardPrinting.cardIdentity, cardIdentity)
            .where(cardPrinting.id.eq(printingId))
            .fetchOne()
            ?: return emptyList()

        return contentQuery(BooleanBuilder(cardIdentity.id.eq(identityId)))
            .orderBy(
                cardPrinting.languageCode.asc(),
                cardPrinting.isParallel.desc(),
                cardPrinting.variantName.asc().nullsFirst(),
                cardPrinting.id.asc(),
            )
            .fetch()
            .toRows(null)
    }

    override fun findMarketplaceLinks(printingId: UUID): List<MarketplaceLinkRow> {
        val detail = findDetailByPrintingId(printingId, null) ?: return emptyList()
        val manualLinks = queryFactory
            .select(
                externalMarketplace.code,
                externalMarketplace.name,
                externalCardLink.url,
                externalCardLink.updatedAt,
            )
            .from(externalCardLink)
            .join(externalCardLink.marketplace, externalMarketplace)
            .where(
                externalCardLink.isActive.isTrue
                    .and(
                        externalCardLink.cardPrinting.id.eq(printingId)
                            .or(
                                externalCardLink.cardIdentity.id.eq(detail.cardIdentityId)
                                    .and(externalCardLink.cardPrinting.isNull),
                            ),
                    ),
            )
            .orderBy(externalCardLink.priority.asc(), externalCardLink.updatedAt.desc())
            .fetch()
            .map { tuple ->
                val provider = requireNotNull(tuple.get(externalMarketplace.code)).uppercase()
                MarketplaceLinkRow(
                    provider = provider,
                    label = "${requireNotNull(tuple.get(externalMarketplace.name))} 검색",
                    url = requireNotNull(tuple.get(externalCardLink.url)),
                    updatedAt = requireNotNull(tuple.get(externalCardLink.updatedAt)),
                )
            }

        if (manualLinks.isNotEmpty()) {
            return manualLinks
        }

        return queryFactory
            .select(
                externalMarketplace.code,
                externalMarketplace.name,
                externalMarketplace.searchUrlTemplate,
                externalMarketplace.updatedAt,
            )
            .from(externalMarketplace)
            .where(externalMarketplace.code.equalsIgnoreCase(SNKR_DUNK_MARKETPLACE_CODE))
            .fetch()
            .map { tuple ->
                val provider = requireNotNull(tuple.get(externalMarketplace.code)).uppercase()
                val marketplaceName = requireNotNull(tuple.get(externalMarketplace.name))
                val template = requireNotNull(tuple.get(externalMarketplace.searchUrlTemplate))
                MarketplaceLinkRow(
                    provider = provider,
                    label = "$marketplaceName 검색",
                    url = template.replace("{query}", detail.searchQuery().urlEncoded()),
                    updatedAt = requireNotNull(tuple.get(externalMarketplace.updatedAt)),
                )
            }
    }

    private fun count(predicate: BooleanBuilder): Long =
        queryFactory
            .select(cardPrinting.count())
            .from(cardPrinting)
            .join(cardPrinting.cardIdentity, cardIdentity)
            .join(cardPrinting.cardSet)
            .leftJoin(cardPrinting.rarity)
            .leftJoin(cardIdentityTranslation).on(
                cardIdentityTranslation.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityTranslation.languageCode.eq(cardPrinting.languageCode)),
            )
            .leftJoin(jpCardIdentityTranslation).on(
                jpCardIdentityTranslation.cardIdentity.eq(cardIdentity)
                    .and(jpCardIdentityTranslation.languageCode.eq(DEFAULT_LANGUAGE_CODE)),
            )
            .where(predicate)
            .fetchOne() ?: 0L

    private fun contentQuery(predicate: BooleanBuilder): JPAQuery<Tuple> =
        queryFactory
            .select(
                cardPrinting,
                cardIdentityTranslation.name,
                cardIdentityTranslation.effectText,
                cardIdentityTranslation.triggerText,
                jpCardIdentityTranslation.name,
                jpCardIdentityTranslation.effectText,
                jpCardIdentityTranslation.triggerText,
                cardSetTranslation.name,
                jpCardSetTranslation.name,
                cardAttributeTranslation.name,
                jpCardAttributeTranslation.name,
            )
            .from(cardPrinting)
            .join(cardPrinting.cardIdentity, cardIdentity).fetchJoin()
            .join(cardPrinting.cardSet).fetchJoin()
            .leftJoin(cardPrinting.rarity).fetchJoin()
            .leftJoin(cardIdentity.attribute).fetchJoin()
            .leftJoin(cardIdentityTranslation).on(
                cardIdentityTranslation.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityTranslation.languageCode.eq(cardPrinting.languageCode)),
            )
            .leftJoin(jpCardIdentityTranslation).on(
                jpCardIdentityTranslation.cardIdentity.eq(cardIdentity)
                    .and(jpCardIdentityTranslation.languageCode.eq(DEFAULT_LANGUAGE_CODE)),
            )
            .leftJoin(cardSetTranslation).on(
                cardSetTranslation.cardSet.eq(cardPrinting.cardSet)
                    .and(cardSetTranslation.languageCode.eq(cardPrinting.languageCode)),
            )
            .leftJoin(jpCardSetTranslation).on(
                jpCardSetTranslation.cardSet.eq(cardPrinting.cardSet)
                    .and(jpCardSetTranslation.languageCode.eq(DEFAULT_LANGUAGE_CODE)),
            )
            .leftJoin(cardAttributeTranslation).on(
                cardAttributeTranslation.attribute.eq(cardIdentity.attribute)
                    .and(cardAttributeTranslation.languageCode.eq(cardPrinting.languageCode)),
            )
            .leftJoin(jpCardAttributeTranslation).on(
                jpCardAttributeTranslation.attribute.eq(cardIdentity.attribute)
                    .and(jpCardAttributeTranslation.languageCode.eq(DEFAULT_LANGUAGE_CODE)),
            )
            .where(predicate)

    private fun predicate(condition: CardPrintingSearchCondition): BooleanBuilder {
        val builder = BooleanBuilder()

        condition.searchWord?.let { searchWord ->
            val searchToken = searchWord.toSearchToken()
            val searchPredicate = if (searchToken.isBlank()) {
                cardIdentity.cardNo.containsIgnoreCase(searchWord)
            } else {
                cardIdentity.cardNo.containsIgnoreCase(searchWord)
                    .or(hasSearchToken(searchToken))
            }
            builder.and(searchPredicate)
        }
        condition.cardTypes.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardIdentity.cardType.`in`(it))
        }
        condition.colors.takeIf { it.isNotEmpty() }?.let {
            builder.and(hasColor(it))
        }
        condition.rarities.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardPrinting.rarity.code.`in`(it))
        }
        condition.cardSets.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardPrinting.cardSet.code.`in`(it))
        }
        condition.traits.takeIf { it.isNotEmpty() }?.let {
            builder.and(hasTrait(it))
        }
        condition.attributes.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardIdentity.attribute.name.`in`(it))
        }
        condition.isParallel?.let {
            builder.and(cardPrinting.isParallel.eq(it))
        }
        condition.languageCode?.let {
            builder.and(cardPrinting.languageCode.equalsIgnoreCase(it))
        }
        condition.regionCode?.let {
            builder.and(cardPrinting.regionCode.equalsIgnoreCase(it))
        }
        condition.illustrationTypes.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardPrinting.illustrationType.`in`(it))
        }
        condition.foilTreatments.takeIf { it.isNotEmpty() }?.let {
            builder.and(cardPrinting.foilTreatment.`in`(it))
        }
        condition.blockNo?.let {
            builder.and(cardIdentity.blockNo.eq(it))
        }
        condition.detailTags.takeIf { it.isNotEmpty() }?.let {
            builder.and(hasDetailTag(it))
        }
        condition.characterIds.takeIf { it.isNotEmpty() }?.let {
            builder.and(hasTraitIds(it))
        }
        condition.illustratorIds.takeIf { it.isNotEmpty() }?.let {
            builder.and(hasIllustratorIds(it))
        }

        return builder
    }

    private fun hasColor(colors: Set<String>) =
        JPAExpressions
            .selectOne()
            .from(cardIdentityColor)
            .where(
                cardIdentityColor.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityColor.color.code.`in`(colors)),
            )
            .exists()

    private fun hasTrait(traits: Set<String>) =
        JPAExpressions
            .selectOne()
            .from(cardIdentityTrait)
            .where(
                cardIdentityTrait.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityTrait.trait.name.`in`(traits)),
            )
            .exists()

    private fun hasTraitIds(traitIds: Set<UUID>) =
        JPAExpressions
            .selectOne()
            .from(cardIdentityTrait)
            .where(
                cardIdentityTrait.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityTrait.trait.id.`in`(traitIds)),
            )
            .exists()

    private fun hasIllustratorIds(illustratorIds: Set<UUID>) =
        JPAExpressions
            .selectOne()
            .from(cardPrintingIllustrator)
            .where(
                cardPrintingIllustrator.cardPrinting.eq(cardPrinting)
                    .and(cardPrintingIllustrator.illustrator.id.`in`(illustratorIds)),
            )
            .exists()

    private fun hasDetailTag(detailTags: Set<String>): BooleanBuilder {
        val tagPredicate = BooleanBuilder()
        detailTags.forEach { tag ->
            when (tag.uppercase()) {
                PARALLEL_TAG -> tagPredicate.or(cardPrinting.isParallel.isTrue)
                SP_TAG -> tagPredicate.or(cardPrinting.rarity.code.`in`("SP", "SP_CARD"))
                PROMO_TAG -> tagPredicate.or(cardPrinting.rarity.code.`in`("P", "PROMO"))
                MANGA_TAG -> tagPredicate.or(cardPrinting.variantName.containsIgnoreCase("manga"))
            }
        }
        return tagPredicate
    }

    private fun hasSearchToken(searchToken: String) =
        JPAExpressions
            .selectOne()
            .from(cardIdentityTranslationSearchToken)
            .where(
                cardIdentityTranslationSearchToken.cardIdentity.eq(cardIdentity)
                    .and(cardIdentityTranslationSearchToken.token.eq(searchToken)),
            )
            .exists()

    private fun String.toSearchToken(): String =
        lowercase().replace(SEARCH_TOKEN_SEPARATOR_REGEX, "")

    private fun orderSpecifiers(condition: CardPrintingSearchCondition): Array<OrderSpecifier<*>> {
        val order = if (condition.sort == DESC) Order.DESC else Order.ASC
        val primary = when (condition.sortBy.lowercase()) {
            "name" -> OrderSpecifier(order, cardIdentityTranslation.name)
            "card_type" -> OrderSpecifier(order, cardIdentity.cardType)
            "cost" -> OrderSpecifier(order, cardIdentity.cost)
            "life" -> OrderSpecifier(order, cardIdentity.life)
            "power" -> OrderSpecifier(order, cardIdentity.power)
            "counter" -> OrderSpecifier(order, cardIdentity.counter)
            "block_no" -> OrderSpecifier(order, cardIdentity.blockNo)
            "rarity" -> OrderSpecifier(order, cardPrinting.rarity.code)
            "card_set" -> OrderSpecifier(order, cardPrinting.cardSet.code)
            "variant_name" -> OrderSpecifier(order, cardPrinting.variantName)
            "is_parallel" -> OrderSpecifier(order, cardPrinting.isParallel)
            "created_at" -> OrderSpecifier(order, cardPrinting.createdAt)
            else -> OrderSpecifier(order, cardIdentity.cardNo)
        }

        return arrayOf(
            primary,
            OrderSpecifier(Order.ASC, cardPrinting.id),
        )
    }

    private fun List<Tuple>.toRows(languageCode: String?): List<CardPrintingSearchRow> {
        if (isEmpty()) {
            return emptyList()
        }

        val printings = map { tuple ->
            requireNotNull(tuple.get(cardPrinting)) { "card printing must exist" }
        }
        val identityIds = printings.map { printing ->
            requireNotNull(printing.cardIdentity.id) { "card identity id must exist" }
        }.toSet()
        val printingIds = printings.map { printing ->
            requireNotNull(printing.id) { "card printing id must exist" }
        }.toSet()
        val colorsByIdentityId = colorsByIdentityId(identityIds)
        val traitsByIdentityId = traitsByIdentityId(identityIds, languageCode)
        val illustratorsByPrintingId = illustratorsByPrintingId(printingIds)

        return map { tuple ->
            val printing = requireNotNull(tuple.get(cardPrinting)) { "card printing must exist" }
            val identity = printing.cardIdentity
            val identityId = requireNotNull(identity.id) { "card identity id must exist" }

            CardPrintingSearchRow(
                printingId = requireNotNull(printing.id) { "card printing id must exist" },
                cardIdentityId = identityId,
                cardNo = identity.cardNo,
                name = tuple.get(cardIdentityTranslation.name)
                    ?: tuple.get(jpCardIdentityTranslation.name)
                    ?: identity.name,
                cardType = identity.cardType,
                cost = identity.cost,
                life = identity.life,
                power = identity.power,
                counter = identity.counter,
                attribute = tuple.get(cardAttributeTranslation.name)
                    ?: tuple.get(jpCardAttributeTranslation.name)
                    ?: identity.attribute?.name,
                effectText = tuple.get(cardIdentityTranslation.effectText)
                    ?: tuple.get(jpCardIdentityTranslation.effectText)
                    ?: identity.effectText,
                triggerText = tuple.get(cardIdentityTranslation.triggerText)
                    ?: tuple.get(jpCardIdentityTranslation.triggerText)
                    ?: identity.triggerText,
                blockNo = identity.blockNo,
                colors = colorsByIdentityId[identityId].orEmpty(),
                traits = traitsByIdentityId[identityId].orEmpty(),
                cardSet = CardSetSearchRow(
                    code = printing.cardSet.code,
                    name = tuple.get(cardSetTranslation.name)
                        ?: tuple.get(jpCardSetTranslation.name)
                        ?: printing.cardSet.name,
                ),
                rarity = printing.rarity?.let {
                    CardRaritySearchRow(
                        code = it.code,
                        name = it.name,
                    )
                },
                languageCode = printing.languageCode,
                regionCode = printing.regionCode,
                variantName = printing.variantName,
                isParallel = printing.isParallel,
                foilTreatment = printing.foilTreatment,
                illustrationType = printing.illustrationType,
                imageUrl = printing.imageUrl,
                sourceUrl = printing.sourceUrl,
                detailTags = detailTagsFor(printing.rarity?.code, printing.variantName, printing.isParallel),
                releaseDate = printing.cardSet.releaseDate,
                illustrator = illustratorsByPrintingId[requireNotNull(printing.id)]?.firstOrNull(),
            )
        }
    }

    private fun colorsByIdentityId(identityIds: Set<UUID>): Map<UUID, List<CardColorSearchRow>> =
        queryFactory
            .select(
                cardIdentityColor.cardIdentity.id,
                cardIdentityColor.color.code,
                cardIdentityColor.color.name,
            )
            .from(cardIdentityColor)
            .where(cardIdentityColor.cardIdentity.id.`in`(identityIds))
            .orderBy(cardIdentityColor.color.code.asc())
            .fetch()
            .groupBy(
                { requireNotNull(it.get(cardIdentityColor.cardIdentity.id)) },
                {
                    CardColorSearchRow(
                        code = requireNotNull(it.get(cardIdentityColor.color.code)),
                        name = requireNotNull(it.get(cardIdentityColor.color.name)),
                    )
                },
            )

    private fun traitsByIdentityId(
        identityIds: Set<UUID>,
        languageCode: String?,
    ): Map<UUID, List<CardTraitSearchRow>> =
        queryFactory
            .select(
                cardIdentityTrait.cardIdentity.id,
                cardTraitTranslation.name,
                jpCardTraitTranslation.name,
                cardIdentityTrait.trait.name,
            )
            .from(cardIdentityTrait)
            .leftJoin(cardTraitTranslation).on(
                cardTraitTranslation.trait.eq(cardIdentityTrait.trait)
                    .and(cardTraitTranslation.languageCode.eq(languageCode ?: DEFAULT_LANGUAGE_CODE)),
            )
            .leftJoin(jpCardTraitTranslation).on(
                jpCardTraitTranslation.trait.eq(cardIdentityTrait.trait)
                    .and(jpCardTraitTranslation.languageCode.eq(DEFAULT_LANGUAGE_CODE)),
            )
            .where(cardIdentityTrait.cardIdentity.id.`in`(identityIds))
            .orderBy(
                cardTraitTranslation.name.asc().nullsLast(),
                jpCardTraitTranslation.name.asc().nullsLast(),
                cardIdentityTrait.trait.name.asc(),
            )
            .fetch()
            .groupBy(
                { requireNotNull(it.get(cardIdentityTrait.cardIdentity.id)) },
                {
                    CardTraitSearchRow(
                        name = requireNotNull(
                            it.get(cardTraitTranslation.name)
                                ?: it.get(jpCardTraitTranslation.name)
                                ?: it.get(cardIdentityTrait.trait.name),
                        ),
                    )
                },
            )

    private fun illustratorsByPrintingId(printingIds: Set<UUID>): Map<UUID, List<IllustratorSearchRow>> {
        if (printingIds.isEmpty()) {
            return emptyMap()
        }

        return queryFactory
            .select(
                cardPrintingIllustrator.cardPrinting.id,
                cardPrintingIllustrator.illustrator.id,
                cardPrintingIllustrator.illustrator.name,
            )
            .from(cardPrintingIllustrator)
            .where(cardPrintingIllustrator.cardPrinting.id.`in`(printingIds))
            .orderBy(cardPrintingIllustrator.illustrator.name.asc())
            .fetch()
            .groupBy(
                { requireNotNull(it.get(cardPrintingIllustrator.cardPrinting.id)) },
                {
                    IllustratorSearchRow(
                        id = requireNotNull(it.get(cardPrintingIllustrator.illustrator.id)),
                        name = requireNotNull(it.get(cardPrintingIllustrator.illustrator.name)),
                    )
                },
            )
    }

    private fun detailTagsFor(
        rarityCode: String?,
        variantName: String?,
        isParallel: Boolean,
    ): List<String> = buildList {
        if (isParallel) {
            add(PARALLEL_TAG)
        }
        val normalizedRarity = rarityCode.orEmpty().uppercase()
        val normalizedVariant = variantName.orEmpty().uppercase()
        if (normalizedRarity in setOf("SP", "SP_CARD") || SP_TAG in normalizedVariant) {
            add(SP_TAG)
        }
        if (normalizedRarity in setOf("P", "PROMO") || PROMO_TAG in normalizedVariant) {
            add(PROMO_TAG)
        }
        if (MANGA_TAG in normalizedVariant) {
            add(MANGA_TAG)
        }
    }.distinct()

    private fun CardPrintingSearchRow.searchQuery(): String =
        "$cardNo $name"

    private fun String.urlEncoded(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8)

    private companion object {
        const val DESC = "DESC"
        const val DEFAULT_LANGUAGE_CODE = "jp"
        const val SNKR_DUNK_MARKETPLACE_CODE = "snkrdunk"
        const val PARALLEL_TAG = "PARALLEL"
        const val SP_TAG = "SP"
        const val PROMO_TAG = "PROMO"
        const val MANGA_TAG = "MANGA"

        val jpCardIdentityTranslation = QCardIdentityTranslation("jpCardIdentityTranslation")
        val jpCardSetTranslation = QCardSetTranslation("jpCardSetTranslation")
        val jpCardAttributeTranslation = QCardAttributeTranslation("jpCardAttributeTranslation")
        val jpCardTraitTranslation = QCardTraitTranslation("jpCardTraitTranslation")
        val SEARCH_TOKEN_SEPARATOR_REGEX = Regex("[^\\p{L}\\p{N}]")
    }
}
