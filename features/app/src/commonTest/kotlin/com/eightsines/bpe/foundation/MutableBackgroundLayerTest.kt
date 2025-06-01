package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
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
            ) to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(BackgroundLayer_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.IntWare(0),
                    TestWare.IntWare(2),
                    TestWare.IntWare(0),
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
                    TestWare.StuffWare(1),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.IntWare(0),
                    TestWare.IntWare(2),
                    TestWare.IntWare(0),
                ),
            )
        },
        act = {
            val sut = it.getStuff(MutableBackgroundLayer_Stuff)

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
