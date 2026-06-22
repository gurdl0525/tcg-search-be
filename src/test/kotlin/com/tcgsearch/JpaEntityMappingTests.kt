package com.tcgsearch

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class JpaEntityMappingTests(
    @Autowired private val entityManagerFactory: EntityManagerFactory,
) {

    @Test
    fun `all flyway application tables are mapped as jpa entities`() {
        val mappedTableNames = entityManagerFactory.metamodel.entities
            .mapNotNull { entityType ->
                entityType.javaType.getAnnotation(Table::class.java)?.name
            }
            .toSet()

        assertEquals(
            setOf(
                "app_users",
                "attributes",
                "colors",
                "rarities",
                "traits",
                "card_sets",
                "card_identities",
                "card_identity_colors",
                "card_identity_traits",
                "card_printings",
                "user_collection_entries",
                "decks",
                "deck_cards",
                "external_marketplaces",
                "external_card_links",
                "user_refresh_tokens",
            ),
            mappedTableNames,
        )
    }
}
