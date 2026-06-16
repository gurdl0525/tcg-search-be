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
			      'external_card_links'
			  )
			""".trimIndent(),
			String::class.java,
		).toSet()

		assertEquals(
			setOf("card_identities", "card_printings", "decks", "external_card_links"),
			tableNames,
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
