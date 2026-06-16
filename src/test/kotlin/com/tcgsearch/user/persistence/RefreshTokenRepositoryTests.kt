package com.tcgsearch.user.persistence

import com.tcgsearch.TestcontainersConfiguration
import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.domain.auth.repository.RefreshTokenRepository
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.domain.user.repository.UserRepository
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class RefreshTokenRepositoryTests(
    @Autowired private val appUsers: UserRepository,
    @Autowired private val refreshTokens: RefreshTokenRepository,
) {

	@Test
	fun `persists app user and refresh token`() {
		val user = appUsers.save(
            User(
                email = "collector@example.com",
                displayName = "Collector",
                authProvider = "local",
                providerSubject = "collector@example.com",
            ),
		)
		val tokenFamilyId = UUID.randomUUID()

		val refreshToken = refreshTokens.save(
            RefreshToken(
                user = user,
                deviceId = "ios-primary",
                tokenHash = "sha256:test-refresh-token",
                tokenFamilyId = tokenFamilyId,
                expiresAt = Instant.now().plusSeconds(60 * 60 * 24 * 30),
            ),
		)

		assertNotNull(user.id)
		assertNotNull(refreshToken.id)
		assertEquals(user.id, refreshToken.user.id)
		assertEquals(tokenFamilyId, refreshToken.tokenFamilyId)
		assertTrue(refreshTokens.existsByTokenHash("sha256:test-refresh-token"))
	}
}
