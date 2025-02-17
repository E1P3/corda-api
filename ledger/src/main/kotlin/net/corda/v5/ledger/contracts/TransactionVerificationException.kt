package net.corda.v5.ledger.contracts

import net.corda.v5.application.flows.exceptions.FlowException
import net.corda.v5.base.annotations.CordaSerializable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.identity.Party
import net.corda.v5.ledger.services.AttachmentId
import net.corda.v5.membership.GroupParameters
import net.corda.v5.membership.GroupParameters.Companion.epoch


/**
 * The node asked a remote peer for the transaction identified by [hash] because it is a dependency of a transaction
 * being resolved, but the remote peer would not provide it.
 *
 * @property hash Merkle root of the transaction being resolved, see [WireTransaction.id][net.corda.v5.ledger.transactions.WireTransaction.id]
 */
open class TransactionResolutionException(val hash: SecureHash, message: String) : FlowException(message) {

    constructor(hash: SecureHash) : this(hash, message = "Transaction resolution failure for $hash")
    /**
     * Thrown if a transaction specifies a set of parameters that aren't stored locally yet verification is requested.
     * This should never normally happen because before verification comes resolution, and if a peer can't provide a
     * new set of parameters, [TransactionResolutionException] will have already been thrown beforehand.
     */
    class UnknownParametersException(txId: SecureHash, paramsHash: SecureHash) : TransactionResolutionException(txId,
            "Transaction specified group parameters $paramsHash but these parameters are not known.")
}

/**
 * The node asked a remote peer for the attachment identified by [hash] because it is a dependency of a transaction
 * being resolved, but the remote peer would not provide it.
 *
 * @property hash Hash of the bytes of the attachment, see [Attachment.id]
 */
class AttachmentResolutionException(val hash: AttachmentId) : FlowException("Attachment resolution failure for $hash")

/**
 * A non-specific exception for the attachment identified by [attachmentId]. The context
 * for this error is provided via the [message] and [cause].
 * @property attachmentId
 */
class BrokenAttachmentException(val attachmentId: AttachmentId, message: String?, cause: Throwable?)
    : FlowException("Attachment $attachmentId has error (${message ?: "no message"})", cause)

/**
 * Indicates that some aspect of the transaction named by [txId] violates the platform rules. The exact type of failure
 * is expressed using a subclass. TransactionVerificationException is a [FlowException] and thus when thrown inside
 * a flow, the details of the failure will be serialised, propagated to the peer and rethrown.
 *
 * @property txId the Merkle root hash (identifier) of the transaction that failed verification.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class TransactionVerificationException(val txId: SecureHash, message: String, cause: Throwable?)
    : FlowException("$message, transaction: $txId", cause) {

    /**
     * Indicates that one of the [Contract.verify] methods selected by the contract constraints and attachments
     * rejected the transaction by throwing an exception.
     *
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class ContractRejection(txId: SecureHash, val contractClass: String, cause: Throwable?, message: String) : TransactionVerificationException(txId, "Contract verification failed: $message, contract: $contractClass", cause) {
        constructor(txId: SecureHash, contract: Contract, cause: Throwable) : this(txId, contract.javaClass.name, cause, cause.message ?: "")
    }

    /**
     * This exception happens when a transaction was not built correctly.
     *
     * @property txId The transaction.
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class ConstraintPropagationRejection(txId: SecureHash, message: String) : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash,
                    contractClass: String,
                    inputConstraint: CPKConstraint,
                    outputConstraint: CPKConstraint
        ) :
                this(txId, "Contract constraints for $contractClass are not propagated correctly. " +
                        "The outputConstraint: $outputConstraint is not a valid transition from the input constraint: $inputConstraint.")

        // This is only required for backwards compatibility. In case the message format changes, update the index.
        val contractClass: String = message.split(" ")[3]
    }

    /**
     * The transaction attachment that contains the [contractClass] class didn't meet the constraints specified by
     * the [TransactionState.constraint] object. This usually implies a version mismatch of some kind.
     *
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class ContractConstraintRejection(txId: SecureHash, val contractClass: String)
        : TransactionVerificationException(txId, "Contract constraints failed for $contractClass", null)

    /**
     * A constraint attached to a state was invalid, e.g. due to size limitations.
     *
     * @property contractClass The fully qualified class name of the failing contract.
     * @property reason a message containing the reason the constraint is invalid included in thrown the exception.
     */
    class InvalidConstraintRejection(txId: SecureHash, val contractClass: String, val reason: String)
        : TransactionVerificationException(txId, "Contract constraints failed for $contractClass. $reason", null)

    /**
     * A state requested a contract class via its [TransactionState.contract] field that didn't appear in any attached
     * JAR at all. This usually implies the attachments were forgotten or a version mismatch.
     *
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class MissingAttachmentRejection(txId: SecureHash, val contractClass: String)
        : TransactionVerificationException(txId, "Contract constraints failed, could not find attachment for: $contractClass", null)

    /**
     * Indicates this transaction violates the "no overlap" rule: two attachments are trying to provide the same file
     * path. Whereas Java classpaths would normally allow that with the first class taking precedence, this is not
     * allowed in transactions for security reasons. This usually indicates that two separate apps share a dependency,
     * in which case you could try 'shading the fat jars' to rename classes of dependencies. Or you could manually
     * attach dependency JARs when building the transaction.
     *
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class ConflictingAttachmentsRejection(txId: SecureHash, val contractClass: String)
        : TransactionVerificationException(txId, "Contract constraints failed for: $contractClass, because multiple attachments providing this contract were attached.", null)

    /**
     * Indicates that the same attachment has been added multiple times to a transaction.
     */
    class DuplicateAttachmentsRejection(txId: SecureHash, val attachmentId: Attachment)
        : TransactionVerificationException(txId, "The attachment: $attachmentId was added multiple times.", null)

    /**
     * A [Contract] class named by a state could not be constructed. Most likely you do not have a no-argument
     * constructor, or the class doesn't subclass [Contract].
     *
     * @property contractClass The fully qualified class name of the failing contract.
     */
    class ContractCreationError internal constructor(txId: SecureHash, val contractClass: String, cause: Throwable?, message: String)
        : TransactionVerificationException(txId, "Contract verification failed: $message, could not create contract class: $contractClass", cause) {
        constructor(txId: SecureHash, contractClass: String, cause: Throwable) : this(txId, contractClass, cause, cause.message ?: "")
    }

    /**
     * An output state has a notary that doesn't match the transaction's notary field. It must!
     *
     * @property txNotary the [Party] specified by the transaction header.
     * @property outputNotary the [Party] specified by the errant state.
     */
    class NotaryChangeInWrongTransactionType(txId: SecureHash, val txNotary: Party, val outputNotary: Party)
        : TransactionVerificationException(txId, "Found unexpected notary change in transaction. Tx notary: $txNotary, found: $outputNotary", null)

    /**
     * If a state is encumbered (the [TransactionState.encumbrance] field is set) then its encumbrance must be used
     * as an input to any transaction that uses it. In this way states can be tied together in chains, thus composing
     * logic. Note that encumbrances aren't fully supported by all aspects of the platform at this time so if you use
     * them, you may find transactions created by the platform don't always respect the encumbrance rule.
     *
     * @property missing the index of the state missing the encumbrance.
     * @property inOut whether the issue exists in the input list or output list.
     */
    class TransactionMissingEncumbranceException(txId: SecureHash, val missing: Int, val inOut: Direction)
        : TransactionVerificationException(txId, "Missing required encumbrance $missing in $inOut", null)

    /**
     * If two or more states refer to another state (as their encumbrance), then the bi-directionality property cannot
     * be satisfied.
     */
    class TransactionDuplicateEncumbranceException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, index: Int) : this(txId, "The bi-directionality property of encumbered output states " +
                "is not satisfied. Index $index is referenced more than once")
    }

    /**
     * An encumbered state should also be referenced as the encumbrance of another state in order to satisfy the
     * bi-directionality property (a full cycle should be present).
     */
    class TransactionNonMatchingEncumbranceException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, nonMatching: Collection<Int>) : this(txId,
                "The bi-directionality property of encumbered output states " +
                "is not satisfied. Encumbered states should also be referenced as an encumbrance of another state to form " +
                "a full cycle. Offending indices $nonMatching")
    }

    /**
     * All encumbered states should be assigned to the same notary. This is due to the fact that multi-notary
     * transactions are not supported and thus two encumbered states with different notaries cannot be consumed
     * in the same transaction.
     */
    class TransactionNotaryMismatchEncumbranceException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, encumberedIndex: Int, encumbranceIndex: Int, encumberedNotary: Party, encumbranceNotary: Party) :
                this(txId, "Encumbered output states assigned to different notaries found. " +
                        "Output state with index $encumberedIndex is assigned to notary [$encumberedNotary], " +
                        "while its encumbrance with index $encumbranceIndex is assigned to notary [$encumbranceNotary]")
    }

    /**
     * If a state is identified as belonging to a contract, either because the state class is defined as an inner class
     * of the contract class or because the state class is annotated with [BelongsToContract], then it must not be
     * bundled in a [TransactionState] with a different contract.
     *
     * @param state The [TransactionState] whose bundled state and contract are in conflict.
     * @param requiredContractClassName The class name of the contract to which the state belongs.
     */
    class TransactionContractConflictException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, state: TransactionState<ContractState>, requiredContractClassName: String): this(txId,
        """
            State of class ${state.data ::class.java.typeName} belongs to contract $requiredContractClassName, but
            is bundled in TransactionState with ${state.contract}.
            """.trimIndent().replace('\n', ' '))
    }

    // TODO: add reference to documentation
    class TransactionRequiredContractUnspecifiedException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, state: TransactionState<ContractState>) : this(txId,
                """
            State of class ${state.data::class.java.typeName} does not have a specified owning contract.
            Add the @BelongsToContract annotation to this class to ensure that it can only be bundled in a TransactionState
            with the correct contract.
            """.trimIndent())
    }

    /**
     * If the group parameters associated with an input or reference state in a transaction are more recent than the group parameters of the new transaction itself.
     */
    class TransactionGroupParameterOrderingException(txId: SecureHash, message: String) :
            TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash,
                    inputStateRef: StateRef,
                    txnGroupParameters: GroupParameters,
                    inputGroupParameters: GroupParameters)
                : this(txId, "The group parameters epoch (${txnGroupParameters.epoch}) of this transaction " +
                "is older than the epoch (${inputGroupParameters.epoch}) of input state: $inputStateRef")
    }

    /**
     * Thrown when the group parameters with hash: missingGroupParametersHash is not available at this node. Usually all the parameters
     * that are in the resolution chain for transaction with txId should be fetched from peer via [FetchParametersFlow] or from member lookup.
     *
     * @param txId Id of the transaction that has missing parameters hash in the resolution chain
     * @param missingGroupParametersHash Missing hash of the group parameters associated to this transaction
     */
    class MissingGroupParametersException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null) {
        constructor(txId: SecureHash, missingGroupParametersHash: SecureHash) :
                this(txId, "Couldn't find group parameters with hash: $missingGroupParametersHash related to this transaction: $txId")
    }

    /**
     * @param txId Id of the transaction that Corda is no longer able to verify.
     */
    class BrokenTransactionException(txId: SecureHash, message: String)
        : TransactionVerificationException(txId, message, null)

    /** Whether the inputs or outputs list contains an encumbrance issue, see [TransactionMissingEncumbranceException]. */
    @CordaSerializable
    enum class Direction {
        /** Issue in the inputs list. */
        INPUT,
        /** Issue in the outputs list. */
        OUTPUT
    }

    class InvalidAttachmentException(txId: SecureHash, @Suppress("unused") val attachmentHash: AttachmentId) : TransactionVerificationException(txId,
            "The attachment $attachmentHash is not a valid ZIP or JAR file.".trimIndent(), null)

    class UnsupportedClassVersionError(txId: SecureHash, message: String, cause: Throwable) : TransactionVerificationException(txId, message, cause)

    // TODO: Make this descend from TransactionVerificationException so that untrusted attachments cause flows to be hospitalized.
    /** Thrown during classloading upon encountering an untrusted attachment (eg. not in the [TRUSTED_UPLOADERS] list) */
    class UntrustedAttachmentsException(val txId: SecureHash, val ids: List<SecureHash>) :
        CordaRuntimeException("Attempting to load untrusted transaction attachments: $ids. " +
                    "You will need to manually install the CorDapp to whitelist it for use.")

    class UnsupportedHashTypeException(txId: SecureHash) : TransactionVerificationException(txId, "The transaction Id is defined by an unsupported hash type", null);

    /*
    If you add a new class extending [TransactionVerificationException], please add a test in `TransactionVerificationExceptionSerializationTests`
    proving that it can actually be serialised. As a rule, exceptions intended to be serialised _must_ have a corresponding readable property
    for every named constructor parameter - so make your constructor parameters `val`s even if nothing other than the serializer is ever
    going to read them.
    */
}
