package com.tcgsearch.common.security

import com.tcgsearch.TestcontainersConfiguration
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class SecurityConfigurationTests {

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
	fun `actuator health is public`() {
		mockMvc
			.perform(get("/actuator/health"))
			.andExpect(status().isOk)
	}

	@Test
	fun `api requests require bearer authentication`() {
		mockMvc
			.perform(get("/api/cards"))
			.andExpect(status().isUnauthorized)
	}

	@Test
	fun `jwt authenticated api request passes security`() {
		mockMvc
			.perform(get("/api/cards").with(jwt()))
			.andExpect(status().isNotFound)
	}

	@Test
	fun `cors preflight allows configured local origin`() {
		mockMvc
			.perform(
				options("/api/cards")
					.header(HttpHeaders.ORIGIN, "http://localhost:3000")
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()),
			)
			.andExpect(status().isOk)
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
	}

	@Test
	fun `csrf does not block bearer api post requests`() {
		val response = mockMvc
			.perform(
				post("/api/cards")
					.with(jwt())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"),
			)
			.andReturn()
			.response

		assertNotEquals(403, response.status)
	}
}
