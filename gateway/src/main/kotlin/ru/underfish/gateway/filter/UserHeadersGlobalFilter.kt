package ru.underfish.gateway.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.underfish.gateway.config.GatewayHeadersProperties

@Component
class UserHeadersGlobalFilter(
    private val props: GatewayHeadersProperties,
) : GlobalFilter, Ordered {

    override fun getOrder(): Int = -10

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return exchange.getPrincipal<Authentication>()
             .flatMap { auth ->
                if (!auth.isAuthenticated) {
                     return@flatMap chain.filter(exchange)
                 }

                 val jwt = auth.principal as? Jwt ?: return@flatMap chain.filter(exchange)

                val userId = jwt.claims.stringClaim("user_id")
                    .ifBlank { jwt.claims.stringClaim("userId") }
                    .ifBlank { jwt.subject ?: "" }

                val roles = auth.authorities
                    .map { it.authority.removePrefix("ROLE_") }
                    .filter { it.isNotBlank() }
                    .joinToString(",")

                val mutatedRequest = exchange.request.mutate()
                    .headers { headers ->
                        headers.set(props.userIdHeader, userId)
                        headers.set(props.userRolesHeader, roles)
                        headers.set(props.internalTokenHeader, props.internalToken)
                    }
                    .build()

                chain.filter(exchange.mutate().request(mutatedRequest).build())
             }
            .switchIfEmpty(chain.filter(exchange))
     }

    private fun Map<String, Any>.stringClaim(name: String): String =
        this[name]?.toString()?.trim().orEmpty()
 }
