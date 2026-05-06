package ru.underfish.gateway.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewayHeadersProperties::class)
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    HttpMethod.POST,
                    "/api/v1/users/register",
                    "/api/v1/users/login",
                    "/api/v1/tokens/refresh",
                ).permitAll()
                it.pathMatchers("/actuator/health").permitAll()
                it.pathMatchers("/api/v1/**").authenticated()
                it.anyExchange().denyAll()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
}
