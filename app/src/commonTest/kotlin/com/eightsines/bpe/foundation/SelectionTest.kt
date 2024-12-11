package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectionTest {
    @Test
    fun shouldComputeSciiBox() = performTest(
        arrange = { Selection(CanvasType.QBlock, Box.ofSize(4, 7, 9, 11)) },
        act = { it.sciiBox },
        assert = { assertEquals(Box.ofSize(2, 3, 5, 6), it) },
    )

    @Test
    fun shouldCopyWithOffsetSeparate() = performTest(
        arrange = { Selection(CanvasType.QBlock, Box.ofSize(4, 7, 9, 11)) },
        act = { it.copyWithOffset(1, 2) },
        assert = { assertEquals(Selection(CanvasType.QBlock, Box.ofSize(5, 9, 9, 11)), it) },
    )

    @Test
    fun shouldCopyWithOffsetPointer() = performTest(
        arrange = { Selection(CanvasType.QBlock, Box.ofSize(4, 7, 9, 11)) },
        act = { it.copyWithOffset(Pair(1, 2)) },
        assert = { assertEquals(Selection(CanvasType.QBlock, Box.ofSize(5, 9, 9, 11)), it) },
    )
}
