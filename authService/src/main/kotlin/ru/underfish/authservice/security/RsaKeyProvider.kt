package ru.underfish.authservice.security

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class RsaKeyProvider(
    @Value("\${rsa.private-key:}") private val privateKeyPem: String,
    @Value("\${rsa.public-key:}") private val publicKeyPem: String,
    @Value("\${rsa.require:false}") private val requireConfiguredKeys: Boolean,
) {
    private lateinit var resolvedPrivateKey: RSAPrivateKey
    private lateinit var resolvedPublicKey: RSAPublicKey
    private lateinit var resolvedKeyId: String

    val privateKey: RSAPrivateKey
        get() = resolvedPrivateKey

    val publicKey: RSAPublicKey
        get() = resolvedPublicKey

    val keyId: String
        get() = resolvedKeyId

    @PostConstruct
    fun initialize() {
        val configuredPrivateKey = privateKeyPem.trim()
        val configuredPublicKey = publicKeyPem.trim()

        if (configuredPrivateKey.isNotBlank() && configuredPublicKey.isNotBlank()) {
            resolvedPrivateKey = parsePrivateKey(configuredPrivateKey)
            resolvedPublicKey = parsePublicKey(configuredPublicKey)
        } else {
            if (requireConfiguredKeys) {
                throw IllegalStateException(
                    "RSA_PRIVATE_KEY and RSA_PUBLIC_KEY must be configured when RSA_REQUIRE=true",
                )
            }

            val keyPair = KeyPairGenerator.getInstance("RSA")
                .apply { initialize(KEY_SIZE_BITS) }
                .generateKeyPair()
            resolvedPrivateKey = keyPair.private as RSAPrivateKey
            resolvedPublicKey = keyPair.public as RSAPublicKey
        }

        resolvedKeyId = calculateKeyId(resolvedPublicKey)
    }

    private fun parsePrivateKey(pem: String): RSAPrivateKey {
        val keyBytes = decodePem(pem, "PRIVATE KEY")
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun parsePublicKey(pem: String): RSAPublicKey {
        val keyBytes = decodePem(pem, "PUBLIC KEY")
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }

    private fun decodePem(pem: String, keyType: String): ByteArray {
        val normalizedPem = pem.replace("\\n", "\n")
        val base64 = normalizedPem
            .replace("-----BEGIN $keyType-----", "")
            .replace("-----END $keyType-----", "")
            .replace(Regex("\\s"), "")

        return Base64.getDecoder().decode(base64)
    }

    private fun calculateKeyId(publicKey: RSAPublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).take(KEY_ID_LENGTH)
    }

    companion object {
        private const val KEY_SIZE_BITS = 2048
        private const val KEY_ID_LENGTH = 16
    }
}
