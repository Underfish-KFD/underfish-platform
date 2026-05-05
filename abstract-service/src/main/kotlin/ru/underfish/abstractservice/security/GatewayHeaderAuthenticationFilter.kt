package ru.underfish.abstractservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import ru.underfish.abstractservice.config.GatewaySecurityProperties

class GatewayHeaderAuthenticationFilter(
    private val gatewaySecurityProperties: GatewaySecurityProperties,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!gatewaySecurityProperties.enabled) {
            return true
        }

        val requestPath = request.requestURI
        return gatewaySecurityProperties.publicPaths.any { pathMatcher.match(it, requestPath) }
    }

    @Suppress("ReturnCount")
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        val headers = gatewaySecurityProperties.headers
        if (!isInternalTokenValid(request.getHeader(headers.internalToken))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal token")
            return
        }

        val userId = request.getHeader(headers.userId)?.trim().orEmpty()
        val rawRoles = request.getHeader(headers.userRoles)?.trim().orEmpty()

        if (userId.isBlank() || rawRoles.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing gateway identity headers")
            return
        }

        val roles =
            rawRoles.split(gatewaySecurityProperties.rolesDelimiter)
                .map(String::trim)
                .filter(String::isNotBlank)

        if (roles.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No roles provided by gateway")
            return
        }

        val principal =
            GatewayPrincipal(
                userId = userId,
                email = request.getHeader(headers.userEmail)?.trim()?.ifBlank { null },
                roles = roles,
                authSource = request.getHeader(headers.authSource)?.trim()?.ifBlank { null },
            )

        val authorities =
            roles.map { role ->
                val normalized = if (role.startsWith("ROLE_")) role else "ROLE_$role"
                SimpleGrantedAuthority(normalized)
            }

        val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }

    private fun isInternalTokenValid(tokenFromHeader: String?): Boolean {
        val requiredToken = gatewaySecurityProperties.trustedInternalToken?.trim().orEmpty()
        if (requiredToken.isEmpty()) {
            return true
        }
        return tokenFromHeader?.trim() == requiredToken
    }
}
