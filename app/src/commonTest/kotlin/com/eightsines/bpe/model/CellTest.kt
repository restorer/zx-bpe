package com.eightsines.bpe.model

import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CellTest {
    @Test
    fun shouldPackScii() {
        val cell = SciiCell(
            character = SciiChar.BlockVerticalLeft,
            ink = SciiColor.Black,
            paper = SciiColor.White,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        val bag = PackableStringBag()
        bag.put(Cell, cell)

        assertEquals("BAG1u1s4sciiu1n008Ai0i7i1iF", bag.toString())
    }

    @Test
    fun shouldPackBlockDrawing() {
        val cell = BlockDrawingCell(color = SciiColor.White, bright = SciiLight.Off)

        val bag = PackableStringBag()
        bag.put(Cell, cell)

        assertEquals("BAG1u1s5blocku1i7i0", bag.toString())
    }

    @Test
    fun shouldUnpackScii() {
        val bag = UnpackableStringBag("BAG1u1s4sciiu1n008Ai0i7i1iF")

        val actual = bag.getStuff(Cell)

        val expected = SciiCell(
            character = SciiChar.BlockVerticalLeft,
            ink = SciiColor.Black,
            paper = SciiColor.White,
            bright = SciiLight.On,
            flash = SciiLight.Transparent,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun shouldUnpackBlockDrawing() {
        val bag = UnpackableStringBag("BAG1u1s5blocku1i7i0")

        val actual = bag.getStuff(Cell)
        val expected = BlockDrawingCell(color = SciiColor.White, bright = SciiLight.Off)

        assertEquals(expected, actual)
    }

    @Test
    fun shouldNotUnpackUnknown() {
        val bag = UnpackableStringBag("BAG1u1s4test")

        assertFailsWith<BagUnpackException> { bag.getStuff(Cell) }
    }
}
