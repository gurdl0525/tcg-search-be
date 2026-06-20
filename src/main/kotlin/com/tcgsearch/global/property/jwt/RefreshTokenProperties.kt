package com.tcgsearch.global.property.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.auth.refresh-token")
data class RefreshTokenProperties(
    val ttl: Duration = Duration.ofDays(30),
)