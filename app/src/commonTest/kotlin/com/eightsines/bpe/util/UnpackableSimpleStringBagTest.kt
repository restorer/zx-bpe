package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestBagStuff
import com.eightsines.bpe.testing.TestBagStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UnpackableSimpleStringBagTest {
    // Empty

    @Test
    fun shouldFailInvalidSignature() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("") }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("Test") }
    }

    @Test
    fun shouldNotFailValidSignature() {
        UnpackableSimpleStringBag("BAG1")
    }

    // Boolean.Nullable

    @Test
    fun shouldNotUnpackNullableNonBoolean() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getBooleanOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getBooleanOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getBooleanOrNull() }
    }

    @Test
    fun shouldUnpackNullableBooleanNull() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1_") },
        act = { it.getBooleanOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableBooleanFalse() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1b") },
        act = { it.getBooleanOrNull() },
        assert = { assertEquals(false, it) },
    )

    @Test
    fun shouldUnpackNullableBooleanTrue() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1B") },
        act = { it.getBooleanOrNull() },
        assert = { assertEquals(true, it) },
    )

    // Boolean.NonNull

    @Test
    fun shouldNotUnpackNonNullNonBoolean() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getBoolean() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getBoolean() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getBoolean() }
    }

    @Test
    fun shouldNotUnpackNonNullBooleanNull() {
        val sut = UnpackableSimpleStringBag("BAG1_")
        assertFailsWith<BagUnpackException> { sut.getBoolean() }
    }

    @Test
    fun shouldUnpackNonNullBooleanFalse() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1b") },
        act = { it.getBoolean() },
        assert = { assertEquals(false, it) },
    )

    @Test
    fun shouldUnpackNonNullBooleanTrue() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1B") },
        act = { it.getBoolean() },
        assert = { assertEquals(true, it) },
    )

    // Int.Nullable

    @Test
    fun shouldNotUnpackNullableNonInt() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getIntOrNull() }
    }

    @Test
    fun shouldUnpackNullableIntNull() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1_") },
        act = { it.getIntOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableIntDigit() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1i0iFi8i1i7") },
        act = {
            listOf(
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf(0, -1, -8, 1, 7), it) },
    )

    @Test
    fun shouldUnpackNullableIntByte() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1I80IC0I40I7F") },
        act = {
            listOf(
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf(-128, -64, 64, 127), it) },
    )

    @Test
    fun shouldUnpackNullableIntShort() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1n8000nC000n4000n7FFF") },
        act = {
            listOf(
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf(-32768, -16384, 16384, 32767), it) },
    )

    @Test
    fun shouldUnpackNullableIntInt() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1N80000000NC0000000N40000000N7FFFFFFF") },
        act = {
            listOf(
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf(-2147483648, -1073741824, 1073741824, 2147483647), it) },
    )

    // Int.NonNull

    @Test
    fun shouldNotUnpackNonNullNonInt() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getInt() }
    }

    @Test
    fun shouldNotUnpackNonNullIntNull() {
        val sut = UnpackableSimpleStringBag("BAG1_")
        assertFailsWith<BagUnpackException> { sut.getInt() }
    }

    @Test
    fun shouldUnpackNonNullIntDigit() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1i0iFi8i1i7") },
        act = {
            listOf(
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
            )
        },
        assert = { assertEquals(listOf(0, -1, -8, 1, 7), it) },
    )

    @Test
    fun shouldUnpackNonNullIntByte() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1I80IC0I40I7F") },
        act = {
            listOf(
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
            )
        },
        assert = { assertEquals(listOf(-128, -64, 64, 127), it) },
    )

    @Test
    fun shouldUnpackNonNullIntShort() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1n8000nC000n4000n7FFF") },
        act = {
            listOf(
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf(-32768, -16384, 16384, 32767), it) },
    )

    @Test
    fun shouldUnpackNotNullIntInt() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1N80000000NC0000000N40000000N7FFFFFFF") },
        act = {
            listOf(
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
            )
        },
        assert = { assertEquals(listOf(-2147483648, -1073741824, 1073741824, 2147483647), it) },
    )

    // String.Nullable

    @Test
    fun shouldNotUnpackNullableNonString() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getStringOrNull() }
    }

    @Test
    fun shouldUnpackNullableStringNull() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1_") },
        act = { it.getStringOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableStringEmpty() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s0") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("", it) },
    )

    @Test
    fun shouldUnpackNullableStringDigitLength() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4Test") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("Test", it) },
    )

    @Test
    fun shouldUnpackNullableStringByteLength() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1S10TestTestTestTest") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("TestTestTestTest", it) },
    )

    @Test
    fun shouldUnpackNullableStringSequence() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4TestS10TestTestTestTest") },
        act = { listOf(it.getStringOrNull(), it.getStringOrNull()) },
        assert = { assertEquals(listOf("Test", "TestTestTestTest"), it) },
    )

    // String.NotNull

    @Test
    fun shouldNotUnpackNonNullNonString() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getString() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getString() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getString() }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1u1").getString() }
    }

    @Test
    fun shouldNotUnpackNonNullStringNull() {
        val sut = UnpackableSimpleStringBag("BAG1_")
        assertFailsWith<BagUnpackException> { sut.getString() }
    }

    @Test
    fun shouldUnpackNotNullStringEmpty() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s0") },
        act = { it.getString() },
        assert = { assertEquals("", it) },
    )

    @Test
    fun shouldUnpackNotNullStringDigitLength() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4Test") },
        act = { it.getString() },
        assert = { assertEquals("Test", it) },
    )

    @Test
    fun shouldUnpackNotNullStringByteLength() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1S10TestTestTestTest") },
        act = { it.getString() },
        assert = { assertEquals("TestTestTestTest", it) },
    )

    @Test
    fun shouldUnpackNotNullStringSequence() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4TestS10TestTestTestTest") },
        act = { listOf(it.getString(), it.getString()) },
        assert = { assertEquals(listOf("Test", "TestTestTestTest"), it) },
    )

    // Stuff.Nullable

    @Test
    fun shouldNotUnpackNullableNonStuff() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getStuffOrNull(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNullableStuffNull() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1_") },
        act = { it.getStuffOrNull(TestBagStuff) },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableStuff() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1u1bI2As5Stuff") },
        act = { it.getStuffOrNull(TestBagStuff) },
        assert = { assertEquals(TestBagStuffMother.TestStuff, it) },
    )

    // Stuff.NonNull

    @Test
    fun shouldNotUnpackNonNullNonStuff() {
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1b").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1B").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1i1").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableSimpleStringBag("BAG1s1S").getStuff(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNonNullStuffNull() {
        val sut = UnpackableSimpleStringBag("BAG1_")
        assertFailsWith<BagUnpackException> { sut.getStuff(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNonNullStuff() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1u1bI2As5Stuff") },
        act = { it.getStuff(TestBagStuff) },
        assert = { assertEquals(TestBagStuffMother.TestStuff, it) },
    )

    // Multi

    @Test
    fun shouldUnpackNullableMulti() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4TestB_u1bI2As5StuffI2A") },
        act = {
            listOf(
                it.getStringOrNull(),
                it.getBooleanOrNull(),
                it.getIntOrNull(),
                it.getStuffOrNull(TestBagStuff),
                it.getIntOrNull(),
            )
        },
        assert = { assertEquals(listOf("Test", true, null, TestBagStuffMother.TestStuff, 42), it) }
    )

    @Test
    fun shouldUnpackNonNullMulti() = performTest(
        arrange = { UnpackableSimpleStringBag("BAG1s4TestBu1bI2As5StuffI2A") },
        act = {
            listOf(
                it.getString(),
                it.getBoolean(),
                it.getStuff(TestBagStuff),
                it.getInt(),
            )
        },
        assert = { assertEquals(listOf("Test", true, TestBagStuffMother.TestStuff, 42), it) },
    )
}
