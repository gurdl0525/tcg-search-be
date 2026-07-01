package com.tcgsearch.domain.card.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

data class CardSearchEventRequest(
    @field:Pattern(regexp = "search|filter_apply|card_open", message = "must be search, filter_apply, or card_open.")
    val eventType: String,

    @field:Size(max = 100, message = "must be at most 100 characters.")
    val query: String? = null,

    @field:Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
    val language: String = "all",

    val filters: Map<String, List<String>> = emptyMap(),

    @field:Min(0, message = "must be greater than or equal to 0.")
    val resultCount: Int? = null,

    val selectedPrintingId: UUID? = null,
)
