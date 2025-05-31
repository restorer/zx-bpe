package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CrateTest {
    @Test
    fun shouldPackScii() = performTest(
        arrange = {
            val sut = Crate(
                canvasType = CanvasType.Scii,
                width = 2,
                height = 1,
                cells = listOf(listOf(SciiCellMother.BlockVerticalLeft, SciiCell.Transparent)),
            )

            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Crate_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(138),
                    TestWare.IntWare(0),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.IntWare(-1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
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
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(138),
                    TestWare.IntWare(0),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.IntWare(-1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                ),
            )
        },
        act = { it.getStuff(Crate_Stuff) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.Scii,
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
                canvasType = CanvasType.Scii,
                width = 2,
                height = 1,
                cells = listOf(listOf(BlockCellMother.WhiteBright, BlockCell.Transparent)),
            )

            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Crate_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldUnpackBlock() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(-1),
                ),
            )
        },
        act = { it.getStuff(Crate_Stuff) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.Scii,
                    width = 2,
                    height = 1,
                    cells = listOf(listOf(BlockCellMother.WhiteBright, BlockCell.Transparent)),
                ),
                it,
            )
        }
    )

    @Test
    fun shouldCopyTransformedFlipHorizontal() = performTest(
        arrange = {
            Crate(
                canvasType = CanvasType.HBlock,
                width = 2,
                height = 3,
                cells = listOf(
                    listOf(BlockCellMother.of(1), BlockCellMother.of(2)),
                    listOf(BlockCellMother.of(3), BlockCellMother.of(4)),
                    listOf(BlockCellMother.of(5), BlockCellMother.of(6)),
                ),
            )
        },
        act = { it.copyTransformed(TransformType.FlipHorizontal) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.HBlock,
                    width = 2,
                    height = 3,
                    cells = listOf(
                        listOf(BlockCellMother.of(2), BlockCellMother.of(1)),
                        listOf(BlockCellMother.of(4), BlockCellMother.of(3)),
                        listOf(BlockCellMother.of(6), BlockCellMother.of(5)),
                    ),
                ),
                it
            )
        },
    )

    @Test
    fun shouldCopyTransformedFlipVertical() = performTest(
        arrange = {
            Crate(
                canvasType = CanvasType.HBlock,
                width = 2,
                height = 3,
                cells = listOf(
                    listOf(BlockCellMother.of(1), BlockCellMother.of(2)),
                    listOf(BlockCellMother.of(3), BlockCellMother.of(4)),
                    listOf(BlockCellMother.of(5), BlockCellMother.of(6)),
                ),
            )
        },
        act = { it.copyTransformed(TransformType.FlipVertical) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.HBlock,
                    width = 2,
                    height = 3,
                    cells = listOf(
                        listOf(BlockCellMother.of(5), BlockCellMother.of(6)),
                        listOf(BlockCellMother.of(3), BlockCellMother.of(4)),
                        listOf(BlockCellMother.of(1), BlockCellMother.of(2)),
                    ),
                ),
                it
            )
        },
    )

    @Test
    fun shouldCopyTransformedRotateCW() = performTest(
        arrange = {
            Crate(
                canvasType = CanvasType.HBlock,
                width = 2,
                height = 3,
                cells = listOf(
                    listOf(BlockCellMother.of(1), BlockCellMother.of(2)),
                    listOf(BlockCellMother.of(3), BlockCellMother.of(4)),
                    listOf(BlockCellMother.of(5), BlockCellMother.of(6)),
                ),
            )
        },
        act = { it.copyTransformed(TransformType.RotateCw) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.HBlock,
                    width = 3,
                    height = 2,
                    cells = listOf(
                        listOf(BlockCellMother.of(5), BlockCellMother.of(3), BlockCellMother.of(1)),
                        listOf(BlockCellMother.of(6), BlockCellMother.of(4), BlockCellMother.of(2)),
                    ),
                ),
                it
            )
        },
    )

    @Test
    fun shouldCopyTransformedRotateCCW() = performTest(
        arrange = {
            Crate(
                canvasType = CanvasType.HBlock,
                width = 2,
                height = 3,
                cells = listOf(
                    listOf(BlockCellMother.of(1), BlockCellMother.of(2)),
                    listOf(BlockCellMother.of(3), BlockCellMother.of(4)),
                    listOf(BlockCellMother.of(5), BlockCellMother.of(6)),
                ),
            )
        },
        act = { it.copyTransformed(TransformType.RotateCcw) },
        assert = {
            assertEqualsExt(
                Crate(
                    canvasType = CanvasType.HBlock,
                    width = 3,
                    height = 2,
                    cells = listOf(
                        listOf(BlockCellMother.of(2), BlockCellMother.of(4), BlockCellMother.of(6)),
                        listOf(BlockCellMother.of(1), BlockCellMother.of(3), BlockCellMother.of(5)),
                    ),
                ),
                it
            )
        },
    )

    private fun assertEqualsExt(expected: Crate<*>, actual: Crate<*>) =
        assertEquals(expected, actual, "expected: ${crateToStringFull(expected)} but was: ${crateToStringFull(actual)}")

    private fun crateToStringFull(crate: Crate<*>) =
        "Crate(canvasType=${crate.canvasType}, width=${crate.width}, height=${crate.height}, cells=${crate.cells})"
}
