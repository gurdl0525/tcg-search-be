package com.tcgsearch.card.api

import com.tcgsearch.TestcontainersConfiguration
import com.tcgsearch.global.property.jwt.JwtProperties
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class CardSearchApiTests(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val jwtProperties: JwtProperties,
    @Autowired private val jwtEncoder: JwtEncoder,
) {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `search cards returns paginated card printings instead of distinct identities`() {
        seedLuffyPrintings()

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "luffy")
                    .param("language_code", "en")
                    .param("page", "0")
                    .param("size", "1")
                    .param("sort_by", "variant_name")
                    .param("sort", "ASC"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.total_elements").value(2))
            .andExpect(jsonPath("$.total_pages").value(2))
            .andExpect(jsonPath("$.has_next").value(true))
            .andExpect(jsonPath("$.content[0].card_no").value("OP01-001"))
            .andExpect(jsonPath("$.content[0].name").value("Monkey.D.Luffy"))
            .andExpect(jsonPath("$.content[0].variant_name").value("parallel"))
            .andExpect(jsonPath("$.content[0].image_url").value(org.hamcrest.Matchers.startsWith("http://localhost:9000/tcg-search-local/cards/op01-001-parallel.png?")))
            .andExpect(jsonPath("$.content[0].image_url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature=")))
    }

    @Test
    fun `search cards filters by card metadata and printing metadata`() {
        seedLuffyPrintings()

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("card_type", "LEADER")
                    .param("color", "red")
                    .param("rarity", "L")
                    .param("card_set", "OP-01")
                    .param("language_code", "en")
                    .param("is_parallel", "true"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].card_no").value("OP01-001"))
            .andExpect(jsonPath("$.content[0].rarity.code").value("L"))
            .andExpect(jsonPath("$.content[0].card_set.code").value("OP-01"))
            .andExpect(jsonPath("$.content[0].colors[0].code").value("red"))
            .andExpect(jsonPath("$.content[0].traits[0].name").value("Straw Hat Crew"))
            .andExpect(jsonPath("$.content[0].is_parallel").value(true))
    }

    @Test
    fun `search cards rejects invalid pagination and sort query parameters`() {
        listOf(
            "page" to "-1",
            "size" to "0",
            "size" to "101",
            "sort_by" to "unknown",
            "sort" to "DOWN",
            "language_code" to "ja",
        ).forEach { (name, value) ->
            mockMvc
                .perform(
                    get("/api/cards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                        .param(name, value),
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.field_error.$name").isArray)
        }
    }

    @Test
    fun `search cards returns requested language translation without overwriting other languages`() {
        seedMultilingualLuffyPrinting()

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "ルフィ")
                    .param("language_code", "jp"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].card_no").value("OP01-001"))
            .andExpect(jsonPath("$.content[0].name").value("モンキー・D・ルフィ"))
            .andExpect(jsonPath("$.content[0].effect_text").value("日本語の効果"))
            .andExpect(jsonPath("$.content[0].card_set.name").value("ROMANCE DAWN 日本語"))
            .andExpect(jsonPath("$.content[0].language_code").value("jp"))

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "Luffy")
                    .param("language_code", "en"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].card_no").value("OP01-001"))
            .andExpect(jsonPath("$.content[0].name").value("Monkey.D.Luffy"))
            .andExpect(jsonPath("$.content[0].effect_text").value("English effect text"))
            .andExpect(jsonPath("$.content[0].card_set.name").value("ROMANCE DAWN English"))
            .andExpect(jsonPath("$.content[0].language_code").value("en"))
    }

    @Test
    fun `search cards matches card names across english japanese and korean translations`() {
        seedMultilingualLuffyPrinting()

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "Luffy")
                    .param("language_code", "jp"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("モンキー・D・ルフィ"))
            .andExpect(jsonPath("$.content[0].language_code").value("jp"))

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "ルフィ")
                    .param("language_code", "en"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Monkey.D.Luffy"))
            .andExpect(jsonPath("$.content[0].language_code").value("en"))

        mockMvc
            .perform(
                get("/api/cards")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                    .param("search_word", "ㄹ")
                    .param("language_code", "ko"),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total_elements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("몽키 D. 루피"))
            .andExpect(jsonPath("$.content[0].language_code").value("ko"))
    }

    private fun seedLuffyPrintings() {
        val strikeAttributeId = findId("select id from attributes where name = ?", "Strike")
        val redColorId = findId("select id from colors where code = ?", "red")
        val greenColorId = findId("select id from colors where code = ?", "green")
        val leaderRarityId = findId("select id from rarities where code = ?", "L")
        val cardSetId = insertCardSet()
        val traitId = insertTrait("Straw Hat Crew")
        val identityId = insertCardIdentity(
            attributeId = strikeAttributeId,
            cardNo = "OP01-001",
            name = "Monkey.D.Luffy",
            effectText = "[DON!! x2] This Leader gains +1000 power.",
        )
        val greenIdentityId = insertCardIdentity(
            attributeId = strikeAttributeId,
            cardNo = "OP01-999",
            name = "Green Test Leader",
            effectText = "Green filter fixture.",
        )

        insertCardIdentityTranslation(
            identityId = identityId,
            languageCode = "en",
            name = "Monkey.D.Luffy",
            effectText = "[DON!! x2] This Leader gains +1000 power.",
            triggerText = null,
        )
        insertCardIdentityTranslation(
            identityId = greenIdentityId,
            languageCode = "en",
            name = "Green Test Leader",
            effectText = "Green filter fixture.",
            triggerText = null,
        )
        insertCardSetTranslation(cardSetId, "en", "ROMANCE DAWN")
        jdbcTemplate.update(
            "insert into card_identity_colors (card_identity_id, color_id) values (?, ?)",
            identityId,
            redColorId,
        )
        jdbcTemplate.update(
            "insert into card_identity_traits (card_identity_id, trait_id) values (?, ?)",
            identityId,
            traitId,
        )
        jdbcTemplate.update(
            "insert into card_identity_colors (card_identity_id, color_id) values (?, ?)",
            greenIdentityId,
            greenColorId,
        )
        jdbcTemplate.update(
            "insert into card_identity_traits (card_identity_id, trait_id) values (?, ?)",
            greenIdentityId,
            traitId,
        )
        insertCardPrinting(
            identityId = identityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "standard",
            isParallel = false,
            imageUrl = "https://cdn.example.test/op01-001-standard.png",
        )
        insertCardPrinting(
            identityId = identityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "parallel",
            isParallel = true,
            imageUrl = "http://localhost:9000/tcg-search-local/cards/op01-001-parallel.png",
        )
        insertCardPrinting(
            identityId = greenIdentityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "green-parallel",
            isParallel = true,
            imageUrl = "https://cdn.example.test/op01-999-parallel.png",
        )
    }

    private fun seedMultilingualLuffyPrinting() {
        val strikeAttributeId = findId("select id from attributes where name = ?", "Strike")
        val leaderRarityId = findId("select id from rarities where code = ?", "L")
        val cardSetId = insertCardSet()
        val identityId = insertCardIdentity(
            attributeId = strikeAttributeId,
            cardNo = "OP01-001",
            name = "canonical OP01-001",
            effectText = "canonical effect should not be returned",
        )

        insertCardIdentityTranslation(
            identityId = identityId,
            languageCode = "jp",
            name = "モンキー・D・ルフィ",
            effectText = "日本語の効果",
            triggerText = null,
        )
        insertCardIdentityTranslation(
            identityId = identityId,
            languageCode = "en",
            name = "Monkey.D.Luffy",
            effectText = "English effect text",
            triggerText = null,
        )
        insertCardIdentityTranslation(
            identityId = identityId,
            languageCode = "ko",
            name = "몽키 D. 루피",
            effectText = "한국어 효과",
            triggerText = null,
        )
        insertCardSetTranslation(cardSetId, "jp", "ROMANCE DAWN 日本語")
        insertCardSetTranslation(cardSetId, "en", "ROMANCE DAWN English")
        insertCardSetTranslation(cardSetId, "ko", "ROMANCE DAWN 한국어")

        insertCardPrinting(
            identityId = identityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "standard-jp",
            isParallel = false,
            imageUrl = "https://cdn.example.test/jp/op01-001.png",
            languageCode = "jp",
        )
        insertCardPrinting(
            identityId = identityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "standard-en",
            isParallel = false,
            imageUrl = "https://cdn.example.test/en/op01-001.png",
            languageCode = "en",
        )
        insertCardPrinting(
            identityId = identityId,
            cardSetId = cardSetId,
            rarityId = leaderRarityId,
            variantName = "standard-ko",
            isParallel = false,
            imageUrl = "https://cdn.example.test/ko/op01-001.png",
            languageCode = "ko",
        )
    }

    private fun insertCardSet(): UUID =
        insertReturningId(
            """
            insert into card_sets (code, name, product_type, release_date)
            values (?, ?, ?, ?)
            returning id
            """.trimIndent(),
            "OP-01",
            "ROMANCE DAWN",
            "booster",
            LocalDate.of(2022, 12, 2),
        )

    private fun insertTrait(name: String): UUID =
        insertReturningId(
            "insert into traits (name) values (?) returning id",
            name,
        )

    private fun insertCardIdentity(
        attributeId: UUID,
        cardNo: String,
        name: String,
        effectText: String,
    ): UUID =
        insertReturningId(
            """
            insert into card_identities (
                card_no,
                name,
                card_type,
                life,
                power,
                attribute_id,
                effect_text,
                block_no
            )
            values (?, ?, ?, ?, ?, ?, ?, ?)
            returning id
            """.trimIndent(),
            cardNo,
            name,
            "LEADER",
            5,
            5000,
            attributeId,
            effectText,
            1,
        )

    private fun insertCardPrinting(
        identityId: UUID,
        cardSetId: UUID,
        rarityId: UUID,
        variantName: String,
        isParallel: Boolean,
        imageUrl: String,
        languageCode: String = "en",
    ): UUID =
        insertReturningId(
            """
            insert into card_printings (
                card_identity_id,
                card_set_id,
                rarity_id,
                language_code,
                region_code,
                variant_name,
                is_parallel,
                image_url,
                source_url
            )
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            returning id
            """.trimIndent(),
            identityId,
            cardSetId,
            rarityId,
            languageCode,
            "global",
            variantName,
            isParallel,
            imageUrl,
            "https://onepiece-cardgame.com/cardlist/",
        )

    private fun insertCardIdentityTranslation(
        identityId: UUID,
        languageCode: String,
        name: String,
        effectText: String,
        triggerText: String?,
    ): UUID {
        val translationId = insertReturningId(
            """
            insert into card_identity_translations (
                card_identity_id,
                language_code,
                name,
                effect_text,
                trigger_text
            )
            values (?, ?, ?, ?, ?)
            returning id
            """.trimIndent(),
            identityId,
            languageCode,
            name,
            effectText,
            triggerText,
        )
        insertSearchTokens(translationId, identityId, languageCode, "name", name)
        insertSearchTokens(translationId, identityId, languageCode, "effect_text", effectText)
        triggerText?.let {
            insertSearchTokens(translationId, identityId, languageCode, "trigger_text", it)
        }
        return translationId
    }

    private fun insertSearchTokens(
        translationId: UUID,
        identityId: UUID,
        languageCode: String,
        sourceField: String,
        value: String,
    ) {
        searchTokensFor(value).forEach { searchToken ->
            jdbcTemplate.update(
                """
                insert into card_identity_translation_search_tokens (
                    card_identity_translation_id,
                    card_identity_id,
                    language_code,
                    source_field,
                    token_type,
                    token,
                    weight
                )
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict do nothing
                """.trimIndent(),
                translationId,
                identityId,
                languageCode,
                sourceField,
                searchToken.type,
                searchToken.token,
                searchToken.weight,
            )
        }
    }

    private fun searchTokensFor(value: String): Set<SearchToken> = buildSet {
        val normalized = value.toSearchToken()
        if (normalized.isNotBlank()) {
            add(SearchToken(token = normalized, type = "normalized", weight = 100))
            normalized.ngrams(size = 2).forEach {
                add(SearchToken(token = it, type = "ngram", weight = 20))
            }
        }

        val words = value
            .split(SEARCH_TOKEN_SEPARATOR_REGEX)
            .map { it.toSearchToken() }
            .filter { it.isNotBlank() }

        words.forEach { word ->
            add(SearchToken(token = word, type = "word", weight = 90))
            word.prefixes().forEach {
                add(SearchToken(token = it, type = "prefix", weight = 50))
            }

            val choseong = word.toChoseong()
            if (choseong.isNotBlank()) {
                add(SearchToken(token = choseong, type = "choseong", weight = 70))
                choseong.prefixes().forEach {
                    add(SearchToken(token = it, type = "choseong_prefix", weight = 60))
                }
            }
        }

        val fullChoseong = value.toChoseong()
        if (fullChoseong.isNotBlank()) {
            add(SearchToken(token = fullChoseong, type = "choseong", weight = 70))
            fullChoseong.prefixes().forEach {
                add(SearchToken(token = it, type = "choseong_prefix", weight = 60))
            }
        }
    }

    private fun String.toSearchToken(): String =
        lowercase().replace(SEARCH_TOKEN_SEPARATOR_REGEX, "")

    private fun String.prefixes(): List<String> =
        indices.map { index -> substring(0, index + 1) }

    private fun String.ngrams(size: Int): List<String> =
        if (length < size) {
            emptyList()
        } else {
            (0..length - size).map { index -> substring(index, index + size) }
        }

    private fun String.toChoseong(): String =
        mapNotNull { char ->
            when (char.code) {
                in HANGUL_SYLLABLE_START..HANGUL_SYLLABLE_END -> {
                    val index = (char.code - HANGUL_SYLLABLE_START) / HANGUL_JUNGSEONG_JONGSEONG_COUNT
                    HANGUL_CHOSEONG[index]
                }
                in HANGUL_COMPATIBILITY_JAMO_START..HANGUL_COMPATIBILITY_JAMO_END -> char
                else -> null
            }
        }.joinToString("")

    private fun insertCardSetTranslation(
        cardSetId: UUID,
        languageCode: String,
        name: String,
    ): UUID =
        insertReturningId(
            """
            insert into card_set_translations (card_set_id, language_code, name)
            values (?, ?, ?)
            returning id
            """.trimIndent(),
            cardSetId,
            languageCode,
            name,
        )

    private fun findId(sql: String, vararg args: Any): UUID =
        jdbcTemplate.queryForObject(sql, UUID::class.java, *args)
            ?: error("id not found")

    private fun insertReturningId(sql: String, vararg args: Any?): UUID =
        jdbcTemplate.queryForObject(sql, UUID::class.java, *args)
            ?: error("id was not returned")

    private fun createAccessToken(): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.accessTokenTtl))
            .subject(UUID.randomUUID().toString())
            .claim("role", "USER")
            .build()

        return jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .tokenValue
    }

    private data class SearchToken(
        val token: String,
        val type: String,
        val weight: Int,
    )

    private companion object {
        val SEARCH_TOKEN_SEPARATOR_REGEX = Regex("[^\\p{L}\\p{N}]")
        val HANGUL_CHOSEONG = listOf(
            'ㄱ',
            'ㄲ',
            'ㄴ',
            'ㄷ',
            'ㄸ',
            'ㄹ',
            'ㅁ',
            'ㅂ',
            'ㅃ',
            'ㅅ',
            'ㅆ',
            'ㅇ',
            'ㅈ',
            'ㅉ',
            'ㅊ',
            'ㅋ',
            'ㅌ',
            'ㅍ',
            'ㅎ',
        )
        const val HANGUL_SYLLABLE_START = 0xAC00
        const val HANGUL_SYLLABLE_END = 0xD7A3
        const val HANGUL_JUNGSEONG_JONGSEONG_COUNT = 21 * 28
        const val HANGUL_COMPATIBILITY_JAMO_START = 0x3130
        const val HANGUL_COMPATIBILITY_JAMO_END = 0x318F
    }
}
