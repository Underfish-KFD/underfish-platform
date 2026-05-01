package ru.underfish.authservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.util.Base64

@RestController
class JwksController(private val rsaKeyPair: KeyPair) {
    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> {
        val publicKey = rsaKeyPair.public as RSAPublicKey
        return mapOf(
            "keys" to
                listOf(
                    mapOf(
                        "kty" to "RSA",
                        "use" to "sig",
                        "alg" to "RS256",
                        "kid" to "underfish-auth-key",
                        "n" to publicKey.modulus.toBase64Url(),
                        "e" to publicKey.publicExponent.toBase64Url(),
                    ),
                ),
        )
    }

    private fun BigInteger.toBase64Url(): String {
        var bytes = this.toByteArray()
        // BigInteger.toByteArray() may prepend a zero sign byte — strip it
        if (bytes[0] == 0.toByte()) bytes = bytes.copyOfRange(1, bytes.size)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
