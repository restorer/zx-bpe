package com.eightsines.bpe.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UnpackableStringBagTest {
    // Empty

    @Test
    fun shouldFailInvalidSignature() {
        assertFailsWith<BagUnpackException> { makeSut("") }
        assertFailsWith<BagUnpackException> { makeSut("Test") }
    }

    @Test
    fun shouldNotFailValidSignature() {
        makeSut("BAG1")
    }

    // Boolean.Nullable

    @Test
    fun shouldUnpackNullableBooleanNull() {
        val sut = makeSut("BAG1_")

        val actual = sut.getBooleanOrNull()
        assertNull(actual)
    }

    @Test
    fun shouldUnpackNullableBooleanFalse() {
        val sut = makeSut("BAG1b")

        val actual = sut.getBooleanOrNull()
        assertEquals(false, actual)
    }

    @Test
    fun shouldUnpackNullableBooleanTrue() {
        val sut = makeSut("BAG1B")

        val actual = sut.getBooleanOrNull()
        assertEquals(true, actual)
    }

    // Boolean.NonNull

    @Test
    fun shouldNotUnpackNonNullBooleanNull() {
        val sut = makeSut("BAG1_")

        assertFailsWith<BagUnpackException> { sut.getBoolean() }
    }

    @Test
    fun shouldUnpackNonNullBooleanFalse() {
        val sut = makeSut("BAG1b")

        val actual = sut.getBoolean()
        assertEquals(false, actual)
    }

    @Test
    fun shouldUnpackNonNullBooleanTrue() {
        val sut = makeSut("BAG1B")

        val actual = sut.getBoolean()
        assertEquals(true, actual)
    }

    // Int.Nullable

    @Test
    fun shouldUnpackNullableIntNull() {
        val sut = makeSut("BAG1_")

        val actual = sut.getIntOrNull()
        assertNull(actual)
    }

    @Test
    fun shouldUnpackNullableIntDigit() {
        val sut = makeSut("BAG1i0iFi8i1i7")

        val actual = listOf(
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
        )

        assertEquals(listOf(0, -1, -8, 1, 7), actual)
    }

    @Test
    fun shouldUnpackNullableIntByte() {
        val sut = makeSut("BAG1I80IC0I40I7F")

        val actual = listOf(
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
        )

        assertEquals(listOf(-128, -64, 64, 127), actual)
    }

    @Test
    fun shouldUnpackNullableIntShort() {
        val sut = makeSut("BAG1n8000nC000n4000n7FFF")

        val actual = listOf(
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
        )

        assertEquals(listOf(-32768, -16384, 16384, 32767), actual)
    }

    @Test
    fun shouldUnpackNullableIntInt() {
        val sut = makeSut("BAG1N80000000NC0000000N40000000N7FFFFFFF")

        val actual = listOf(
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
        )

        assertEquals(listOf(-2147483648, -1073741824, 1073741824, 2147483647), actual)
    }

    // Int.NonNull

    @Test
    fun shouldNotUnpackNonNullIntNull() {
        val sut = makeSut("BAG1_")

        assertFailsWith<BagUnpackException> { sut.getInt() }
    }

    @Test
    fun shouldUnpackNonNullIntDigit() {
        val sut = makeSut("BAG1i0iFi8i1i7")

        val actual = listOf(
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
        )

        assertEquals(listOf(0, -1, -8, 1, 7), actual)
    }

    @Test
    fun shouldUnpackNonNullIntByte() {
        val sut = makeSut("BAG1I80IC0I40I7F")

        val actual = listOf(
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
        )

        assertEquals(listOf(-128, -64, 64, 127), actual)
    }

    @Test
    fun shouldUnpackNonNullIntShort() {
        val sut = makeSut("BAG1n8000nC000n4000n7FFF")

        val actual = listOf(
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
            sut.getIntOrNull(),
        )

        assertEquals(listOf(-32768, -16384, 16384, 32767), actual)
    }

    @Test
    fun shouldUnpackNotNullIntInt() {
        val sut = makeSut("BAG1N80000000NC0000000N40000000N7FFFFFFF")

        val actual = listOf(
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
            sut.getInt(),
        )

        assertEquals(listOf(-2147483648, -1073741824, 1073741824, 2147483647), actual)
    }

    // String.Nullable

    @Test
    fun shouldUnpackNullableStringNull() {
        val sut = makeSut("BAG1_")

        val actual = sut.getStringOrNull()
        assertNull(actual)
    }

    @Test
    fun shouldUnpackNullableStringEmpty() {
        val sut = makeSut("BAG1s0")

        val actual = sut.getStringOrNull()
        assertEquals("", actual)
    }

    @Test
    fun shouldUnpackNullableStringDigitLength() {
        val sut = makeSut("BAG1s4Test")

        val actual = sut.getStringOrNull()
        assertEquals("Test", actual)
    }

    @Test
    fun shouldUnpackNullableStringByteLength() {
        val sut = makeSut("BAG1S10TestTestTestTest")

        val actual = sut.getStringOrNull()
        assertEquals("TestTestTestTest", actual)
    }

    @Test
    fun shouldUnpackNullableStringSequence() {
        val sut = makeSut("BAG1s4TestS10TestTestTestTest")

        val actual = listOf(sut.getStringOrNull(), sut.getStringOrNull())
        assertEquals(listOf("Test", "TestTestTestTest"), actual)
    }

    // String.NotNull

    @Test
    fun shouldNotUnpackNonNullStringNull() {
        val sut = makeSut("BAG1_")

        assertFailsWith<BagUnpackException> { sut.getString() }
    }

    @Test
    fun shouldUnpackNotNullStringEmpty() {
        val sut = makeSut("BAG1s0")

        val actual = sut.getString()
        assertEquals("", actual)
    }

    @Test
    fun shouldUnpackNotNullStringDigitLength() {
        val sut = makeSut("BAG1s4Test")

        val actual = sut.getString()
        assertEquals("Test", actual)
    }

    @Test
    fun shouldUnpackNotNullStringByteLength() {
        val sut = makeSut("BAG1S10TestTestTestTest")

        val actual = sut.getString()
        assertEquals("TestTestTestTest", actual)
    }

    @Test
    fun shouldUnpackNotNullStringSequence() {
        val sut = makeSut("BAG1s4TestS10TestTestTestTest")

        val actual = listOf(sut.getString(), sut.getString())
        assertEquals(listOf("Test", "TestTestTestTest"), actual)
    }

    // Stuff.Nullable

    @Test
    fun shouldUnpackNullableStuffNull() {
        val sut = makeSut("BAG1_")

        val actual = sut.getStuffOrNull(BagStuffStub)
        assertNull(actual)
    }

    @Test
    fun shouldUnpackNullableStuff() {
        val sut = makeSut("BAG1u1bI2As5Stuff")

        val actual = sut.getStuffOrNull(BagStuffStub)
        assertEquals(makeStuff(), actual)
    }

    // Stuff.NonNull

    @Test
    fun shouldUnpackNonNullStuffNull() {
        val sut = makeSut("BAG1_")

        assertFailsWith<BagUnpackException> { sut.getStuff(BagStuffStub) }
    }

    @Test
    fun shouldUnpackNonNullStuff() {
        val sut = makeSut("BAG1u1bI2As5Stuff")

        val actual = sut.getStuff(BagStuffStub)
        assertEquals(makeStuff(), actual)
    }

    // Multi

    @Test
    fun shouldUnpackNullableMulti() {
        val sut = makeSut("BAG1s4TestB_u1bI2As5StuffI2A")

        val actual = listOf(
            sut.getStringOrNull(),
            sut.getBooleanOrNull(),
            sut.getIntOrNull(),
            sut.getStuffOrNull(BagStuffStub),
            sut.getIntOrNull(),
        )

        assertEquals(listOf("Test", true, null, makeStuff(), 42), actual)
    }

    @Test
    fun shouldUnpackNonNullMulti() {
        val sut = makeSut("BAG1s4TestBu1bI2As5StuffI2A")

        val actual = listOf(
            sut.getString(),
            sut.getBoolean(),
            sut.getStuff(BagStuffStub),
            sut.getInt(),
        )

        assertEquals(listOf("Test", true, makeStuff(), 42), actual)
    }

    // Utils

    private fun makeSut(input: String) = UnpackableStringBag(input)

    private fun makeStuff() = BagStuffStub(
        booleanValue = false,
        intValue = 42,
        stringValue = "Stuff",
    )
}
