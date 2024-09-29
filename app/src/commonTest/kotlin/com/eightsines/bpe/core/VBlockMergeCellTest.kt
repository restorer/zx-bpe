package com.eightsines.bpe.core

import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VBlockMergeCellTest {
    @Test
    fun shouldConvertTransparentToScii() = performTest(
        arrange = { VBlockMergeCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On) },
        act = { it.toSciiCell() },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldConvertNonTransparentToScii() = performTest(
        arrange = { VBlockMergeCell(SciiColor.Black, SciiColor.White, SciiLight.On) },
        act = { it.toSciiCell() },
        assert = { assertEquals(SciiCellMother.BlockVerticalLeft, it) },
    )

    @Test
    fun shouldMerge() = performTest(
        arrange = { VBlockMergeCell(SciiColor.White, SciiColor.Transparent, SciiLight.On) },
        act = { it.merge(VBlockMergeCell(SciiColor.Black, SciiColor.Red, SciiLight.Off)) },
        assert = { assertEquals(VBlockMergeCell(SciiColor.White, SciiColor.Red, SciiLight.On), it) },
    )

    @Test
    fun shouldConvertTransparentToSciiStatic() {
        val actual = VBlockMergeCell.makeSciiCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToSciiStatic() {
        val actual = VBlockMergeCell.makeSciiCell(SciiColor.Black, SciiColor.White, SciiLight.On)
        assertEquals(SciiCellMother.BlockVerticalLeft, actual)
    }
}
