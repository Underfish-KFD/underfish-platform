package ru.underfish.communityservice.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.underfish.communityservice.client.AuthClient
import ru.underfish.communityservice.database.dao.CommunityMemberRepository
import ru.underfish.communityservice.database.dao.CommunityRepository
import ru.underfish.communityservice.dto.request.CommunityMemberRequest
import ru.underfish.communityservice.exception.NotFoundException
import ru.underfish.communityservice.security.CurrentUserProvider
import java.util.UUID

@ExtendWith(SpringExtension::class)
class CommunityMemberServiceTest {
    private val memberRepo: CommunityMemberRepository = mock()
    private val communityRepo: CommunityRepository = mock()
    private val currentUserProvider: CurrentUserProvider = mock()
    private val communityOrganizerService: CommunityOrganizerService = mock()
    private val authClient: AuthClient = mock()

    private val service =
        CommunityMemberService(
            memberRepo,
            communityRepo,
            currentUserProvider,
            communityOrganizerService,
            authClient,
        )

    @Test
    fun `addMember should throw NotFound when auth client reports missing user`() {
        val communityId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request = CommunityMemberRequest(userId = userId)

        whenever(authClient.getUser(userId)).thenThrow(RuntimeException("not found"))

        // stub current user provider to return the same user (self) so permission check passes
        whenever(currentUserProvider.getRequired())
            .thenReturn(
                ru.underfish.communityservice.security.GatewayPrincipal(
                    userId = userId.toString(),
                    email = null,
                    roles = listOf("USER"),
                    authSource = null,
                ),
            )

        assertThrows(NotFoundException::class.java) {
            service.addMember(communityId, request)
        }
    }
}
