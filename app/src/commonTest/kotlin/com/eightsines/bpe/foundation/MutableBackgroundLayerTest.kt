package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.testing.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MutableBackgroundLayerTest {
    @Test
    fun shouldPack() = performTest(
        arrange = {
            MutableBackgroundLayer(
                isVisible = true,
                isLocked = false,
                border = SciiColor.Black,
                color = SciiColor.Red,
                bright = SciiLight.Off,
            ) to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(BackgroundLayer, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1Bbi0i2i0", it) },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = { UnpackableStringBag("BAG1u1Bbi0i2i0") },
        act = {
            val sut = it.getStuff(MutableBackgroundLayer)

            sut to listOf(
                sut.isVisible,
                sut.isLocked,
                sut.border,
                sut.color,
                sut.bright,
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableBackgroundLayer>(sut)
            assertEquals(listOf(true, false, SciiColor.Black, SciiColor.Red, SciiLight.Off), props)
        },
    )
}
