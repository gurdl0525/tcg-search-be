package com.tcgsearch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class SchemaMigrationTests(
	@Autowired private val jdbcTemplate: JdbcTemplate,
) {

	@Test
	fun `flyway creates representative application tables`() {
		val tableNames = jdbcTemplate.queryForList(
			"""
			select table_name
			from information_schema.tables
			where table_schema = 'public'
			  and table_name in (
			      'card_identities',
			      'card_printings',
			      'decks',
			      'external_card_links',
			      'user_refresh_tokens'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(
			setOf("card_identities", "card_printings", "decks", "external_card_links", "user_refresh_tokens"),
			tableNames,
		)
	}

	@Test
	fun `flyway creates user authentication columns`() {
		val userColumnNames = jdbcTemplate.queryForList(
			"""
			select column_name
			from information_schema.columns
			where table_schema = 'public'
			  and table_name = 'app_users'
			  and column_name in ('auth_provider', 'provider_subject', 'password_hash', 'role', 'enabled')
			""".trimIndent(),
			String::class.java,
		).toSet()

		val refreshTokenColumnNames = jdbcTemplate.queryForList(
			"""
			select column_name
			from information_schema.columns
			where table_schema = 'public'
			  and table_name = 'user_refresh_tokens'
			  and column_name in (
			      'user_id',
			      'device_id',
			      'token_hash',
			      'token_family_id',
			      'expires_at',
			      'last_used_at',
			      'revoked_at',
			      'rotated_at'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(setOf("auth_provider", "provider_subject", "password_hash", "role", "enabled"), userColumnNames)
		assertEquals(
			setOf(
				"user_id",
				"device_id",
				"token_hash",
				"token_family_id",
				"expires_at",
				"last_used_at",
				"revoked_at",
				"rotated_at",
			),
			refreshTokenColumnNames,
		)
	}

	@Test
	fun `flyway seeds marketplace and card reference values`() {
		val snkrDunkCount = jdbcTemplate.queryForObject(
			"select count(*) from external_marketplaces where code = 'snkrdunk'",
			Int::class.java,
		)
		val colorCount = jdbcTemplate.queryForObject(
			"select count(*) from colors",
			Int::class.java,
		)

		assertEquals(1, snkrDunkCount)
		assertEquals(6, colorCount)
	}

	@Test
	fun `flyway creates multilingual card translation tables`() {
		val tableNames = jdbcTemplate.queryForList(
			"""
			select table_name
			from information_schema.tables
			where table_schema = 'public'
			  and table_name in (
			      'card_identity_translations',
			      'card_set_translations',
			      'attribute_translations',
			      'trait_translations'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(
			setOf(
				"card_identity_translations",
				"card_set_translations",
				"attribute_translations",
				"trait_translations",
			),
			tableNames,
		)
	}

	@Test
	fun `flyway creates multilingual translation unique constraints`() {
		val uniqueIndexes = jdbcTemplate.queryForList(
			"""
			select indexname
			from pg_indexes
			where schemaname = 'public'
			  and indexname in (
			      'card_identity_translations_identity_language_unique',
			      'card_set_translations_set_language_unique',
			      'attribute_translations_attribute_language_unique',
			      'trait_translations_trait_language_unique'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(
			setOf(
				"card_identity_translations_identity_language_unique",
				"card_set_translations_set_language_unique",
				"attribute_translations_attribute_language_unique",
				"trait_translations_trait_language_unique",
			),
			uniqueIndexes,
		)
	}

	@Test
	fun `flyway creates card translation search token table and indexes`() {
		val tableNames = jdbcTemplate.queryForList(
			"""
			select table_name
			from information_schema.tables
			where table_schema = 'public'
			  and table_name = 'card_identity_translation_search_tokens'
			""".trimIndent(),
			String::class.java,
		).toSet()

		val indexNames = jdbcTemplate.queryForList(
			"""
			select indexname
			from pg_indexes
			where schemaname = 'public'
			  and tablename = 'card_identity_translation_search_tokens'
			  and indexname in (
			      'cit_search_tokens_translation_token_unique',
			      'cit_search_tokens_token_identity_idx',
			      'cit_search_tokens_identity_idx',
			      'cit_search_tokens_language_token_idx'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(setOf("card_identity_translation_search_tokens"), tableNames)
		assertEquals(
			setOf(
				"cit_search_tokens_translation_token_unique",
				"cit_search_tokens_token_identity_idx",
				"cit_search_tokens_identity_idx",
				"cit_search_tokens_language_token_idx",
			),
			indexNames,
		)
	}

	@Test
	fun `flyway creates ios card search support tables columns and indexes`() {
		val columnNames = jdbcTemplate.queryForList(
			"""
			select column_name
			from information_schema.columns
			where table_schema = 'public'
			  and table_name = 'card_printings'
			  and column_name = 'detail_tags'
			""".trimIndent(),
			String::class.java,
		).toSet()

		val tableNames = jdbcTemplate.queryForList(
			"""
			select table_name
			from information_schema.tables
			where table_schema = 'public'
			  and table_name in (
			      'illustrators',
			      'card_printing_illustrators',
			      'card_search_events'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		val indexNames = jdbcTemplate.queryForList(
			"""
			select indexname
			from pg_indexes
			where schemaname = 'public'
			  and indexname in (
			      'card_printings_detail_tags_idx',
			      'card_printing_illustrators_illustrator_idx',
			      'card_search_events_event_type_created_idx',
			      'card_search_events_selected_printing_idx'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(setOf("detail_tags"), columnNames)
		assertEquals(
			setOf(
				"illustrators",
				"card_printing_illustrators",
				"card_search_events",
			),
			tableNames,
		)
		assertEquals(
			setOf(
				"card_printings_detail_tags_idx",
				"card_printing_illustrators_illustrator_idx",
				"card_search_events_event_type_created_idx",
				"card_search_events_selected_printing_idx",
			),
			indexNames,
		)
	}
}
