package ru.underfish.communityservice.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.underfish.communityservice.client.AuthClient
import ru.underfish.communityservice.database.dao.CommunityOrganizerRepository
import ru.underfish.communityservice.security.CurrentUserProvider
import ru.underfish.communityservice.exception.NotFoundException
import java.util.UUID

@ExtendWith(SpringExtension::class)
class CommunityOrganizerServiceTest {
    private val organizerRepo: CommunityOrganizerRepository = mock()
    private val currentUserProvider: CurrentUserProvider = mock()
    private val authClient: AuthClient = mock()

    private val service = CommunityOrganizerService(
        organizerRepo,
        currentUserProvider,
        authClient,
    )

    @Test
    fun `addOrganizer should throw NotFound when auth client reports missing user`() {
        val communityId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        whenever(authClient.getUser(userId)).thenThrow(RuntimeException("not found"))

        whenever(currentUserProvider.getRequired()).thenReturn(
            ru.underfish.communityservice.security.GatewayPrincipal(
                userId = userId.toString(),
                email = null,
                roles = listOf("USER"),
                authSource = null,
            ),
        )

        assertThrows(NotFoundException::class.java) {
            service.addOrganizer(communityId, userId)
        }
    }
}

