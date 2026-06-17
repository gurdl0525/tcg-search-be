package com.tcgsearch.global.property

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth.jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenTtl: Duration = Duration.ofMinutes(15),
)
