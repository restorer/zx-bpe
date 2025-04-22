package com.eightsines.bpe.foundation

import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
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
            ) to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(CanvasLayer, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(2),
                    TestWare.StringWare("TEST"),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.BooleanWare(false),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(2),
                    TestWare.StringWare("TEST"),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.BooleanWare(false),
                ),
            )
        },
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
