package com.eightsines.bpe.model

import kotlin.test.Test
import kotlin.test.assertEquals

class HBlockMergeCellTest {
    @Test
    fun shouldConvertTransparentToScii() {
        val sut = HBlockMergeCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)

        val actual = sut.toSciiCell()
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToScii() {
        val sut = HBlockMergeCell(SciiColor.White, SciiColor.Black, SciiLight.On)

        val actual = sut.toSciiCell()

        val expected = SciiCell(
            character = SciiChar.BlockHorizontalTop,
            ink = SciiColor.White,
            paper = SciiColor.Black,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun shouldMerge() {
        val sut = HBlockMergeCell(SciiColor.White, SciiColor.Transparent, SciiLight.On)

        val actual = sut.merge(HBlockMergeCell(SciiColor.Black, SciiColor.Red, SciiLight.Off))
        assertEquals(HBlockMergeCell(SciiColor.White, SciiColor.Red, SciiLight.On), actual)
    }

    @Test
    fun shouldConvertTransparentToSciiStatic() {
        val actual = HBlockMergeCell.makeSciiCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToSciiStatic() {
        val actual = HBlockMergeCell.makeSciiCell(SciiColor.White, SciiColor.Black, SciiLight.On)

        val expected = SciiCell(
            character = SciiChar.BlockHorizontalTop,
            ink = SciiColor.White,
            paper = SciiColor.Black,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        assertEquals(expected, actual)
    }
}
