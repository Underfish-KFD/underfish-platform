package ru.underfish.authservice.client.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProfileClientConfig(
    private val props: ProfileClientProperties,
) {
    @Bean
    fun profileAuthHeadersInterceptor(): RequestInterceptor =
        RequestInterceptor { template ->
            if (props.internalToken.isNotBlank()) {
                template.header("X-Internal-Token", props.internalToken)
            }
            template.header("X-User-Id", props.internalUserId)
            template.header("X-User-Roles", props.internalRoles)
            template.header("X-Auth-Source", props.authSource)
        }
}

