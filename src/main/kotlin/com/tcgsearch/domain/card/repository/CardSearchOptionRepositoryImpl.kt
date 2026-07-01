package com.tcgsearch.domain.card.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.tcgsearch.domain.card.entity.QCardPrinting.cardPrinting
import com.tcgsearch.domain.card.entity.QCardPrintingIllustrator.cardPrintingIllustrator
import com.tcgsearch.domain.card.entity.QCardSearchEvent.cardSearchEvent
import com.tcgsearch.domain.card.entity.QCardSet.cardSet
import com.tcgsearch.domain.card.entity.QCardSetTranslation
import com.tcgsearch.domain.card.entity.QCardSetTranslation.cardSetTranslation
import com.tcgsearch.domain.card.entity.QCardTrait.cardTrait
import com.tcgsearch.domain.card.entity.QCardTraitTranslation
import com.tcgsearch.domain.card.entity.QCardTraitTranslation.cardTraitTranslation
import com.tcgsearch.domain.card.entity.QIllustrator.illustrator
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.stereotype.Repository

@Repository
class CardSearchOptionRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CardSearchOptionRepository {

    override fun packs(
        language: String,
        query: String?,
        limit: Int,
    ): List<PackOptionRow> {
        val predicate = BooleanBuilder()
        query?.let {
            predicate.and(
                cardSet.code.containsIgnoreCase(it)
                    .or(cardSet.name.containsIgnoreCase(it))
                    .or(cardSetTranslation.name.containsIgnoreCase(it)),
            )
        }

        return queryFactory
            .select(
                cardSet.code,
                cardSetTranslation.name,
                jpCardSetTranslation.name,
                cardSet.name,
                cardSet.releaseDate,
            )
            .from(cardSet)
            .leftJoin(cardSetTranslation).on(
                cardSetTranslation.cardSet.eq(cardSet)
                    .and(cardSetTranslation.languageCode.eq(language.takeUnless { it == ALL_LANGUAGE } ?: DEFAULT_LANGUAGE)),
            )
            .leftJoin(jpCardSetTranslation).on(
                jpCardSetTranslation.cardSet.eq(cardSet)
                    .and(jpCardSetTranslation.languageCode.eq(DEFAULT_LANGUAGE)),
            )
            .where(predicate)
            .orderBy(cardSet.code.asc())
            .limit(limit.toLong())
            .fetch()
            .map {
                PackOptionRow(
                    code = requireNotNull(it.get(cardSet.code)),
                    name = requireNotNull(
                        it.get(cardSetTranslation.name)
                            ?: it.get(jpCardSetTranslation.name)
                            ?: it.get(cardSet.name),
                    ),
                    releaseDate = it.get(cardSet.releaseDate),
                )
            }
    }

    override fun characters(
        language: String,
        query: String?,
        limit: Int,
    ): List<CharacterOptionRow> {
        val displayLanguage = language.takeUnless { it == ALL_LANGUAGE } ?: DEFAULT_LANGUAGE
        val predicate = BooleanBuilder()
        query?.let {
            predicate.and(
                cardTrait.name.containsIgnoreCase(it)
                    .or(cardTraitTranslation.name.containsIgnoreCase(it))
                    .or(jpCardTraitTranslation.name.containsIgnoreCase(it)),
            )
        }

        return queryFactory
            .select(
                cardTrait.id,
                cardTraitTranslation.name,
                jpCardTraitTranslation.name,
                cardTrait.name,
                cardPrinting.countDistinct(),
            )
            .from(cardTrait)
            .leftJoin(cardTraitTranslation).on(
                cardTraitTranslation.trait.eq(cardTrait)
                    .and(cardTraitTranslation.languageCode.eq(displayLanguage)),
            )
            .leftJoin(jpCardTraitTranslation).on(
                jpCardTraitTranslation.trait.eq(cardTrait)
                    .and(jpCardTraitTranslation.languageCode.eq(DEFAULT_LANGUAGE)),
            )
            .leftJoin(com.tcgsearch.domain.card.entity.QCardIdentityTrait.cardIdentityTrait)
            .on(com.tcgsearch.domain.card.entity.QCardIdentityTrait.cardIdentityTrait.trait.eq(cardTrait))
            .leftJoin(cardPrinting).on(
                cardPrinting.cardIdentity.eq(
                    com.tcgsearch.domain.card.entity.QCardIdentityTrait.cardIdentityTrait.cardIdentity,
                ),
            )
            .where(predicate)
            .groupBy(
                cardTrait.id,
                cardTraitTranslation.name,
                jpCardTraitTranslation.name,
                cardTrait.name,
            )
            .orderBy(cardPrinting.countDistinct().desc(), cardTrait.name.asc())
            .limit(limit.toLong())
            .fetch()
            .map {
                val canonicalName = requireNotNull(it.get(cardTrait.name))
                val translatedName = it.get(cardTraitTranslation.name)
                val jpName = it.get(jpCardTraitTranslation.name)
                CharacterOptionRow(
                    id = requireNotNull(it.get(cardTrait.id)),
                    name = requireNotNull(translatedName ?: jpName ?: canonicalName),
                    aliases = listOfNotNull(canonicalName, translatedName, jpName).distinct(),
                    cardCount = it.get(cardPrinting.countDistinct()) ?: 0L,
                )
            }
    }

    override fun illustrators(
        query: String?,
        limit: Int,
    ): List<IllustratorOptionRow> {
        val predicate = BooleanBuilder()
        query?.let {
            predicate.and(illustrator.name.containsIgnoreCase(it))
        }

        return queryFactory
            .select(
                illustrator.id,
                illustrator.name,
                cardPrintingIllustrator.cardPrinting.countDistinct(),
            )
            .from(illustrator)
            .leftJoin(cardPrintingIllustrator).on(cardPrintingIllustrator.illustrator.eq(illustrator))
            .where(predicate)
            .groupBy(illustrator.id, illustrator.name)
            .orderBy(cardPrintingIllustrator.cardPrinting.countDistinct().desc(), illustrator.name.asc())
            .limit(limit.toLong())
            .fetch()
            .map {
                IllustratorOptionRow(
                    id = requireNotNull(it.get(illustrator.id)),
                    name = requireNotNull(it.get(illustrator.name)),
                    cardCount = it.get(cardPrintingIllustrator.cardPrinting.countDistinct()) ?: 0L,
                )
            }
    }

    override fun trending(
        language: String,
        limit: Int,
        period: String,
    ): List<TrendingCardRow> {
        val predicate = BooleanBuilder()
            .and(cardSearchEvent.eventType.eq(CARD_OPEN_EVENT_TYPE))
            .and(cardSearchEvent.selectedPrinting.isNotNull)
            .and(cardSearchEvent.createdAt.goe(periodStart(period)))
        if (language != ALL_LANGUAGE) {
            predicate.and(cardSearchEvent.selectedPrinting.languageCode.eq(language))
        }

        return queryFactory
            .select(
                cardSearchEvent.selectedPrinting.id,
                cardSearchEvent.id.count(),
            )
            .from(cardSearchEvent)
            .where(predicate)
            .groupBy(cardSearchEvent.selectedPrinting.id)
            .orderBy(cardSearchEvent.id.count().desc())
            .limit(limit.toLong())
            .fetch()
            .map {
                TrendingCardRow(
                    printingId = requireNotNull(it.get(cardSearchEvent.selectedPrinting.id)),
                    searchCount = it.get(cardSearchEvent.id.count()) ?: 0L,
                )
            }
    }

    private fun periodStart(period: String): Instant =
        when (period) {
            "day" -> Instant.now().minus(1, ChronoUnit.DAYS)
            "month" -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now().minus(7, ChronoUnit.DAYS)
        }

    private companion object {
        const val ALL_LANGUAGE = "all"
        const val DEFAULT_LANGUAGE = "jp"
        const val CARD_OPEN_EVENT_TYPE = "card_open"

        val jpCardSetTranslation = QCardSetTranslation("iosJpCardSetTranslation")
        val jpCardTraitTranslation = QCardTraitTranslation("iosJpCardTraitTranslation")
    }
}
