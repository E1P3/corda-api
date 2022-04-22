package net.corda.v5.membership

import net.corda.v5.base.annotations.CordaSerializable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.base.util.NetworkHostAndPort
import net.corda.v5.crypto.PublicKeyHash
import java.security.PublicKey
import java.time.Instant

/**
 * The member info consist of two parts:
 * Member provided context: Parameters added and signed by member as part of the initial MemberInfo proposal.
 * MGM provided context: Parameters added by MGM as a part of member acceptance.
 */
@CordaSerializable
interface MemberInfo {

    /**
     * Context representing the member set data regarding this members information.
     * Required data from this context is parsed nd returned via other class properties.
     */
    val memberProvidedContext: MemberContext

    /**
     * Context representing the MGM set data regarding this members information.
     * Required data from this context is parsed nd returned via other class properties.
     */
    val mgmProvidedContext: MGMContext

    /**
     * Member's X.500 name.
     * x.500 name is unique within the group and cannot be changed while the membership exists.
     */
    val name: MemberX500Name

    /**
     * Group identifier. UUID as a String.
     */
    val groupId: String

    /**
     * Member's identity key.
     */
    val owningKey: PublicKey

    /**
     * List of current and previous (rotated) identity keys, which member can still use to sign unspent transactions on ledger.
     */
    val identityKeys: List<PublicKey>

    /**
     * List of P2P endpoints for member's node.
     */
    val addresses: List<NetworkHostAndPort>

    /**
     * List of P2P endpoints for member's node.
     */
    val endpoints: List<EndpointInfo>

    /** Corda platform version */
    val platformVersion: Int

    /**
     * Corda-Release-Version.
     */
    val softwareVersion: String

    /**
     * An arbitrary number incremented each time the [MemberInfo] is changed.
     */
    val serial: Long

    /**
     * Checks the status of the member.
     */
    val isActive: Boolean

    /**
     * Status of Membership.
     */
    val status: String

    /**
     * The last time Membership was modified. Can be null.
     * MGM won't have modifiedTime,
     * also Members won't have it at the beginning when
     * they create their MemberInfo proposals
     * this is because only MGM can populate this information.
     */
    val modifiedTime: Instant?

    /**
     * Collection of identity key hashes for member's node.
     */
    val identityKeyHashes: Collection<PublicKeyHash>

    /**
     * Returns boolean value to indicate if the member info is for an MGM.
     */
    val isMgm: Boolean
}
