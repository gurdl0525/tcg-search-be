package com.tcgsearch.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.tcgsearch.global.util.TokenProvider
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class FilterConfig(
    private val tokenProvider: TokenProvider,
    private val objectMapper: ObjectMapper,
): SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
    override fun configure(http: HttpSecurity) {
        builder.addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
        builder.addFilterBefore(ExceptionFilter(objectMapper), JwtFilter::class.java)
    }
}