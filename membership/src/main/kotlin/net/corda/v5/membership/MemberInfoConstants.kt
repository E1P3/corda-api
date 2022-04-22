@file:JvmName("MemberInfoConstants")
package net.corda.v5.membership

/** Key name for identity keys property. */
const val IDENTITY_KEYS = "corda.identityKeys"
const val IDENTITY_KEYS_KEY = "corda.identityKeys.%s"

/** Key name for identity key hashes property. */
const val IDENTITY_KEY_HASHES = "corda.identityKeyHashes"
const val IDENTITY_KEY_HASHES_KEY = "corda.identityKeyHashes.%s"

/** Key name for platform version property. */
const val PLATFORM_VERSION = "corda.platformVersion"

/** Key name for party property. */
const val PARTY_NAME = "corda.party.name"
const val PARTY_OWNING_KEY = "corda.party.owningKey"

/** Key name for notary service property. */
const val NOTARY_SERVICE_PARTY_NAME = "corda.notaryServiceParty.name"
const val NOTARY_SERVICE_PARTY_KEY = "corda.notaryServiceParty.owningKey"

/** Key name for serial property. */
const val SERIAL = "corda.serial"

/** Key name for status property. */
const val STATUS = "corda.status"

/** Key name for creation time **/
const val CREATION_TIME = "corda.creationTime"

/** Key name for endpoints property. */
const val ENDPOINTS = "corda.endpoints"
const val URL_KEY = "corda.endpoints.%s.connectionURL"
const val PROTOCOL_VERSION = "corda.endpoints.%s.protocolVersion"

/** Key name for softwareVersion property. */
const val SOFTWARE_VERSION = "corda.softwareVersion"

/** Key name for group identifier property. */
const val GROUP_ID = "corda.groupId"

/** Key name for certificate property. */
const val CERTIFICATE = "corda.certificate"

/** Key name for modified time property. */
const val MODIFIED_TIME = "corda.modifiedTime"

/** Key name for MGM property. */
const val IS_MGM = "corda.mgm"

/** Key name for the ID of the registration in which the current member info was approved. */
const val REGISTRATION_ID = "corda.registration.id"

/** Active nodes can transact in the Membership Group with the other nodes. **/
const val MEMBER_STATUS_ACTIVE = "ACTIVE"

/**
 * Membership request has been submitted but Group Manager still hasn't responded to it. Nodes with this status can't
 * communicate with the other nodes in the Membership Group.
 */
const val MEMBER_STATUS_PENDING = "PENDING"

/**
 * Suspended nodes can't communicate with the other nodes in the Membership Group. They are still members of the Membership Group
 * meaning they can be re-activated.
 */
const val MEMBER_STATUS_SUSPENDED = "SUSPENDED"
