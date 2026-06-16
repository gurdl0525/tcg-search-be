package com.tcgsearch.auth.api

import com.tcgsearch.TestcontainersConfiguration
import com.tcgsearch.user.persistence.AppUserJpaEntity
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.user.persistence.UserRefreshTokenJpaEntity
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class AuthApiTests(
    @Autowired private val appUsers: UserRepository,
    @Autowired private val refreshTokens: RefreshTokenRepository,
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
	fun `refresh rotates refresh token and returns a bearer token pair`() {
		val refreshToken = seedRefreshToken("refresh-token-to-rotate")

		mockMvc
			.perform(
				post("/api/auth/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"refresh_token":"refresh-token-to-rotate"}"""),
			)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.token_type").value("Bearer"))
			.andExpect(jsonPath("$.access_token").isNotEmpty)
			.andExpect(jsonPath("$.refresh_token").isNotEmpty)
			.andExpect(jsonPath("$.expires_in").value(900))

		val rotated = refreshTokens.findById(refreshToken.id!!).orElseThrow()

		assertNotNull(rotated.revokedAt)
		assertNotNull(rotated.rotatedAt)
	}

	@Test
	fun `refresh rejects an unknown refresh token`() {
		mockMvc
			.perform(
				post("/api/auth/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"refresh_token":"missing-refresh-token"}"""),
			)
			.andExpect(status().isUnauthorized)
	}

	@Test
	fun `logout revokes refresh token`() {
		val refreshToken = seedRefreshToken("refresh-token-to-logout")

		mockMvc
			.perform(
				post("/api/auth/logout")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"refresh_token":"refresh-token-to-logout"}"""),
			)
			.andExpect(status().isNoContent)

		val revoked = refreshTokens.findById(refreshToken.id!!).orElseThrow()

		assertNotNull(revoked.revokedAt)
	}

	@Test
	fun `current user returns authenticated user profile`() {
		val user = appUsers.save(
			AppUserJpaEntity(
				email = "me-${UUID.randomUUID()}@example.com",
				displayName = "Collector Me",
				authProvider = "local",
				providerSubject = UUID.randomUUID().toString(),
			),
		)

		mockMvc
			.perform(
				get("/api/users/me")
					.with(jwt().jwt { token -> token.subject(user.id.toString()) }),
			)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.id").value(user.id.toString()))
			.andExpect(jsonPath("$.email").value(user.email))
			.andExpect(jsonPath("$.display_name").value(user.displayName))
			.andExpect(jsonPath("$.role").value("USER"))
			.andExpect(jsonPath("$.enabled").value(true))
	}

	@Test
	fun `current user rejects unknown jwt subject`() {
		mockMvc
			.perform(
				get("/api/users/me")
					.with(jwt().jwt { token -> token.subject(UUID.randomUUID().toString()) }),
			)
			.andExpect(status().isNotFound)
	}

	private fun seedRefreshToken(rawToken: String): UserRefreshTokenJpaEntity {
		val user = appUsers.save(
			AppUserJpaEntity(
				email = "${UUID.randomUUID()}@example.com",
				displayName = "Collector",
				authProvider = "local",
				providerSubject = UUID.randomUUID().toString(),
			),
		)

		val refreshToken = refreshTokens.save(
			UserRefreshTokenJpaEntity(
				user = user,
				deviceId = "ios-primary",
				tokenHash = sha256(rawToken),
				tokenFamilyId = UUID.randomUUID(),
				expiresAt = Instant.now().plusSeconds(60 * 60 * 24 * 30),
			),
		)

		assertFalse(refreshToken.isRevoked())

		return refreshToken
	}

	private fun sha256(value: String): String {
		return MessageDigest
			.getInstance("SHA-256")
			.digest(value.toByteArray())
			.joinToString("") { "%02x".format(it) }
	}
}
