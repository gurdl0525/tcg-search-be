package com.tcgsearch.global.property.cors

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.cors")
data class SecurityCorsProperties(
	val allowedOrigins: List<String> = listOf(
		"http://localhost:3000",
		"http://localhost:5173",
		"http://127.0.0.1:3000",
		"http://127.0.0.1:5173",
	),
	val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
	val allowedHeaders: List<String> = listOf("Authorization", "Content-Type", "Accept", "Origin"),
	val exposedHeaders: List<String> = listOf("Location"),
	val maxAge: Long = 3600,
)