package net.corda.packaging.converters

import net.corda.packaging.CPI
import net.corda.packaging.CPK
import net.corda.packaging.CordappManifest
import net.corda.packaging.ManifestCordappInfo
import net.corda.v5.crypto.DigestAlgorithmName
import net.corda.v5.crypto.SecureHash
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Random
import java.util.TreeSet

class ConvertersTest {

    private val random = Random(0)

    private val cpiId = CPI.Identifier.newInstance("SomeName",
        "1.0",
        SecureHash(DigestAlgorithmName.DEFAULT_ALGORITHM_NAME.name, ByteArray(32).also(random::nextBytes))
    )
    private val cpkId = CPK.Identifier.newInstance("SomeName",
        "1.0", SecureHash(DigestAlgorithmName.DEFAULT_ALGORITHM_NAME.name, ByteArray(32).also(random::nextBytes)))
    private val cpkType = CPK.Type.CORDA_API
    private val cpkFormatVersion = CPK.FormatVersion.newInstance(2, 3)
    private val cpkManifest = CPK.Manifest.newInstance(CPK.FormatVersion.newInstance(2, 3))
    private val manifestCordappInfo = ManifestCordappInfo("someName", "R3", 42, "some license")
    private val cordappManifest = CordappManifest(
        "net.corda.Bundle",
        "1.2.3",
        12,
        34,
        ManifestCordappInfo("someName", "R3", 42, "some license"),
        ManifestCordappInfo("someName", "R3", 42, "some license"),
        mapOf("Corda-Contract-Classes" to "contractClass1, contractClass2",
            "Corda-Flow-Classes" to "flowClass1, flowClass2"),
    )

    private val cpkMetadata = CPK.Metadata.newInstance(cpkManifest,
        "mainBundle.jar",
        listOf("library.jar"),
        sequenceOf(cpkId).toCollection(TreeSet()),
        cordappManifest,
        cpkType,
        SecureHash(DigestAlgorithmName.DEFAULT_ALGORITHM_NAME.name, ByteArray(32).also(random::nextBytes)),
        emptySet()
    )

    private companion object {

        fun assertCpkMetadataEquals(cpkMetadata1 : CPK.Metadata, cpkMetadata2 : CPK.Metadata) {
            Assertions.assertEquals(cpkMetadata1.id, cpkMetadata2.id)
            Assertions.assertEquals(cpkMetadata1.mainBundle, cpkMetadata2.mainBundle)
            Assertions.assertEquals(cpkMetadata1.libraries, cpkMetadata2.libraries)
            Assertions.assertEquals(cpkMetadata1.dependencies, cpkMetadata2.dependencies)
            assertCordappManifestEquals(cpkMetadata1.cordappManifest, cpkMetadata2.cordappManifest)
            Assertions.assertEquals(cpkMetadata1.type, cpkMetadata2.type)
            Assertions.assertEquals(cpkMetadata1.hash, cpkMetadata2.hash)
            Assertions.assertEquals(cpkMetadata1.signers, cpkMetadata2.signers)
        }

        fun assertCordappManifestEquals(m1 : CordappManifest, m2 : CordappManifest) {
            sequenceOf(
                CordappManifest::bundleSymbolicName,
                CordappManifest::bundleVersion,
                CordappManifest::minPlatformVersion,
                CordappManifest::targetPlatformVersion,
                CordappManifest::contractInfo,
                CordappManifest::workflowInfo,
                CordappManifest::attributes,
            ).forEach {
                Assertions.assertEquals(it(m1), it(m2))
            }
        }

        fun assertCPIMetadataEquals(cpiMetadata1: CPI.Metadata, cpiMetadata2: CPI.Metadata) {
            Assertions.assertEquals(cpiMetadata1.id, cpiMetadata2.id)
            Assertions.assertEquals(cpiMetadata1.hash, cpiMetadata2.hash)
            Assertions.assertEquals(cpiMetadata1.cpks.size, cpiMetadata2.cpks.size)
            cpiMetadata1.cpks.asSequence().zip(cpiMetadata2.cpks.asSequence()).forEach {
                Assertions.assertEquals(it.first, it.second)
            }
            Assertions.assertEquals(cpiMetadata1.groupPolicy, cpiMetadata2.groupPolicy)
        }
    }

    @Test
    fun `CPK․Identifier round trip`() {
        val original = cpkId
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CPK․Identifier without signerSummaryHash round trip`() {
        val original = CPK.Identifier.newInstance("SomeName", "1.0", null)
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CPK․Type round trip`() {
        val original = cpkType
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CPK․FormatVersion round trip`() {
        val original = cpkFormatVersion
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CPK․Manifest round trip`() {
        val original = cpkManifest
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original.cpkFormatVersion, cordaObject.cpkFormatVersion)
    }

    @Test
    fun `ManifestCordappInfo round trip`() {
        val original = manifestCordappInfo
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CordappManifest round trip`() {
        val original = cordappManifest
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        assertCordappManifestEquals(original, cordaObject)
    }

    @Test
    fun `CPK․Metadata round trip`() {
        val original = CPK.Metadata.newInstance(cpkManifest,
            "mainBundle.jar",
            listOf("library.jar"),
            sequenceOf(cpkId).toCollection(TreeSet()),
            cordappManifest,
            cpkType,
            SecureHash(DigestAlgorithmName.DEFAULT_ALGORITHM_NAME.name, ByteArray(32).also(random::nextBytes)),
            emptySet(),
        )
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        assertCpkMetadataEquals(original, cordaObject)
    }

    @Test
    fun `CPI․Identifier round trip`() {
        val original = cpiId
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }

    @Test
    fun `CPI․Identifier without signerSummaryHash round trip`() {
        val original = CPI.Identifier.newInstance("SomeName",
            "1.0",
            null
        )
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        Assertions.assertEquals(original, cordaObject)
    }


    @Test
    fun `CPI․Metadata round trip`() {
        val original = CPI.Metadata.newInstance(
            cpiId,
            SecureHash(DigestAlgorithmName.DEFAULT_ALGORITHM_NAME.name, ByteArray(32).also(random::nextBytes)),
            listOf(cpkMetadata),
            "someString", emptySet())
        val avroObject = original.toAvro()
        val cordaObject = avroObject.toCorda()
        assertCPIMetadataEquals(original, cordaObject)
    }
}
