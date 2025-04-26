package com.eightsines.bpe.util

import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BitstreamBase64WriterTest {
    @Test
    fun shouldWriteNoBits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = { it.toString() },
        assert = { assertEquals("[]", it) },
    )

    @Test
    fun shouldWrite0Bits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(1, 0)
            it.toString()
        },
        assert = { assertEquals("[]", it) },
    )

    @Test
    fun shouldWrite1BitZero() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(0, 1)
            it.toString()
        },
        assert = { assertEquals("[]AA==", it) },
    )

    @Test
    fun shouldWrite1BitOne() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(1, 1)
            it.toString()
        },
        assert = { assertEquals("[]gA==", it) },
    )

    @Test
    fun shouldWrite1BitMasked() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(3, 1)
            it.toString()
        },
        assert = { assertEquals("[]gA==", it) },
    )

    @Test
    fun shouldWrite4BitsMasked() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(53, 4)
            it.toString()
        },
        assert = { assertEquals("[]UA==", it) },
    )

    @Test
    fun shouldWrite6Bits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(42, 6)
            it.toString()
        },
        assert = { assertEquals("[]qA==", it) },
    )

    @Test
    fun shouldWrite10Bits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(953, 10)
            it.toString()
        },
        assert = { assertEquals("[]7kA=", it) },
    )

    @Test
    fun shouldWrite14Bits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(4953, 14)
            it.toString()
        },
        assert = { assertEquals("[]TWQ=", it) },
    )

    @Test
    fun shouldWrite24Bits() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(5071214, 24)
            it.toString()
        },
        assert = { assertEquals("[]TWFu", it) },
    )

    @Test
    fun shouldWriteSequential() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(4, 4)
            it.write(6, 3)
            it.write(22, 5)
            it.write(11, 7)
            it.write(29, 6)
            it.toString()
        },
        assert = { assertEquals("[]TWFugA==", it) },
    )

    @Test
    fun shouldNotModifyStateOnInspection() = performTest(
        arrange = { BitstreamBase64Writer("[]") },
        act = {
            it.write(1, 1)
            it.toString() + it.toString()
        },
        assert = { assertEquals("[]gA==[]gA==", it) },
    )
}
