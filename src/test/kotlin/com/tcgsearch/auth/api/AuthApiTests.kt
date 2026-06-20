package com.tcgsearch.auth.api

import com.tcgsearch.TestcontainersConfiguration
import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.global.property.jwt.JwtProperties
import com.tcgsearch.global.util.RefreshTokenHasher
import com.tcgsearch.global.util.TokenIssuer
import java.time.Instant
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import org.hamcrest.Matchers.hasItem

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class AuthApiTests(
    @Autowired private val appUsers: UserRepository,
    @Autowired private val refreshTokens: RefreshTokenRepository,
	@Autowired private val refreshTokenHasher: RefreshTokenHasher,
	@Autowired private val tokenIssuer: TokenIssuer,
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
    fun `signup creates local user and returns a bearer token pair`() {
        mockMvc
            .perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "collector01",
                          "password": "password123!",
                          "device_id": "ios-primary"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.access_token").isNotEmpty)
            .andExpect(jsonPath("$.refresh_token").isNotEmpty)
            .andExpect(jsonPath("$.expires_in").value(900))

        val user = appUsers.findByAuthProviderAndProviderSubject("local", "collector01")
            ?: error("local user not found")

        assertEquals("local", user.authProvider)
        assertEquals("collector01", user.providerSubject)
        assertNotNull(user.passwordHash)
        assertNotEquals("password123", user.passwordHash)
    }

    @Test
    fun `signup rejects duplicate local id`() {
        signUp(id = "duplicate01")

        mockMvc
            .perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "duplicate01",
                          "password": "password123!",
                          "device_id": "ios-primary"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("DUPLICATE_USER_ID"))
    }

    @Test
    fun `login returns a bearer token pair for valid local credentials`() {
        signUp(id = "login01", password = "password123!")

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "login01",
                          "password": "password123!",
                          "device_id": "ios-primary"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.access_token").isNotEmpty)
            .andExpect(jsonPath("$.refresh_token").isNotEmpty)
            .andExpect(jsonPath("$.expires_in").value(900))
    }

    @Test
    fun `login rejects invalid password`() {
        signUp(id = "wrong_password01", password = "password123!")

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "wrong_password01",
                          "password": "wrong-password",
                          "device_id": "ios-primary"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("INVALID_LOGIN_CREDENTIALS"))
    }

    @Test
    fun `signup rejects local id that violates policy`() {
        listOf(
            "abc",
            "User01",
            ".user01",
            "user01.",
            "user..name",
        ).forEach { invalidId ->
            mockMvc
                .perform(
                    post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "id": "$invalidId",
                              "password": "password123!",
                              "device_id": "ios-primary"
                            }
                            """.trimIndent(),
                        ),
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }
    }

    @Test
    fun `signup validation groups field errors by field name with exception names`() {
        mockMvc
            .perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "ABC",
                          "password": "password123!",
                          "device_id": "ios-primary"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.field_error.id").isArray)
            .andExpect(jsonPath("$.field_error.id[*].message", hasItem("must be between 4 and 20 characters.")))
            .andExpect(
                jsonPath(
                    "$.field_error.id[*].message",
                    hasItem("must be 4 to 20 lowercase letters, numbers, dots, or underscores and start/end with a letter or number."),
                ),
            )
            .andExpect(jsonPath("$.field_error.id[*].exception", hasItem("SizeException")))
            .andExpect(jsonPath("$.field_error.id[*].exception", hasItem("RegexException")))
    }

    @Test
    fun `signup rejects password that violates policy`() {
        listOf(
            "password123",
            "password!",
            "12345678!",
            "pass 123!",
        ).forEach { invalidPassword ->
            mockMvc
                .perform(
                    post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "id": "valid_${invalidPassword.length}",
                              "password": "$invalidPassword",
                              "device_id": "ios-primary"
                            }
                            """.trimIndent(),
                        ),
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }
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

		val rotated = refreshTokens.findById(refreshToken.id!!) ?: error("refresh token not found")

		assertNotNull(rotated.revokedAt)
		assertNotNull(rotated.rotatedAt)
	}

	@Test
	fun `refresh rejects a reused refresh token and revokes the token family`() {
		val user = seedUser()
		val tokenFamilyId = UUID.randomUUID()
		val now = Instant.now()
		val reused = refreshTokens.save(
			RefreshToken(
				user = user,
				deviceId = "ios-primary",
				tokenHash = refreshTokenHasher.hash("reused-refresh-token"),
				tokenFamilyId = tokenFamilyId,
				expiresAt = now.plusSeconds(60 * 60 * 24 * 30),
			).apply {
				rotatedAt = now.minusSeconds(10)
			},
		)
		val sibling = refreshTokens.save(
			RefreshToken(
				user = user,
				deviceId = "ios-primary",
				tokenHash = refreshTokenHasher.hash("sibling-refresh-token"),
				tokenFamilyId = tokenFamilyId,
				expiresAt = now.plusSeconds(60 * 60 * 24 * 30),
			),
		)

		mockMvc
			.perform(
				post("/api/auth/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"refresh_token":"reused-refresh-token"}"""),
			)
			.andExpect(status().isUnauthorized)

		val revokedReused = refreshTokens.findById(reused.id!!) ?: error("reused token not found")
		val revokedSibling = refreshTokens.findById(sibling.id!!) ?: error("sibling token not found")

		assertNotNull(revokedReused.revokedAt)
		assertNotNull(revokedSibling.revokedAt)
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

		val revoked = refreshTokens.findById(refreshToken.id!!) ?: error("refresh token not found")

		assertNotNull(revoked.revokedAt)
	}

	@Test
	fun `current user returns authenticated user profile`() {
		val user = seedUser(displayName = "Collector Me")
		val accessToken = tokenIssuer.createTokenPair(
			user = user,
			deviceId = "ios-primary",
			tokenFamilyId = UUID.randomUUID(),
			now = Instant.now(),
		).accessToken

		mockMvc
			.perform(
				get("/api/users/me")
					.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken"),
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
					.header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessTokenFor(UUID.randomUUID())}"),
			)
			.andExpect(status().isNotFound)
	}

	private fun seedRefreshToken(rawToken: String): RefreshToken {
		val user = seedUser()

		val refreshToken = refreshTokens.save(
			RefreshToken(
				user = user,
				deviceId = "ios-primary",
				tokenHash = refreshTokenHasher.hash(rawToken),
				tokenFamilyId = UUID.randomUUID(),
				expiresAt = Instant.now().plusSeconds(60 * 60 * 24 * 30),
			),
		)

		assertNull(refreshToken.revokedAt)

		return refreshToken
	}

	private fun seedUser(displayName: String = "Collector"): User =
		appUsers.save(
			User(
				email = "${UUID.randomUUID()}@example.com",
				displayName = displayName,
				authProvider = "local",
				providerSubject = UUID.randomUUID().toString(),
			),
		)

    private fun signUp(
        id: String,
        password: String = "password123!",
        deviceId: String = "ios-primary",
    ) {
        mockMvc
            .perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "$id",
                          "password": "$password",
                          "device_id": "$deviceId"
                        }
                        """.trimIndent(),
                    ),
            )
            .andExpect(status().isCreated)
    }

	private fun createAccessTokenFor(userId: UUID): String {
		val now = Instant.now()
		val claims = JwtClaimsSet.builder()
			.issuer(jwtProperties.issuer)
			.issuedAt(now)
			.expiresAt(now.plus(jwtProperties.accessTokenTtl))
			.subject(userId.toString())
			.claim("role", "USER")
			.build()

		return jwtEncoder
			.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.tokenValue
	}
}
