package ru.underfish.abstractservice.security

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class TestSecurityController {
    @GetMapping("/me")
    fun me(currentUserProvider: CurrentUserProvider): String = currentUserProvider.getRequired().userId
}
