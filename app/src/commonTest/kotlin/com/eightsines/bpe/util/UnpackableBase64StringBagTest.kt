package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestBagStuff
import com.eightsines.bpe.testing.TestBagStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UnpackableBase64StringBagTest {
    // Empty

    @Test
    fun shouldFailInvalidSignature() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("") }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("Test") }
    }

    @Test
    fun shouldNotFailValidSignature() {
        UnpackableBase64StringBag("BAG2")
    }

    // Boolean.Nullable

    @Test
    fun shouldNotUnpackNullableNonBoolean() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getBooleanOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getBooleanOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getBooleanOrNull() }
    }

    @Test
    fun shouldUnpackNullableBooleanNull() = performTest(
        arrange = { UnpackableBase64StringBag("BAG24A==") },
        act = { it.getBooleanOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableBooleanFalse() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2gA==") },
        act = { it.getBooleanOrNull() },
        assert = { assertEquals(false, it) },
    )

    @Test
    fun shouldUnpackNullableBooleanTrue() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2kA==") },
        act = { it.getBooleanOrNull() },
        assert = { assertEquals(true, it) },
    )

    // Boolean.NonNull

    @Test
    fun shouldNotUnpackNonNullNonBoolean() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getBoolean() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getBoolean() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getBoolean() }
    }

    @Test
    fun shouldNotUnpackNonNullBooleanNull() {
        val sut = UnpackableBase64StringBag("BAG24A==")
        assertFailsWith<BagUnpackException> { sut.getBoolean() }
    }

    @Test
    fun shouldUnpackNonNullBooleanFalse() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2gA==") },
        act = { it.getBoolean() },
        assert = { assertEquals(false, it) },
    )

    @Test
    fun shouldUnpackNonNullBooleanTrue() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2kA==") },
        act = { it.getBoolean() },
        assert = { assertEquals(true, it) },
    )

    // Int.Nullable

    @Test
    fun shouldNotUnpackNullableNonInt() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getIntOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getIntOrNull() }
    }

    @Test
    fun shouldUnpackNullableIntNull() = performTest(
        arrange = { UnpackableBase64StringBag("BAG24A==") },
        act = { it.getIntOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableIntDigit() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2APIBHA==") },
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
        arrange = { UnpackableBase64StringBag("BAG2pAUwKIFH8A==") },
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
        arrange = { UnpackableBase64StringBag("BAG2rAAFcAAqgAFX//A=") },
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
        arrange = { UnpackableBase64StringBag("BAG29AAAAAewAAAAPIAAAAHn////8A==") },
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
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getInt() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getInt() }
    }

    @Test
    fun shouldNotUnpackNonNullIntNull() {
        val sut = UnpackableBase64StringBag("BAG24A==")
        assertFailsWith<BagUnpackException> { sut.getInt() }
    }

    @Test
    fun shouldUnpackNonNullIntDigit() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2APIBHA==") },
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
        arrange = { UnpackableBase64StringBag("BAG2pAUwKIFH8A==") },
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
        arrange = { UnpackableBase64StringBag("BAG2rAAFcAAqgAFX//A=") },
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
        arrange = { UnpackableBase64StringBag("BAG29AAAAAewAAAAPIAAAAHn////8A==") },
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
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getStringOrNull() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getStringOrNull() }
    }

    @Test
    fun shouldUnpackNullableStringNull() = performTest(
        arrange = { UnpackableBase64StringBag("BAG24A==") },
        act = { it.getStringOrNull() },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableStringEmpty() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wAA=") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("", it) },
    )

    @Test
    fun shouldUnpackNullableStringDigitLength() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wioyuboA") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("Test", it) },
    )

    @Test
    fun shouldUnpackNullableStringByteLength() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2yIKjK5uioyuboqMrm6KjK5ug") },
        act = { it.getStringOrNull() },
        assert = { assertEquals("TestTestTestTest", it) },
    )

    @Test
    fun shouldUnpackNullableStringSequence() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wioyubpkQVGVzdFRlc3RUZXN0VGVzdA=") },
        act = { listOf(it.getStringOrNull(), it.getStringOrNull()) },
        assert = { assertEquals(listOf("Test", "TestTestTestTest"), it) },
    )

    // String.NotNull

    @Test
    fun shouldNotUnpackNonNullNonString() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getString() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getString() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getString() }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2RihVhVN0dWZm").getString() }
    }

    @Test
    fun shouldNotUnpackNonNullStringNull() {
        val sut = UnpackableBase64StringBag("BAG24A==")
        assertFailsWith<BagUnpackException> { sut.getString() }
    }

    @Test
    fun shouldUnpackNotNullStringEmpty() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wAA=") },
        act = { it.getString() },
        assert = { assertEquals("", it) },
    )

    @Test
    fun shouldUnpackNotNullStringDigitLength() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wioyuboA") },
        act = { it.getString() },
        assert = { assertEquals("Test", it) },
    )

//    @Test
//    fun t() {
//        PackableBase64StringBag().also { bag ->
//            bag.put("TestTestTestTest")
//            throw RuntimeException("[ $bag ]")
//        }
//    }

    @Test
    fun shouldUnpackNotNullStringByteLength() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2yIKjK5uioyuboqMrm6KjK5ug") },
        act = { it.getString() },
        assert = { assertEquals("TestTestTestTest", it) },
    )

    @Test
    fun shouldUnpackNotNullStringSequence() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wioyubpkQVGVzdFRlc3RUZXN0VGVzdA=") },
        act = { listOf(it.getString(), it.getString()) },
        assert = { assertEquals(listOf("Test", "TestTestTestTest"), it) },
    )

    // Stuff.Nullable

    @Test
    fun shouldNotUnpackNullableNonStuff() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getStuffOrNull(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getStuffOrNull(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNullableStuffNull() = performTest(
        arrange = { UnpackableBase64StringBag("BAG24A==") },
        act = { it.getStuffOrNull(TestBagStuff) },
        assert = { assertNull(it) },
    )

    @Test
    fun shouldUnpackNullableStuff() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2RihVhVN0dWZm") },
        act = { it.getStuffOrNull(TestBagStuff) },
        assert = { assertEquals(TestBagStuffMother.TestStuff, it) },
    )

    // Stuff.NonNull

    @Test
    fun shouldNotUnpackNonNullNonStuff() {
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2gA==").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2kA==").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2BA==").getStuff(TestBagStuff) }
        assertFailsWith<BagUnpackException> { UnpackableBase64StringBag("BAG2wKmA").getStuff(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNonNullStuffNull() {
        val sut = UnpackableBase64StringBag("BAG24A==")
        assertFailsWith<BagUnpackException> { sut.getStuff(TestBagStuff) }
    }

    @Test
    fun shouldUnpackNonNullStuff() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2RihVhVN0dWZm") },
        act = { it.getStuff(TestBagStuff) },
        assert = { assertEquals(TestBagStuffMother.TestStuff, it) },
    )

    // Multi

    @Test
    fun shouldUnpackNullableMulti() = performTest(
        arrange = { UnpackableBase64StringBag("BAG2wioyubpPIxQqwqm6OrMzUKg=") },
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
        arrange = { UnpackableBase64StringBag("BAG2wioyubpKMUKsKpujqzM1CoA=") },
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
