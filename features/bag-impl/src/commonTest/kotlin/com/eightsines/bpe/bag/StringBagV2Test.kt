package com.eightsines.bpe.bag

import com.eightsines.bpe.testing.TestStuff
import com.eightsines.bpe.testing.TestStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringBagV2Test {
    private fun createPackableBag(): PackableBag = PackableStringBagV2()
    private fun createUnpackableBag(packableBag: PackableBag): UnpackableBag = UnpackableStringBagV2(packableBag.toString())

    // Empty

    @Test
    fun shouldPackAndUnpackEmpty() {
        val packableBag = createPackableBag()
        val unpackableBag = createUnpackableBag(packableBag)

        assertFailsWith<BagUnpackException> { unpackableBag.getBooleanOrNull() }
    }

    // Boolean

    @Test
    fun shouldPackAndUnpackBooleanNull() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(null as Boolean?)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getBooleanOrNull()
        },
        assert = { assertEquals(null, it) },
    )

    @Test
    fun shouldPackAndUnpackBooleanFalse() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(false)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getBoolean()
        },
        assert = { assertEquals(false, it) },
    )

    @Test
    fun shouldPackAndUnpackBooleanTrue() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(false)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getBoolean()
        },
        assert = { assertEquals(false, it) },
    )

    // Int

    @Test
    fun shouldPackAndUnpackIntNull() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(null as Int?)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getIntOrNull()
        },
        assert = { assertEquals(null, it) },
    )

    @Test
    fun shouldPackAndUnpackInt4() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(0)
            packableBag.put(-1)
            packableBag.put(-8)
            packableBag.put(1)
            packableBag.put(7)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
            )
        },
        assert = { assertEquals(listOf(0, -1, -8, 1, 7), it) },
    )

    @Test
    fun shouldPackAndUnpackInt8() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(-128)
            packableBag.put(-64)
            packableBag.put(64)
            packableBag.put(127)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
            )
        },
        assert = { assertEquals(listOf(-128, -64, 64, 127), it) },
    )

    @Test
    fun shouldPackAndUnpackInt16() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(-32768)
            packableBag.put(-16384)
            packableBag.put(16384)
            packableBag.put(32767)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
            )
        },
        assert = { assertEquals(listOf(-32768, -16384, 16384, 32767), it) },
    )

    @Test
    fun shouldPackAndUnpackInt32() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(-2147483648)
            packableBag.put(-1073741824)
            packableBag.put(1073741824)
            packableBag.put(2147483647)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
                unpackableBag.getInt(),
            )
        },
        assert = { assertEquals(listOf(-2147483648, -1073741824, 1073741824, 2147483647), it) },
    )

    // String

    @Test
    fun shouldPackAndUnpackStringNull() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(null as String?)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getStringOrNull()
        },
        assert = { assertEquals(null, it) },
    )

    @Test
    fun shouldPackAndUnpackString4() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put("")
            packableBag.put("1")
            packableBag.put("1234")
            packableBag.put("1234567")

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getString(),
                unpackableBag.getString(),
                unpackableBag.getString(),
                unpackableBag.getString(),
            )
        },
        assert = { assertEquals(listOf("", "1", "1234", "1234567"), it) },
    )

    @Test
    fun shouldPackAndUnpackString8() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put("1234567890")
            packableBag.put("ThisIsTheTestAndYetAnotherThisIsTheTestAndYetAnother")

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getString(),
                unpackableBag.getString(),
            )
        },
        assert = {
            assertEquals(
                listOf("1234567890", "ThisIsTheTestAndYetAnotherThisIsTheTestAndYetAnother"),
                it,
            )
        },
    )

    @Test
    fun shouldPackAndUnpackString16() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TEST_STRING_16)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getString()
        },
        assert = { assertEquals(TEST_STRING_16, it) },
    )

    @Test
    fun shouldPackAndUnpackString32() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TEST_STRING_32)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getString()
        },
        assert = { assertEquals(TEST_STRING_32, it) },
    )

    // Stuff

    @Test
    fun shouldPackAndUnpackStuffNull() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TestStuff, null as TestStuff?)
            val unpackableBag = createUnpackableBag(packableBag)
            unpackableBag.getStringOrNull()
        },
        assert = { assertEquals(null, it) },
    )

    @Test
    fun shouldPackAndUnpackStuff4() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TestStuff.VersionedPackerUnpacker(0), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(-1), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(-8), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(1), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(7), TestStuffMother.TestStuff)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(0)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-1)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-8)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(1)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(7)),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackAndUnpackStuff8() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TestStuff.VersionedPackerUnpacker(-128), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(-64), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(64), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(127), TestStuffMother.TestStuff)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-128)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-64)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(64)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(127)),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackAndUnpackStuff16() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TestStuff.VersionedPackerUnpacker(-32768), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(-16384), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(16384), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(32767), TestStuffMother.TestStuff)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-32768)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-16384)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(16384)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(32767)),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackAndUnpackStuff32() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put(TestStuff.VersionedPackerUnpacker(-2147483648), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(-1073741824), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(1073741824), TestStuffMother.TestStuff)
            packableBag.put(TestStuff.VersionedPackerUnpacker(2147483647), TestStuffMother.TestStuff)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-2147483648)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(-1073741824)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(1073741824)),
                unpackableBag.getStuff(TestStuff.VersionedPackerUnpacker(2147483647)),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff
                ),
                it,
            )
        },
    )

    // Multi

    @Suppress("INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION")
    @Test
    fun shouldPackAndUnpackMulti() = performTest(
        arrange = { createPackableBag() },
        act = { packableBag ->
            packableBag.put("Test")
            packableBag.put(true)
            packableBag.put(null as Int?)
            packableBag.put(TestStuff, TestStuffMother.TestStuff)
            packableBag.put(42)

            val unpackableBag = createUnpackableBag(packableBag)

            listOf(
                unpackableBag.getString(),
                unpackableBag.getBoolean(),
                unpackableBag.getIntOrNull(),
                unpackableBag.getStuff(TestStuff),
                unpackableBag.getInt(),
            )
        },
        assert = { assertEquals(listOf("Test", true, null, TestStuffMother.TestStuff, 42), it) },
    )

    private companion object {
        private val TEST_STRING_16 = "1234567890".repeat(50)
        private val TEST_STRING_32 = "1234567890".repeat(10000)
    }
}
