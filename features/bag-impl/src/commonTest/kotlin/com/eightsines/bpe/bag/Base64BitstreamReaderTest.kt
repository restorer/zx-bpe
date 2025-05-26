package com.eightsines.bpe.bag

import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.Base64BitstreamReader
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Base64BitstreamReaderTest {
    @Test
    fun shouldRead0Bits() = performTest(
        arrange = { Base64BitstreamReader("[]", 2) },
        act = { it.read(0) },
        assert = { assertEquals(0, it) },
    )

    @Test
    fun shouldRead1BitZero() = performTest(
        arrange = { Base64BitstreamReader("[]AA==", 2) },
        act = { it.read(1) },
        assert = { assertEquals(0, it) },
    )

    @Test
    fun shouldRead1BitOne() = performTest(
        arrange = { Base64BitstreamReader("[]gA==", 2) },
        act = { it.read(1) },
        assert = { assertEquals(1, it) },
    )

    @Test
    fun shouldRead4Bits() = performTest(
        arrange = { Base64BitstreamReader("[]UA==", 2) },
        act = { it.read(4) },
        assert = { assertEquals(5, it) },
    )

    @Test
    fun shouldRead6Bits() = performTest(
        arrange = { Base64BitstreamReader("[]qA==", 2) },
        act = { it.read(6) },
        assert = { assertEquals(42, it) },
    )

    @Test
    fun shouldRead10Bits() = performTest(
        arrange = { Base64BitstreamReader("[]7k==", 2) },
        act = { it.read(10) },
        assert = { assertEquals(953, it) },
    )

    @Test
    fun shouldRead14Bits() = performTest(
        arrange = { Base64BitstreamReader("[]TWQ=", 2) },
        act = { it.read(14) },
        assert = { assertEquals(4953, it) },
    )

    @Test
    fun shouldRead24Bits() = performTest(
        arrange = { Base64BitstreamReader("[]TWFu", 2) },
        act = { it.read(24) },
        assert = { assertEquals(5071214, it) },
    )

    @Test
    fun shouldReadSequential() = performTest(
        arrange = { Base64BitstreamReader("[]TWFugA==", 2) },
        act = { listOf(it.read(4), it.read(3), it.read(5), it.read(7), it.read(6)) },
        assert = { assertEquals(listOf(4, 6, 22, 11, 29), it) },
    )

    @Test
    fun shouldPeek1BitZero() = performTest(
        arrange = { Base64BitstreamReader("[]AA==", 2) },
        act = { it.peek(1) to it.read(1) },
        assert = { assertEquals(0 to 0, it) },
    )

    @Test
    fun shouldPeek1BitOne() = performTest(
        arrange = { Base64BitstreamReader("[]gA==", 2) },
        act = { it.peek(1) to it.read(1) },
        assert = { assertEquals(1 to 1, it) },
    )

    @Test
    fun shouldPeek4Bits() = performTest(
        arrange = { Base64BitstreamReader("[]UA==", 2) },
        act = { it.peek(4) to it.read(4) },
        assert = { assertEquals(5 to 5, it) },
    )

    @Test
    fun shouldPeek6Bits() = performTest(
        arrange = { Base64BitstreamReader("[]qA==", 2) },
        act = { it.peek(6) to it.read(6) },
        assert = { assertEquals(42 to 42, it) },
    )

    @Test
    fun shouldPeek10Bits() = performTest(
        arrange = { Base64BitstreamReader("[]7k==", 2) },
        act = { it.peek(10) to it.read(10) },
        assert = { assertEquals(953 to 953, it) },
    )

    @Test
    fun shouldPeek14Bits() = performTest(
        arrange = { Base64BitstreamReader("[]TWQ=", 2) },
        act = { it.peek(14) to it.read(14) },
        assert = { assertEquals(4953 to 4953, it) },
    )

    @Test
    fun shouldConsume2Bits() = performTest(
        arrange = { Base64BitstreamReader("[]UA==", 2) },
        act = {
            val value = it.peek(2)
            it.consume(2)
            value to it.read(2)
        },
        assert = { assertEquals(1 to 1, it) },
    )

    @Test
    fun shouldConsume3Bits() = performTest(
        arrange = { Base64BitstreamReader("[]qA==", 2) },
        act = {
            val value = it.peek(3)
            it.consume(3)
            value to it.read(3)
        },
        assert = { assertEquals(5 to 2, it) },
    )

    @Test
    fun shouldConsume5Bits() = performTest(
        arrange = { Base64BitstreamReader("[]7k==", 2) },
        act = {
            val value = it.peek(5)
            it.consume(5)
            value to it.read(5)
        },
        assert = { assertEquals(29 to 25, it) },
    )

    @Test
    fun shouldConsume7Bits() = performTest(
        arrange = { Base64BitstreamReader("[]TWQ=", 2) },
        act = {
            val value = it.peek(7)
            it.consume(7)
            value to it.read(7)
        },
        assert = { assertEquals(38 to 89, it) },
    )

    @Test
    fun shouldNotReadEmpty() {
        assertFailsWith<BagUnpackException> { Base64BitstreamReader("[]", 2).read(1) }
    }

    @Test
    fun shouldNotReadAfterEnd() {
        val reader = Base64BitstreamReader("[]TWFu", 2)
        reader.read(24)
        assertFailsWith<BagUnpackException> { reader.read(1) }
    }

    @Test
    fun shouldNotReadPadding() {
        val reader = Base64BitstreamReader("[]7k==", 2)
        reader.read(12)
        assertFailsWith<BagUnpackException> { reader.read(1) }
    }
}
