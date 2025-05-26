package com.eightsines.bpe.core

import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SciiLightTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() = performTest(
        arrange = { SciiLight.Transparent },
        act = { it.merge(SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.Transparent, it) },
    )

    @Test
    fun shouldNotMergeTransparentOnOff() = performTest(
        arrange = { SciiLight.Transparent },
        act = { it.merge(SciiLight.Off) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldNotMergeTransparentOnOn() = performTest(
        arrange = { SciiLight.Transparent },
        act = { it.merge(SciiLight.On) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldNotChangeOffOnOff() = performTest(
        arrange = { SciiLight.Off },
        act = { it.merge(SciiLight.Off) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldMergeOffOnOn() = performTest(
        arrange = { SciiLight.Off },
        act = { it.merge(SciiLight.On) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldMergeOffOnTransparent() = performTest(
        arrange = { SciiLight.Off },
        act = { it.merge(SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.Off, it) },
    )

    @Test
    fun shouldNotChangeOnOnOn() = performTest(
        arrange = { SciiLight.On },
        act = { it.merge(SciiLight.On) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldNotChangeOnOnOff() = performTest(
        arrange = { SciiLight.On },
        act = { it.merge(SciiLight.Off) },
        assert = { assertEquals(SciiLight.On, it) },
    )

    @Test
    fun shouldMergeOnOnTransparent() = performTest(
        arrange = { SciiLight.On },
        act = { it.merge(SciiLight.Transparent) },
        assert = { assertEquals(SciiLight.On, it) },
    )
}
