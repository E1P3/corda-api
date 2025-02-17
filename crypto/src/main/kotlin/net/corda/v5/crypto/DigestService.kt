package net.corda.v5.crypto

import java.io.InputStream

/**
 * Basic hashing service, handling hashing of bytes.
 */
interface DigestService {

    /**
     * Computes the digest of the [ByteArray].
     *
     * @param bytes The [ByteArray] to hash.
     * @param digestAlgorithmName The digest algorithm to be used for hashing.
     */
    fun hash(bytes: ByteArray, digestAlgorithmName: DigestAlgorithmName): SecureHash

    /**
     * Computes the digest of the [InputStream].
     *
     * @param inputStream The [InputStream] to hash.
     * @param digestAlgorithmName The digest algorithm to be used for hashing.
     */
    fun hash(inputStream : InputStream, digestAlgorithmName: DigestAlgorithmName): SecureHash

    /**
     * Returns the [DigestAlgorithmName] digest length in bytes.
     *
     * @param digestAlgorithmName The digest algorithm to get the digest length for.
     */
    fun digestLength(digestAlgorithmName: DigestAlgorithmName): Int
}