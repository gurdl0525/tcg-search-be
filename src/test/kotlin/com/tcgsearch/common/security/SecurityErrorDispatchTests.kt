package com.tcgsearch.common.security

import com.tcgsearch.TestcontainersConfiguration
import com.tcgsearch.domain.user.entity.User
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.global.util.TokenIssuer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityErrorDispatchTests(
    @Autowired private val appUsers: UserRepository,
    @Autowired private val tokenIssuer: TokenIssuer,
) {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `authenticated missing api path returns not found`() {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("http://localhost:$port/api/user/me"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${createAccessToken()}")
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode())
    }

    private fun createAccessToken(): String {
        val user = appUsers.save(
            User(
                email = "${UUID.randomUUID()}@example.com",
                displayName = "Collector",
                authProvider = "local",
                providerSubject = UUID.randomUUID().toString(),
            ),
        )

        return tokenIssuer.createTokenPair(
            user = user,
            deviceId = "ios-primary",
            tokenFamilyId = UUID.randomUUID(),
            now = Instant.now(),
        ).accessToken
    }
}
