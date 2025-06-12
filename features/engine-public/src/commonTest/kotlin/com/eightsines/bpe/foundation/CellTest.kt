package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CellTest {
    @Test
    fun shouldPackScii() = performTest(
        arrange = { SciiBlockVerticalLeft to PackableTestBag() },
        act = { (sut, bag) ->
            bag.put(Cell_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(138),
                    TestWare.IntWare(0),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.IntWare(-1),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackBlockDrawing() = performTest(
        arrange = { BlockWhiteBright to PackableTestBag() },
        act = { (sut, bag) ->
            bag.put(Cell_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldUnpackScii() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(138),
                    TestWare.IntWare(0),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.IntWare(-1),
                ),
            )
        },
        act = { it.getStuff(Cell_Stuff) },
        assert = { assertEquals(SciiBlockVerticalLeft, it) },
    )

    @Test
    fun shouldUnpackBlockDrawing() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = { it.getStuff(Cell_Stuff) },
        assert = { assertEquals(BlockWhiteBright, it) },
    )

    @Test
    fun shouldNotUnpackUnknown() {
        val bag = UnpackableTestBag(
            listOf(
                TestWare.StuffWare(1),
                TestWare.StringWare("test"),
            ),
        )

        assertFailsWith<BagUnpackException> { bag.getStuff(Cell_Stuff) }
    }

    companion object {
        val SciiBlockVerticalLeft = SciiCell(
            character = SciiChar.BlockVerticalLeft,
            ink = SciiColor.Black,
            paper = SciiColor.White,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        val BlockWhiteBright = BlockCell(color = SciiColor.White, bright = SciiLight.On)
    }
}