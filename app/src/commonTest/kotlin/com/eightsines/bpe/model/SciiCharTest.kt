package com.eightsines.bpe.model

import com.eightsines.bpe.test.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SciiCharTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() = performTest(
        arrange = { SciiChar.Transparent },
        act = { it.merge(SciiChar.Transparent) },
        assert = { assertEquals(SciiChar.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentOnNonTransparent() = performTest(
        arrange = { SciiChar.Transparent },
        act = { it.merge(SciiChar.BlockFull) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )

    @Test
    fun shouldMergeNonTransparentOnNonTransparent() = performTest(
        arrange = { SciiChar.BlockFull },
        act = { it.merge(SciiChar.Space) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )

    @Test
    fun shouldMergeNonTransparentOnTransparent() = performTest(
        arrange = { SciiChar.BlockFull },
        act = { it.merge(SciiChar.Transparent) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )
}
