package com.eightsines.bpe.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SciiCharTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() {
        val sut = SciiChar.Transparent

        val actual = sut.merge(SciiChar.Transparent)
        assertEquals(SciiChar.Transparent, actual)
    }

    @Test
    fun shouldNotMergeTransparentOnNonTransparent() {
        val sut = SciiChar.Transparent

        val actual = sut.merge(SciiChar.BlockFull)
        assertEquals(SciiChar.BlockFull, actual)
    }

    @Test
    fun shouldMergeNonTransparentOnNonTransparent() {
        val sut = SciiChar.BlockFull

        val actual = sut.merge(SciiChar.Space)
        assertEquals(SciiChar.BlockFull, actual)
    }

    @Test
    fun shouldMergeNonTransparentOnTransparent() {
        val sut = SciiChar.BlockFull

        val actual = sut.merge(SciiChar.Transparent)
        assertEquals(SciiChar.BlockFull, actual)
    }
}
