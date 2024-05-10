package com.eightsines.bpe.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SciiColorTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() {
        val sut = SciiColor.Transparent

        val actual = sut.merge(SciiColor.Transparent)
        assertEquals(SciiColor.Transparent, actual)
    }

    @Test
    fun shouldNotMergeTransparentOnNonTransparent() {
        val sut = SciiColor.Transparent

        val actual = sut.merge(SciiColor.White)
        assertEquals(SciiColor.White, actual)
    }

    @Test
    fun shouldMergeNonTransparentOnNonTransparent() {
        val sut = SciiColor.White

        val actual = sut.merge(SciiColor.Black)
        assertEquals(SciiColor.White, actual)
    }

    @Test
    fun shouldMergeNonTransparentOnTransparent() {
        val sut = SciiColor.White

        val actual = sut.merge(SciiColor.Transparent)
        assertEquals(SciiColor.White, actual)
    }
}
