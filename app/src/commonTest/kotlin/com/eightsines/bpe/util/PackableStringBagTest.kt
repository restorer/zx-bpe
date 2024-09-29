package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestBagStuff
import com.eightsines.bpe.testing.TestBagStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PackableStringBagTest {
    // Empty

    @Test
    fun shouldPackEmpty() = performTest(
        arrange = { PackableStringBag() },
        act = { it.toString() },
        assert = { assertEquals("BAG1", it) },
    )

    // Boolean

    @Test
    fun shouldPackBooleanNull() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(null as Boolean?)
            it.toString()
        },
        assert = { assertEquals("BAG1_", it) },
    )

    @Test
    fun shouldPackBooleanFalse() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(false)
            it.toString()
        },
        assert = { assertEquals("BAG1b", it) },
    )

    @Test
    fun shouldPackBooleanTrue() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(true)
            it.toString()
        },
        assert = { assertEquals("BAG1B", it) },
    )

    // Int

    @Test
    fun shouldPackIntNull() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(null as Int?)
            it.toString()
        },
        assert = { assertEquals("BAG1_", it) },
    )

    @Test
    fun shouldPackIntDigit() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(0)
            it.put(-1)
            it.put(-8)
            it.put(1)
            it.put(7)

            it.toString()
        },
        assert = { assertEquals("BAG1i0iFi8i1i7", it) },
    )

    @Test
    fun shouldPackIntByte() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(-128)
            it.put(-64)
            it.put(64)
            it.put(127)

            it.toString()
        },
        assert = { assertEquals("BAG1I80IC0I40I7F", it) },
    )

    @Test
    fun shouldPackIntShort() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(-32768)
            it.put(-16384)
            it.put(16384)
            it.put(32767)

            it.toString()
        },
        assert = { assertEquals("BAG1n8000nC000n4000n7FFF", it) },
    )

    @Test
    fun shouldPackIntInt() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(-2147483648)
            it.put(-1073741824)
            it.put(1073741824)
            it.put(2147483647)

            it.toString()
        },
        assert = { assertEquals("BAG1N80000000NC0000000N40000000N7FFFFFFF", it) },
    )

    // String

    @Test
    fun shouldPackStringNull() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(null as String?)
            it.toString()
        },
        assert = { assertEquals("BAG1_", it) },
    )

    @Test
    fun shouldPackStringEmpty() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put("")
            it.toString()
        },
        assert = { assertEquals("BAG1s0", it) },
    )

    @Test
    fun shouldPackStringDigitLength() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put("Test")
            it.toString()
        },
        assert = { assertEquals("BAG1s4Test", it) },
    )

    @Test
    fun shouldPackStringByteLength() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put("TestTestTestTest")
            it.toString()
        },
        assert = { assertEquals("BAG1S10TestTestTestTest", it) },
    )

    // Stuff

    @Test
    fun shouldPackStuffNull() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(TestBagStuff, null)
            it.toString()
        },
        assert = { assertEquals("BAG1_", it) },
    )

    @Test
    fun shouldPackStuff() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put(TestBagStuff, TestBagStuffMother.TestStuff)
            it.toString()
        },
        assert = { assertEquals("BAG1u1bI2As5Stuff", it) },
    )

    // Multi

    @Test
    fun shouldPackMulti() = performTest(
        arrange = { PackableStringBag() },
        act = {
            it.put("Test")
            it.put(true)
            it.put(null as Int?)
            it.put(TestBagStuff, TestBagStuffMother.TestStuff)
            it.put(42)

            it.toString()
        },
        assert = { assertEquals("BAG1s4TestB_u1bI2As5StuffI2A", it) }
    )
}
