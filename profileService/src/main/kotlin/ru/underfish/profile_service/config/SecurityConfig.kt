package ru.underfish.profile_service.config


import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import ru.underfish.profile_service.security.GatewayHeaderAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(GatewaySecurityProperties::class)
class SecurityConfig(
    private val gatewaySecurityProperties: GatewaySecurityProperties,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun gatewayHeaderAuthenticationFilter(): GatewayHeaderAuthenticationFilter =
        GatewayHeaderAuthenticationFilter(gatewaySecurityProperties)

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        gatewayHeaderAuthenticationFilter: GatewayHeaderAuthenticationFilter,
    ): SecurityFilterChain {
        http {
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }

        if (!gatewaySecurityProperties.enabled) {
            http {
                authorizeHttpRequests {
                    authorize(anyRequest, permitAll)
                }
            }
            return http.build()
        }

        http {
            authorizeHttpRequests {
                gatewaySecurityProperties.publicPaths.forEach { path ->
                    authorize(path, permitAll)
                }
                authorize(anyRequest, authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(gatewayHeaderAuthenticationFilter)
        }

        return http.build()
    }
}
