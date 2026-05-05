package ru.underfish.authservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Configuration
class RsaKeyConfig(
    @Value("\${rsa.private-key:}") private val privateKeyPem: String,
    @Value("\${rsa.public-key:}") private val publicKeyPem: String,
    @Value("\${rsa.require:false}") private val requireKeys: Boolean,
) {
    @Bean
    fun rsaKeyPair(): KeyPair {
        if (privateKeyPem.isNotBlank() && publicKeyPem.isNotBlank()) {
            return loadFromPem(privateKeyPem, publicKeyPem)
        }
        if (requireKeys) {
            error("RSA_PRIVATE_KEY and RSA_PUBLIC_KEY are required when rsa.require=true")
        }
        // In dev/test: generate a transient key pair on startup.
        // For production, set RSA_PRIVATE_KEY and RSA_PUBLIC_KEY env vars
        // with base64-encoded PKCS8/X509 PEM content.
        return KeyPairGenerator.getInstance("RSA").also { it.initialize(2048) }.generateKeyPair()
    }

    private fun loadFromPem(
        privatePem: String,
        publicPem: String,
    ): KeyPair {
        val kf = KeyFactory.getInstance("RSA")
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(decodePem(privatePem)))
        val publicKey = kf.generatePublic(X509EncodedKeySpec(decodePem(publicPem)))
        return KeyPair(publicKey, privateKey)
    }

    private fun decodePem(pem: String): ByteArray =
        Base64.getDecoder().decode(
            pem.replace("-----BEGIN.*-----".toRegex(), "")
                .replace("-----END.*-----".toRegex(), "")
                .replace("\\s".toRegex(), ""),
        )
}
