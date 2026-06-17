package com.tcgsearch.global.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.tcgsearch.global.property.JwtProperties
import com.tcgsearch.global.property.SecurityCorsProperties
import jakarta.servlet.DispatcherType
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

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
@EnableConfigurationProperties(SecurityCorsProperties::class, JwtProperties::class)
class SecurityConfig(
    private val corsProperties: SecurityCorsProperties,
    private val jwtProperties: JwtProperties,
) {
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
            .exceptionHandling { exception ->
                exception.authenticationEntryPoint { _, response, _ ->
                    response.status = HttpStatus.UNAUTHORIZED.value()
                }
            }
            // HTTP Basic 인증 비활성화
            .httpBasic { httpBasic -> httpBasic.disable() }
            // 서버 세션 기반 logout 비활성화
            .logout { logout -> logout.disable() }
            // 인증 설정
            .authorizeHttpRequests { authorize ->
                authorize
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                    ).permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().denyAll()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { }
            }
            .build()

    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder
            .withSecretKey(jwtSecretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    @Bean
    fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(jwtSecretKey()))

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

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

    private fun jwtSecretKey(): SecretKey =
        SecretKeySpec(Base64.getDecoder().decode(jwtProperties.secret), HMAC_SHA_256)

    private companion object {
        const val HMAC_SHA_256 = "HmacSHA256"
    }
}
