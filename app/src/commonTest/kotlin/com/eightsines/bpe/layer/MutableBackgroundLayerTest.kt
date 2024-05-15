package com.eightsines.bpe.layer

import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.test.performTest
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
                color = SciiColor.Red,
                bright = SciiLight.Off,
            ) to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(BackgroundLayer, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1Bbi2i0", it) },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = { UnpackableStringBag("BAG1u1Bbi2i0") },
        act = {
            val sut = it.getStuff(MutableBackgroundLayer)

            sut to listOf(
                sut.isVisible,
                sut.isLocked,
                sut.color,
                sut.bright,
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableBackgroundLayer>(sut)
            assertEquals(listOf(true, false, SciiColor.Red, SciiLight.Off), props)
        },
    )
}
