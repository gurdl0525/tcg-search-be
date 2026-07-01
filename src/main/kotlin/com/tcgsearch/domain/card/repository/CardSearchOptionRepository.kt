package com.tcgsearch.domain.card.repository

import java.time.LocalDate
import java.util.UUID

interface CardSearchOptionRepository {
    fun packs(language: String, query: String?, limit: Int): List<PackOptionRow>

    fun characters(language: String, query: String?, limit: Int): List<CharacterOptionRow>

    fun illustrators(query: String?, limit: Int): List<IllustratorOptionRow>

    fun trending(language: String, limit: Int, period: String): List<TrendingCardRow>
}

data class PackOptionRow(
    val code: String,
    val name: String,
    val releaseDate: LocalDate?,
)

data class CharacterOptionRow(
    val id: UUID,
    val name: String,
    val aliases: List<String>,
    val cardCount: Long,
)

data class IllustratorOptionRow(
    val id: UUID,
    val name: String,
    val cardCount: Long,
)

data class TrendingCardRow(
    val printingId: UUID,
    val searchCount: Long,
)
