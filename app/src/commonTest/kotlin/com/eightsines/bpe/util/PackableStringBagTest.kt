package com.eightsines.bpe.util

import kotlin.test.Test
import kotlin.test.assertEquals

class PackableStringBagTest {
    // Empty

    @Test
    fun shouldPackEmpty() {
        val sut = makeSut()

        val actual = sut.toString()
        assertEquals("BAG1", actual)
    }

    // Boolean

    @Test
    fun shouldPackBooleanNull() {
        val sut = makeSut()

        sut.put(null as Boolean?)

        val actual = sut.toString()
        assertEquals("BAG1_", actual)
    }

    @Test
    fun shouldPackBooleanFalse() {
        val sut = makeSut()

        sut.put(false)

        val actual = sut.toString()
        assertEquals("BAG1b", actual)
    }

    @Test
    fun shouldPackBooleanTrue() {
        val sut = makeSut()

        sut.put(true)

        val actual = sut.toString()
        assertEquals("BAG1B", actual)
    }

    // Int

    @Test
    fun shouldPackIntNull() {
        val sut = makeSut()

        sut.put(null as Int?)

        val actual = sut.toString()
        assertEquals("BAG1_", actual)
    }

    @Test
    fun shouldPackIntDigit() {
        val sut = makeSut()

        sut.put(0)
        sut.put(-1)
        sut.put(-8)
        sut.put(1)
        sut.put(7)

        val actual = sut.toString()
        assertEquals("BAG1i0iFi8i1i7", actual)
    }

    @Test
    fun shouldPackIntByte() {
        val sut = makeSut()

        sut.put(-128)
        sut.put(-64)
        sut.put(64)
        sut.put(127)

        val actual = sut.toString()
        assertEquals("BAG1I80IC0I40I7F", actual)
    }

    @Test
    fun shouldPackIntShort() {
        val sut = makeSut()

        sut.put(-32768)
        sut.put(-16384)
        sut.put(16384)
        sut.put(32767)

        val actual = sut.toString()
        assertEquals("BAG1n8000nC000n4000n7FFF", actual)
    }

    @Test
    fun shouldPackIntInt() {
        val sut = makeSut()

        sut.put(-2147483648)
        sut.put(-1073741824)
        sut.put(1073741824)
        sut.put(2147483647)

        val actual = sut.toString()
        assertEquals("BAG1N80000000NC0000000N40000000N7FFFFFFF", actual)
    }

    // String

    @Test
    fun shouldPackStringNull() {
        val sut = makeSut()

        sut.put(null as String?)

        val actual = sut.toString()
        assertEquals("BAG1_", actual)
    }

    @Test
    fun shouldPackStringEmpty() {
        val sut = makeSut()

        sut.put("")

        val actual = sut.toString()
        assertEquals("BAG1s0", actual)
    }

    @Test
    fun shouldPackStringDigitLength() {
        val sut = makeSut()

        sut.put("Test")

        val actual = sut.toString()
        assertEquals("BAG1s4Test", actual)
    }

    @Test
    fun shouldPackStringByteLength() {
        val sut = makeSut()

        sut.put("TestTestTestTest")

        val actual = sut.toString()
        assertEquals("BAG1S10TestTestTestTest", actual)
    }

    // Stuff

    @Test
    fun shouldPackStuffNull() {
        val sut = makeSut()

        sut.put(BagStuffStub, null)

        val actual = sut.toString()
        assertEquals("BAG1_", actual)
    }

    @Test
    fun shouldPackStuff() {
        val sut = makeSut()

        sut.put(BagStuffStub, makeStuff())

        val actual = sut.toString()
        assertEquals("BAG1u1bI2As5Stuff", actual)
    }

    // Multi

    @Test
    fun shouldPackMulti() {
        val sut = makeSut()

        sut.put("Test")
        sut.put(true)
        sut.put(null as Int?)
        sut.put(BagStuffStub, makeStuff())
        sut.put(42)

        val actual = sut.toString()
        assertEquals("BAG1s4TestB_u1bI2As5StuffI2A", actual)
    }

    // Utils

    private fun makeSut() = PackableStringBag()

    private fun makeStuff() = BagStuffStub(
        booleanValue = false,
        intValue = 42,
        stringValue = "Stuff",
    )
}
