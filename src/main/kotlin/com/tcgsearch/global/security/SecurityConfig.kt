package com.tcgsearch.global.security

import com.tcgsearch.global.property.SecurityCorsProperties
import com.tcgsearch.global.property.SecurityJwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * API 서버 보안 정책을 구성합니다.
 *
 * Native App 클라이언트를 기준으로 세션을 만들지 않고 Bearer JWT를 검증합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityCorsProperties::class, SecurityJwtProperties::class)
class SecurityConfig(private val corsProperties: SecurityCorsProperties) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            // HTML 기본 로그인
            .formLogin { formLogin -> formLogin.disable() }
            // CSRF 공격 차단
            .csrf { csrf -> csrf.disable() }
            // CORS 정책
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            // 세션 정책
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // ?
            .httpBasic { httpBasic -> httpBasic.disable() }
            // ?
            .logout { logout -> logout.disable() }
            // 인증 설정
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/logout").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().denyAll()
            }
            .apply {  }
            .build()

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.let {
            it.allowedOrigins = corsProperties.allowedOrigins
            it.allowedMethods = corsProperties.allowedMethods
            it.allowedHeaders = corsProperties.allowedHeaders
            it.exposedHeaders = corsProperties.exposedHeaders
            it.allowCredentials = false
            it.maxAge = corsProperties.maxAge
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}