package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestBagStuff
import com.eightsines.bpe.testing.TestBagStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalEncodingApi::class)
class PackableBase64StringBagTest {
    // Empty

    @Test
    fun shouldPackEmpty() = performTest(
        arrange = { PackableBase64StringBag() },
        act = { it.toString() },
        assert = { assertEquals("BAG2", it) },
    )

    // Boolean

    @Test
    fun shouldPackBooleanNull() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(null as Boolean?)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b1110_0000.toByte())),
                it,
            )
        },
    )

    @Test
    fun shouldPackBooleanFalse() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(false)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b100_0_0000.toByte())),
                it,
            )
        },
    )

    @Test
    fun shouldPackBooleanTrue() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(true)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b100_1_0000.toByte())),
                it,
            )
        },
    )

    // Int

    @Test
    fun shouldPackIntNull() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(null as Int?)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b1110_0000.toByte())),
                it,
            )
        },
    )

    @Test
    fun shouldPackIntDigit() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(0) // 0000
            it.put(-1) // 1111
            it.put(-8) // 1000
            it.put(1) // 0001
            it.put(7) // 0111

            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b00_0000_00.toByte(),
                        0b1111_00_10.toByte(),
                        0b00_00_0001.toByte(),
                        0b00_0111_00.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackIntByte() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(-128) // 10000000
            it.put(-64) // 11000000
            it.put(64) // 01000000
            it.put(127) // 01111111

            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b10100_100.toByte(),
                        0b00000_101.toByte(),
                        0b00_110000.toByte(),
                        0b00_10100_0.toByte(),
                        0b1000000_1.toByte(),
                        0b0100_0111.toByte(),
                        0b1111_0000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackIntShort() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(-32768) // 1000000000000000
            it.put(-16384) // 1100000000000000
            it.put(16384) // 0100000000000000
            it.put(32767) // 0111111111111111

            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b10101_100.toByte(),
                        0b00000000.toByte(),
                        0b00000_101.toByte(),
                        0b01_110000.toByte(),
                        0b00000000.toByte(),
                        0b00_10101_0.toByte(),
                        0b10000000.toByte(),
                        0b0000000_1.toByte(),
                        0b0101_0111.toByte(),
                        0b11111111.toByte(),
                        0b1111_0000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackIntInt() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(-2147483648) // 10000000000000000000000000000000
            it.put(-1073741824) // 11000000000000000000000000000000
            it.put(1073741824) // 01000000000000000000000000000000
            it.put(2147483647) // 01111111111111111111111111111111

            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b11110_100.toByte(),
                        0b00000000.toByte(),
                        0b00000000.toByte(),
                        0b00000000.toByte(),
                        0b00000_111.toByte(),
                        0b10_110000.toByte(),
                        0b00000000.toByte(),
                        0b00000000.toByte(),
                        0b00000000.toByte(),
                        0b00_11110_0.toByte(),
                        0b10000000.toByte(),
                        0b00000000.toByte(),
                        0b00000000.toByte(),
                        0b0000000_1.toByte(),
                        0b1110_0111.toByte(),
                        0b11111111.toByte(),
                        0b11111111.toByte(),
                        0b11111111.toByte(),
                        0b1111_0000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    // String

    @Test
    fun shouldPackStringNull() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(null as String?)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b1110_0000.toByte())),
                it,
            )
        },
    )

    @Test
    fun shouldPackStringEmpty() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put("")
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b11000_000.toByte(),
                        0b0_0000000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackStringDigitLength() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put("Test") // 01010100 01100101 01110011 01110100
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b11000_010.toByte(),
                        0b0_0101010.toByte(),
                        0b0_0110010.toByte(),
                        0b1_0111001.toByte(),
                        0b1_0111010.toByte(),
                        0b0_0000000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackStringByteLength() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put("TestTestTestTest") // (01010100 01100101 01110011 01110100) x 4
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b11001_000.toByte(),
                        0b10000_010.toByte(),
                        0b10100_011.toByte(),
                        0b00101_011.toByte(),
                        0b10011_011.toByte(),

                        0b10100_010.toByte(),
                        0b10100_011.toByte(),
                        0b00101_011.toByte(),
                        0b10011_011.toByte(),

                        0b10100_010.toByte(),
                        0b10100_011.toByte(),
                        0b00101_011.toByte(),
                        0b10011_011.toByte(),

                        0b10100_010.toByte(),
                        0b10100_011.toByte(),
                        0b00101_011.toByte(),
                        0b10011_011.toByte(),
                        0b10100_000.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    // Stuff

    @Test
    fun shouldPackStuffNull() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put(TestBagStuff, null)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(byteArrayOf(0b1110_0000.toByte())),
                it,
            )
        },
    )

    @Test
    fun shouldPackStuff() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            // 42 — 00101010
            // Stuff - 01010011 01110100 01110101 01100110 01100110
            it.put(TestBagStuff, TestBagStuffMother.TestStuff)
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b01_0001_10.toByte(),
                        0b00_10100_0.toByte(),
                        0b0101010_1.toByte(),
                        0b1000_0101.toByte(),
                        0b01010011.toByte(),
                        0b01110100.toByte(),
                        0b01110101.toByte(),
                        0b01100110.toByte(),
                        0b01100110.toByte(),
                    ),
                ),
                it,
            )
        },
    )

    // Multi

    @Test
    fun shouldPackMulti() = performTest(
        arrange = { PackableBase64StringBag() },
        act = {
            it.put("Test") // 01010100 01100101 01110011 01110100
            it.put(true)
            it.put(null as Int?)

            // 42 — 00101010
            // Stuff - 01010011 01110100 01110101 01100110 01100110
            it.put(TestBagStuff, TestBagStuffMother.TestStuff)

            it.put(42) // 00101010
            it.toString()
        },
        assert = {
            assertEquals(
                "BAG2" + Base64.Default.encode(
                    byteArrayOf(
                        0b11000_010.toByte(), // S4:4
                        0b0_0101010.toByte(), // T
                        0b0_0110010.toByte(), // e
                        0b1_0111001.toByte(), // s
                        0b1_0111010.toByte(), // t
                        0b0_1001_111.toByte(), // B:1, NULL
                        0b0_01_0001_1.toByte(), // U4:1, B:0
                        0b000_10100.toByte(), // I8:42
                        0b00101010.toByte(),
                        0b11000_010.toByte(), // S4:5
                        0b1_0101001.toByte(), // S
                        0b1_0111010.toByte(), // t
                        0b0_0111010.toByte(), // u
                        0b1_0110011.toByte(), // f
                        0b0_0110011.toByte(), // f
                        0b0_10100_00.toByte(), // I8:42
                        0b101010_00.toByte(),
                    ),
                ),
                it,
            )
        },
    )
}
