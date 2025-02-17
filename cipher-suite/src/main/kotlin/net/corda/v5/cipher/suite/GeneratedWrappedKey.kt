package net.corda.v5.cipher.suite

import java.security.PublicKey

/**
 * Holding class for the returned by the [CryptoService] wrapped key pair.
 *
 * @property publicKey The public key of the pair.
 * @property keyMaterial The encoded and encrypted private key.
 * @property encodingVersion The encoding version which was used to encode the private key.
 */
class GeneratedWrappedKey(
    override val publicKey: PublicKey,
    val keyMaterial: ByteArray,
    val encodingVersion: Int
) : GeneratedKey
