package com.eightsines.bpe.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SciiLightTest {
    @Test
    fun shouldNotChangeTransparentOnTransparent() {
        val sut = SciiLight.Transparent

        val actual = sut.merge(SciiLight.Transparent)
        assertEquals(SciiLight.Transparent, actual)
    }

    @Test
    fun shouldNotMergeTransparentOnOff() {
        val sut = SciiLight.Transparent

        val actual = sut.merge(SciiLight.Off)
        assertEquals(SciiLight.Off, actual)
    }

    @Test
    fun shouldNotMergeTransparentOnOn() {
        val sut = SciiLight.Transparent

        val actual = sut.merge(SciiLight.On)
        assertEquals(SciiLight.On, actual)
    }

    @Test
    fun shouldNotChangeOffOnOff() {
        val sut = SciiLight.Off

        val actual = sut.merge(SciiLight.Off)
        assertEquals(SciiLight.Off, actual)
    }

    @Test
    fun shouldMergeOffOnOn() {
        val sut = SciiLight.Off

        val actual = sut.merge(SciiLight.On)
        assertEquals(SciiLight.Off, actual)
    }

    @Test
    fun shouldMergeOffOnTransparent() {
        val sut = SciiLight.Off

        val actual = sut.merge(SciiLight.Transparent)
        assertEquals(SciiLight.Off, actual)
    }

    @Test
    fun shouldNotChangeOnOnOn() {
        val sut = SciiLight.On

        val actual = sut.merge(SciiLight.On)
        assertEquals(SciiLight.On, actual)
    }

    @Test
    fun shouldMergeOnOnOff() {
        val sut = SciiLight.On

        val actual = sut.merge(SciiLight.Off)
        assertEquals(SciiLight.On, actual)
    }

    @Test
    fun shouldMergeOnOnTransparent() {
        val sut = SciiLight.On

        val actual = sut.merge(SciiLight.Transparent)
        assertEquals(SciiLight.On, actual)
    }
}
