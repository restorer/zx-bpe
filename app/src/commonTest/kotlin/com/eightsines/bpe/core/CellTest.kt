package com.eightsines.bpe.core

import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
import com.eightsines.bpe.util.BagUnpackException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CellTest {
    @Test
    fun shouldPackScii() = performTest(
        arrange = { SciiCellMother.BlockVerticalLeft to PackableTestBag() },
        act = { (sut, bag) ->
            bag.put(Cell, sut)
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
        arrange = { BlockCellMother.WhiteBright to PackableTestBag() },
        act = { (sut, bag) ->
            bag.put(Cell, sut)
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
        act = { it.getStuff(Cell) },
        assert = { assertEquals(SciiCellMother.BlockVerticalLeft, it) },
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
        act = { it.getStuff(Cell) },
        assert = { assertEquals(BlockCellMother.WhiteBright, it) },
    )

    @Test
    fun shouldNotUnpackUnknown() {
        val bag = UnpackableTestBag(
            listOf(
                TestWare.StuffWare(1),
                TestWare.StringWare("test"),
            ),
        )

        assertFailsWith<BagUnpackException> { bag.getStuff(Cell) }
    }
}
