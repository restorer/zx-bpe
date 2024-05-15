package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.CellType
import com.eightsines.bpe.test.BlockCellMother
import com.eightsines.bpe.test.TestPencil
import com.eightsines.bpe.test.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PainterTest {
    // BBox

    @Test
    fun shouldGetBBoxPoint() = performTest(
        arrange = { Painter() to Shape.Point(5, 8, BlockCellMother.White) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(5, 8, 1, 1), it) }
    )

    @Test
    fun shouldGetBBoxLine() = performTest(
        arrange = { Painter() to Shape.Line(5, 2, 1, 8, BlockCellMother.White) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxFillBox() = performTest(
        arrange = { Painter() to Shape.FillBox(5, 2, 1, 8, BlockCellMother.White) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxStrokeBox() = performTest(
        arrange = { Painter() to Shape.StrokeBox(5, 2, 1, 8, BlockCellMother.White) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxCells() = performTest(
        arrange = {
            Painter() to Shape.Cells(
                5,
                2,
                Crate(
                    CellType.Block,
                    2,
                    1,
                    listOf(listOf(BlockCellMother.Black, BlockCellMother.White)),
                ),
            )
        },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(5, 2, 2, 1), it) }
    )

    // Paint

    @Test
    fun shouldPaintPoint() = performTest(
        arrange = { Painter() to Shape.Point(5, 8, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(5, 8, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDxFwd() = performTest(
        arrange = { Painter() to Shape.Line(2, 3, 5, 3, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(2, 3, BlockCellMother.White),
                    TestPencil.Point(3, 3, BlockCellMother.White),
                    TestPencil.Point(4, 3, BlockCellMother.White),
                    TestPencil.Point(5, 3, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDxBkw() = performTest(
        arrange = { Painter() to Shape.Line(5, 3, 2, 3, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(2, 3, BlockCellMother.White),
                    TestPencil.Point(3, 3, BlockCellMother.White),
                    TestPencil.Point(4, 3, BlockCellMother.White),
                    TestPencil.Point(5, 3, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDyFwd() = performTest(
        arrange = { Painter() to Shape.Line(3, 2, 3, 5, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(3, 2, BlockCellMother.White),
                    TestPencil.Point(3, 3, BlockCellMother.White),
                    TestPencil.Point(3, 4, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDyBkw() = performTest(
        arrange = { Painter() to Shape.Line(3, 5, 3, 2, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(3, 2, BlockCellMother.White),
                    TestPencil.Point(3, 3, BlockCellMother.White),
                    TestPencil.Point(3, 4, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDxFwd() = performTest(
        arrange = { Painter() to Shape.Line(8, 1, 2, 5, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(2, 1, BlockCellMother.White),
                    TestPencil.Point(3, 2, BlockCellMother.White),
                    TestPencil.Point(4, 2, BlockCellMother.White),
                    TestPencil.Point(5, 3, BlockCellMother.White),
                    TestPencil.Point(6, 4, BlockCellMother.White),
                    TestPencil.Point(7, 4, BlockCellMother.White),
                    TestPencil.Point(8, 5, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDxBkw() = performTest(
        arrange = { Painter() to Shape.Line(2, 5, 8, 1, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(2, 5, BlockCellMother.White),
                    TestPencil.Point(3, 4, BlockCellMother.White),
                    TestPencil.Point(4, 4, BlockCellMother.White),
                    TestPencil.Point(5, 3, BlockCellMother.White),
                    TestPencil.Point(6, 2, BlockCellMother.White),
                    TestPencil.Point(7, 2, BlockCellMother.White),
                    TestPencil.Point(8, 1, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDyFwd() = performTest(
        arrange = { Painter() to Shape.Line(1, 8, 5, 2, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(1, 2, BlockCellMother.White),
                    TestPencil.Point(2, 3, BlockCellMother.White),
                    TestPencil.Point(2, 4, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),
                    TestPencil.Point(4, 6, BlockCellMother.White),
                    TestPencil.Point(4, 7, BlockCellMother.White),
                    TestPencil.Point(5, 8, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkeyDyBkw() = performTest(
        arrange = { Painter() to Shape.Line(5, 2, 1, 8, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(5, 2, BlockCellMother.White),
                    TestPencil.Point(4, 3, BlockCellMother.White),
                    TestPencil.Point(4, 4, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),
                    TestPencil.Point(2, 6, BlockCellMother.White),
                    TestPencil.Point(2, 7, BlockCellMother.White),
                    TestPencil.Point(1, 8, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintFillBox() = performTest(
        arrange = { Painter() to Shape.FillBox(3, 2, 1, 5, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(1, 2, BlockCellMother.White),
                    TestPencil.Point(2, 2, BlockCellMother.White),
                    TestPencil.Point(3, 2, BlockCellMother.White),

                    TestPencil.Point(1, 3, BlockCellMother.White),
                    TestPencil.Point(2, 3, BlockCellMother.White),
                    TestPencil.Point(3, 3, BlockCellMother.White),

                    TestPencil.Point(1, 4, BlockCellMother.White),
                    TestPencil.Point(2, 4, BlockCellMother.White),
                    TestPencil.Point(3, 4, BlockCellMother.White),

                    TestPencil.Point(1, 5, BlockCellMother.White),
                    TestPencil.Point(2, 5, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintStrokeBox() = performTest(
        arrange = { Painter() to Shape.StrokeBox(3, 2, 1, 5, BlockCellMother.White) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(1, 2, BlockCellMother.White),
                    TestPencil.Point(2, 2, BlockCellMother.White),
                    TestPencil.Point(3, 2, BlockCellMother.White),

                    TestPencil.Point(1, 5, BlockCellMother.White),
                    TestPencil.Point(2, 5, BlockCellMother.White),
                    TestPencil.Point(3, 5, BlockCellMother.White),

                    TestPencil.Point(1, 3, BlockCellMother.White),
                    TestPencil.Point(1, 4, BlockCellMother.White),

                    TestPencil.Point(3, 3, BlockCellMother.White),
                    TestPencil.Point(3, 4, BlockCellMother.White),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintCells() = performTest(
        arrange = {
            Painter() to Shape.Cells(
                5,
                2,
                Crate(
                    CellType.Block,
                    3,
                    2,
                    listOf(
                        listOf(BlockCellMother.Black, BlockCellMother.Black, BlockCellMother.White),
                        listOf(BlockCellMother.White, BlockCellMother.White, BlockCellMother.Black),
                    ),
                ),
            )
        },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                listOf(
                    TestPencil.Point(5, 2, BlockCellMother.Black),
                    TestPencil.Point(6, 2, BlockCellMother.Black),
                    TestPencil.Point(7, 2, BlockCellMother.White),

                    TestPencil.Point(5, 3, BlockCellMother.White),
                    TestPencil.Point(6, 3, BlockCellMother.White),
                    TestPencil.Point(7, 3, BlockCellMother.Black),
                ),
                it.testPoints,
            )
        }
    )
}
