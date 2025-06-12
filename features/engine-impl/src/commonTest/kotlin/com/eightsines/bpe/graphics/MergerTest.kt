package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.Merger
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MergerTest {
    @Test
    fun shouldNotChangeTransparentCharOnTransparent() = performTest(
        arrange = { SciiChar.Transparent },
        act = { Merger.mergeChar(it, SciiChar.Transparent) },
        assert = { assertEquals(SciiChar.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentCharOnNonTransparent() = performTest(
        arrange = { SciiChar.Transparent },
        act = { Merger.mergeChar(it, SciiChar.BlockFull) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )

    @Test
    fun shouldMergeNonTransparentCharOnNonTransparent() = performTest(
        arrange = { SciiChar.BlockFull },
        act = { Merger.mergeChar(it, SciiChar.Space) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )

    @Test
    fun shouldMergeNonTransparentCharOnTransparent() = performTest(
        arrange = { SciiChar.BlockFull },
        act = { Merger.mergeChar(it, SciiChar.Transparent) },
        assert = { assertEquals(SciiChar.BlockFull, it) },
    )

    @Test
    fun shouldNotChangeTransparentColorOnTransparent() = performTest(
        arrange = { SciiColor.Transparent },
        act = { Merger.mergeColor(it, SciiColor.Transparent) },
        assert = { assertEquals(SciiColor.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentColorOnNonTransparent() = performTest(
        arrange = { SciiColor.Transparent },
        act = { Merger.mergeColor(it, SciiColor.White) },
        assert = { assertEquals(SciiColor.White, it) },
    )

    @Test
    fun shouldMergeNonTransparentColorOnNonTransparent() = performTest(
        arrange = { SciiColor.White },
        act = { Merger.mergeColor(it, SciiColor.Black) },
        assert = { assertEquals(SciiColor.White, it) },
    )

    @Test
    fun shouldMergeNonTransparentColorOnTransparent() = performTest(
        arrange = { SciiColor.White },
        act = { Merger.mergeColor(it, SciiColor.Transparent) },
        assert = { assertEquals(SciiColor.White, it) },
    )

    @Test
    fun shouldNotChangeTransparentLightOnTransparent() = performTest(
        arrange = { SciiLight.Transparent },
        act = { Merger.mergeLight(it, SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentLightOnOff() = performTest(
        arrange = { SciiLight.Transparent },
        act = { Merger.mergeLight(it, SciiLight.Off) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldNotMergeTransparentLightOnOn() = performTest(
        arrange = { SciiLight.Transparent },
        act = { Merger.mergeLight(it, SciiLight.On) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldNotChangeOffLightOnOff() = performTest(
        arrange = { SciiLight.Off },
        act = { Merger.mergeLight(it, SciiLight.Off) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldMergeOffLightOnOn() = performTest(
        arrange = { SciiLight.Off },
        act = { Merger.mergeLight(it, SciiLight.On) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldMergeOffLightOnTransparent() = performTest(
        arrange = { SciiLight.Off },
        act = { Merger.mergeLight(it, SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldNotChangeOnLightOnOn() = performTest(
        arrange = { SciiLight.On },
        act = { Merger.mergeLight(it, SciiLight.On) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldNotChangeOnLightOnOff() = performTest(
        arrange = { SciiLight.On },
        act = { Merger.mergeLight(it, SciiLight.Off) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldMergeOnLightOnTransparent() = performTest(
        arrange = { SciiLight.On },
        act = { Merger.mergeLight(it, SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.On, it) },
    )
}
