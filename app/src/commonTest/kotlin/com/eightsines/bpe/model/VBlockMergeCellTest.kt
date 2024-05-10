package com.eightsines.bpe.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VBlockMergeCellTest {
    @Test
    fun shouldConvertTransparentToScii() {
        val sut = VBlockMergeCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)

        val actual = sut.toSciiCell()
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToScii() {
        val sut = VBlockMergeCell(SciiColor.White, SciiColor.Black, SciiLight.On)

        val actual = sut.toSciiCell()

        val expected = SciiCell(
            character = SciiChar.BlockVerticalLeft,
            ink = SciiColor.White,
            paper = SciiColor.Black,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun shouldMerge() {
        val sut = VBlockMergeCell(SciiColor.White, SciiColor.Transparent, SciiLight.On)

        val actual = sut.merge(VBlockMergeCell(SciiColor.Black, SciiColor.Red, SciiLight.Off))
        assertEquals(VBlockMergeCell(SciiColor.White, SciiColor.Red, SciiLight.On), actual)
    }

    @Test
    fun shouldConvertTransparentToSciiStatic() {
        val actual = VBlockMergeCell.makeSciiCell(SciiColor.Transparent, SciiColor.Transparent, SciiLight.On)
        assertEquals(SciiCell.Transparent, actual)
    }

    @Test
    fun shouldConvertNonTransparentToSciiStatic() {
        val actual = VBlockMergeCell.makeSciiCell(SciiColor.White, SciiColor.Black, SciiLight.On)

        val expected = SciiCell(
            character = SciiChar.BlockVerticalLeft,
            ink = SciiColor.White,
            paper = SciiColor.Black,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        assertEquals(expected, actual)
    }
}