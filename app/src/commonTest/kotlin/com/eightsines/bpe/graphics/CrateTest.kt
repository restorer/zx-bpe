package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.CellType
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.test.BlockCellMother
import com.eightsines.bpe.test.SciiCellMother
import com.eightsines.bpe.test.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals

class CrateTest {
    @Test
    fun shouldPackScii() = performTest(
        arrange = {
            val sut = Crate(
                cellType = CellType.Scii,
                width = 2,
                height = 1,
                cells = listOf(listOf(SciiCellMother.BlockVerticalLeft, SciiCell.Transparent)),
            )

            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Crate, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i1i2i1u1i1u1n008Ai0i7i1iFu1i1u1iFiFiFiFiF", it) },
    )

    @Test
    fun shouldUnpackScii() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i1i2i1u1i1u1n008Ai0i7i1iFu1i1u1iFiFiFiFiF") },
        act = { it.getStuff(Crate) },
        assert = {
            assertEquals(
                Crate(
                    cellType = CellType.Scii,
                    width = 2,
                    height = 1,
                    cells = listOf(listOf(SciiCellMother.BlockVerticalLeft, SciiCell.Transparent)),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldPackBlock() = performTest(
        arrange = {
            val sut = Crate(
                cellType = CellType.Scii,
                width = 2,
                height = 1,
                cells = listOf(listOf(BlockCellMother.White, BlockCell.Transparent)),
            )

            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Crate, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i1i2i1u1i2u1i7i1u1i2u1iFiF", it) },
    )

    @Test
    fun shouldUnpackBlock() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i1i2i1u1i2u1i7i1u1i2u1iFiF") },
        act = { it.getStuff(Crate) },
        assert = {
            assertEquals(
                Crate(
                    cellType = CellType.Scii,
                    width = 2,
                    height = 1,
                    cells = listOf(listOf(BlockCellMother.White, BlockCell.Transparent)),
                ),
                it,
            )
        }
    )
}
