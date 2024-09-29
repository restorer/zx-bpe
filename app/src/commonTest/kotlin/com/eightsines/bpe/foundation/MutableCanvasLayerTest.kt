package com.eightsines.bpe.foundation

import com.eightsines.bpe.testing.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MutableCanvasLayerTest {
    @Test
    fun shouldPack() = performTest(
        arrange = {
            MutableCanvasLayer(
                LayerUid("TEST"),
                isVisible = true,
                isLocked = false,
                canvas = MutableSciiCanvas(1, 1),
            ) to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(CanvasLayer, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1s4TESTBbu1i1i1i1u1u1i1u1iFiFiFiFiF", it) },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = { UnpackableStringBag("BAG1u1s4TESTBbu1i1i1i1u1u1i1u1iFiFiFiFiF") },
        act = {
            val sut = it.getStuff(MutableCanvasLayer)

            sut to listOf(
                sut.isVisible,
                sut.isLocked,
                sut.canvas.type,
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableCanvasLayer<*>>(sut)
            assertEquals(listOf(true, false, CanvasType.Scii), props)
        },
    )
}
