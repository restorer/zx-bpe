package com.eightsines.bpe.model

import com.eightsines.bpe.test.BlockCellMother
import com.eightsines.bpe.test.SciiCellMother
import com.eightsines.bpe.test.performTest
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CellTest {
    @Test
    fun shouldPackScii() = performTest(
        arrange = { SciiCellMother.BlockVerticalLeft to PackableStringBag() },
        act = { (sut, bag) ->
            bag.put(Cell, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i1u1n008Ai0i7i1iF", it) },
    )

    @Test
    fun shouldPackBlockDrawing() = performTest(
        arrange = { BlockCellMother.White to PackableStringBag() },
        act = { (sut, bag) ->
            bag.put(Cell, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackScii() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i1u1n008Ai0i7i1iF") },
        act = { it.getStuff(Cell) },
        assert = { assertEquals(SciiCellMother.BlockVerticalLeft, it) },
    )

    @Test
    fun shouldUnpackBlockDrawing() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i2u1i7i1") },
        act = { it.getStuff(Cell) },
        assert = { assertEquals(BlockCellMother.White, it) },
    )

    @Test
    fun shouldNotUnpackUnknown() {
        val bag = UnpackableStringBag("BAG1u1s4test")
        assertFailsWith<BagUnpackException> { bag.getStuff(Cell) }
    }
}
