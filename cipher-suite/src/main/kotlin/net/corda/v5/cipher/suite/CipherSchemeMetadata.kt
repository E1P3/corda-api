package net.corda.v5.cipher.suite

import net.corda.v5.cipher.suite.schemes.DigestScheme
import net.corda.v5.cipher.suite.schemes.KeyScheme
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import java.security.KeyFactory
import java.security.Provider
import java.security.PublicKey
import java.security.SecureRandom
import java.util.*

/**
 * Service which provides metadata about cipher suite, such as available key schemes,
 * digests, security providers, key factories, and [SecureRandom] instance.
 */
interface CipherSchemeMetadata : KeyEncodingService, AlgorithmParameterSpecEncodingService {
    companion object {
        /**
         * List of digest algorithms that must not be used due their vulnerabilities.
         */
        @JvmField
        val BANNED_DIGESTS: Set<String> = Collections.unmodifiableSet(setOf(
            "MD5",
            "MD2",
            "SHA-1",
            "MD4",
            "HARAKA-256",
            "HARAKA-512"
        ))
    }

    /**
     * The map of initialized security providers where the key is the provider name.
     */
    val providers: Map<String, Provider>

    /**
     * The list of all available key schemes for the cipher suite.
     */
    val schemes: Array<KeyScheme>

    /**
     * The list of all available digest algorithms for the cipher suite with the provider name which implements it.
     */
    val digests: Array<DigestScheme>

    /** Get an instance of [SecureRandom] */
    val secureRandom: SecureRandom

    /**
     * Find the corresponding [KeyScheme] based on its [AlgorithmIdentifier]
     *
     * @throws [IllegalArgumentException] if the scheme is not supported
     */
    fun findKeyScheme(algorithm: AlgorithmIdentifier): KeyScheme

    /**
     * Find the corresponding [KeyScheme] based on the type of the input [PublicKey].
     *
     * @throws IllegalArgumentException if the requested key type is not supported.
     */
    fun findKeyScheme(key: PublicKey): KeyScheme

    /**
     * Find the corresponding [KeyScheme] based on the code name.
     *
     * @throws IllegalArgumentException if the requested key type is not supported.
     */
    fun findKeyScheme(codeName: String): KeyScheme

    /**
     * Find the corresponding [KeyFactory] based on the [KeyScheme].
     *
     * @throws IllegalArgumentException if the requested key type is not supported.
     */
    fun findKeyFactory(scheme: KeyScheme): KeyFactory
}