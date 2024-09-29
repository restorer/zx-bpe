package com.eightsines.bpe.core

import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HBlockMergeCellTest {
    @Test
    fun shouldConvertTransparentToScii() = performTest(
        arrange = { HBlockMergeCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On) },
        act = { it.toSciiCell() },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldConvertNonTransparentToScii() = performTest(
        arrange = { HBlockMergeCell(SciiColor.White, SciiColor.Black, SciiLight.On) },
        act = { it.toSciiCell() },
        assert = { assertEquals(SciiCellMother.BlockHorizontalTop, it) },
    )

    @Test
    fun shouldMerge() = performTest(
        arrange = { HBlockMergeCell(SciiColor.White, SciiColor.Transparent, SciiLight.On) },
        act = { it.merge(HBlockMergeCell(SciiColor.Black, SciiColor.Red, SciiLight.Off)) },
        assert = { assertEquals(HBlockMergeCell(SciiColor.White, SciiColor.Red, SciiLight.On), it) },
    )

    @Test
    fun shouldConvertTransparentToSciiStatic() {
        val actual = HBlockMergeCell.makeSciiCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToSciiStatic() {
        val actual = HBlockMergeCell.makeSciiCell(SciiColor.White, SciiColor.Black, SciiLight.On)
        assertEquals(SciiCellMother.BlockHorizontalTop, actual)
    }
}
