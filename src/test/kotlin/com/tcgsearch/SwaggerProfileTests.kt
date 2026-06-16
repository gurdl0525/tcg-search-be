package com.tcgsearch

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("swagger")
@SpringBootTest
class SwaggerProfileTests {

	@Autowired
	lateinit var webApplicationContext: WebApplicationContext

	private lateinit var mockMvc: MockMvc

	@BeforeTest
	fun setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
	}

	@Test
	fun openApiDocsAreExposed() {
		val response = mockMvc
			.perform(get("/v3/api-docs"))
			.andExpect(status().isOk)
			.andReturn()
			.response
			.contentAsString

		assertTrue(response.contains("\"openapi\""))
	}

	@Test
	fun swaggerUiIsExposed() {
		val status = mockMvc
			.perform(get("/swagger-ui"))
			.andReturn()
			.response
			.status

		assertTrue(status in 200..399)
	}
}
