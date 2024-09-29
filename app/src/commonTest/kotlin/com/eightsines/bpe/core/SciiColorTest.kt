package com.eightsines.bpe.core

import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SciiColorTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() = performTest(
        arrange = { SciiColor.Transparent },
        act = { it.merge(SciiColor.Transparent) },
        assert = { assertEquals(SciiColor.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentOnNonTransparent() = performTest(
        arrange = { SciiColor.Transparent },
        act = { it.merge(SciiColor.White) },
        assert = { assertEquals(SciiColor.White, it) },
    )

    @Test
    fun shouldMergeNonTransparentOnNonTransparent() = performTest(
        arrange = { SciiColor.White },
        act = { it.merge(SciiColor.Black) },
        assert = { assertEquals(SciiColor.White, it) },
    )

    @Test
    fun shouldMergeNonTransparentOnTransparent() = performTest(
        arrange = { SciiColor.White },
        act = { it.merge(SciiColor.Transparent) },
        assert = { assertEquals(SciiColor.White, it) },
    )
}
