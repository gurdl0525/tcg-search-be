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
			  and column_name in ('auth_provider', 'provider_subject', 'role', 'enabled')
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

		assertEquals(setOf("auth_provider", "provider_subject", "role", "enabled"), userColumnNames)
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
}
