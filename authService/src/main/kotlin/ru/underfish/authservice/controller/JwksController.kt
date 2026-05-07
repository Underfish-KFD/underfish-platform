package ru.underfish.authservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.underfish.authservice.security.RsaKeyProvider
import java.security.interfaces.RSAPublicKey
import java.util.Base64

@RestController
class JwksController(
    private val rsaKeyProvider: RsaKeyProvider,
) {
    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, List<Map<String, String>>> {
        val publicKey = rsaKeyProvider.publicKey

        return mapOf(
            "keys" to listOf(
                mapOf(
                    "kty" to "RSA",
                    "use" to "sig",
                    "kid" to rsaKeyProvider.keyId,
                    "alg" to "RS256",
                    "n" to publicKey.urlEncodedModulus(),
                    "e" to publicKey.urlEncodedExponent(),
                ),
            ),
        )
    }

    private fun RSAPublicKey.urlEncodedModulus(): String =
        base64Url(modulus.toByteArray().withoutLeadingZeroByte())

    private fun RSAPublicKey.urlEncodedExponent(): String =
        base64Url(publicExponent.toByteArray().withoutLeadingZeroByte())

    private fun ByteArray.withoutLeadingZeroByte(): ByteArray =
        if (size > 1 && first() == ZERO_BYTE) copyOfRange(1, size) else this

    private fun base64Url(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    companion object {
        private const val ZERO_BYTE: Byte = 0
    }
}
