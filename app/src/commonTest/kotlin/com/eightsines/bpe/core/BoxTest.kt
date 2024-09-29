package com.eightsines.bpe.core

import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BoxTest {
    @Test
    fun shouldComputeEndPoint() = performTest(
        arrange = { Box(10, 20, 30, 40) },
        act = { it.ex to it.ey },
        assert = { assertEquals(39 to 59, it) },
    )

    @Test
    fun shouldCopyWithOffsetSeparate() = performTest(
        arrange = { Box(10, 20, 30, 40) },
        act = { it.copyWithOffset(3, 5) },
        assert = { assertEquals(Box(13, 25, 30, 40), it) },
    )

    @Test
    fun shouldCopyWithOffsetPoint() = performTest(
        arrange = { Box(10, 20, 30, 40) },
        act = { it.copyWithOffset(Pair(3, 5)) },
        assert = { assertEquals(Box(13, 25, 30, 40), it) },
    )

    @Test
    fun shouldCreate() = performTest(
        arrange = { Box.of(10, 20, 40, 60) },
        act = { it },
        assert = { assertEquals(Box(10, 20, 31, 41), it) },
    )

    @Test
    fun shouldCreateInverted() = performTest(
        arrange = { Box.of(40, 60, 10, 20) },
        act = { it },
        assert = { assertEquals(Box(10, 20, 31, 41), it) },
    )

    @Test
    fun shouldCreateSame() = performTest(
        arrange = { Box.of(10, 20, 10, 20) },
        act = { it },
        assert = { assertEquals(Box(10, 20, 1, 1), it) },
    )
}
