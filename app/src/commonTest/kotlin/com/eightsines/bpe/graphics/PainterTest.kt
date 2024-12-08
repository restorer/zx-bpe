package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.TestPencil
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PainterTest {
    // BBox

    @Test
    fun shouldGetBBoxPoint() = performTest(
        arrange = { Painter() to Shape.Points(listOf(5 to 8), BlockCellMother.WhiteBright) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(5, 8, 1, 1), it) }
    )

    @Test
    fun shouldGetBBoxLine() = performTest(
        arrange = { Painter() to Shape.Line(5, 2, 1, 8, BlockCellMother.WhiteBright) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxFillBox() = performTest(
        arrange = { Painter() to Shape.FillBox(5, 2, 1, 8, BlockCellMother.WhiteBright) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxStrokeBox() = performTest(
        arrange = { Painter() to Shape.StrokeBox(5, 2, 1, 8, BlockCellMother.WhiteBright) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxFillEllipse() = performTest(
        arrange = { Painter() to Shape.FillEllipse(5, 2, 1, 8, BlockCellMother.WhiteBright) },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(1, 2, 5, 7), it) }
    )

    @Test
    fun shouldGetBBoxStrokeEllipse() = performTest(
        arrange = { Painter() to Shape.StrokeEllipse(5, 2, 1, 8, BlockCellMother.WhiteBright) },
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
                    CanvasType.HBlock,
                    2,
                    1,
                    listOf(listOf(BlockCellMother.Black, BlockCellMother.WhiteBright)),
                ),
            )
        },
        act = { (sut, shape) -> sut.getBBox(shape) },
        assert = { assertEquals(Box(5, 2, 2, 1), it) }
    )

    // Paint

    @Test
    fun shouldPaintPoint() = performTest(
        arrange = { Painter() to Shape.Points(listOf(5 to 8), BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(5, 8, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDxFwd() = performTest(
        arrange = { Painter() to Shape.Line(2, 3, 5, 3, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(2, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(5, 3, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDxBkw() = performTest(
        arrange = { Painter() to Shape.Line(5, 3, 2, 3, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(5, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 3, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDyFwd() = performTest(
        arrange = { Painter() to Shape.Line(3, 2, 3, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(3, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineStraightDyBkw() = performTest(
        arrange = { Painter() to Shape.Line(3, 5, 3, 2, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 2, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDxFwd() = performTest(
        arrange = { Painter() to Shape.Line(8, 1, 2, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(8, 1, BlockCellMother.WhiteBright),
                    TestPencil.Point(7, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(6, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(5, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDxBkw() = performTest(
        arrange = { Painter() to Shape.Line(2, 5, 8, 1, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(5, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(6, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(7, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(8, 1, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDyFwd() = performTest(
        arrange = { Painter() to Shape.Line(1, 8, 5, 2, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(1, 8, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 7, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 6, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(5, 2, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintLineSkewDyBkw() = performTest(
        arrange = { Painter() to Shape.Line(5, 2, 1, 8, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(5, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(4, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 6, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 7, BlockCellMother.WhiteBright),
                    TestPencil.Point(1, 8, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintFillBox() = performTest(
        arrange = { Painter() to Shape.FillBox(3, 2, 1, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(1, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 2, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintStrokeBox() = performTest(
        arrange = { Painter() to Shape.StrokeBox(3, 2, 1, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(1, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 2, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 5, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(1, 4, BlockCellMother.WhiteBright),

                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintFillEllipse() = performTest(
        arrange = { Painter() to Shape.FillEllipse(3, 2, 1, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(2, 2, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),

                    TestPencil.Point(1, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),

                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
                ),
                it.testPoints,
            )
        }
    )

    @Test
    fun shouldPaintStrokeEllipse() = performTest(
        arrange = { Painter() to Shape.StrokeEllipse(3, 2, 1, 5, BlockCellMother.WhiteBright) },
        act = { (sut, shape) ->
            val pencil = TestPencil<BlockCell>()
            sut.paint(shape, pencil)
            pencil
        },
        assert = {
            assertEquals(
                setOf(
                    TestPencil.Point(2, 2, BlockCellMother.WhiteBright),
                    TestPencil.Point(1, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(1, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(3, 4, BlockCellMother.WhiteBright),
                    TestPencil.Point(2, 5, BlockCellMother.WhiteBright),
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
                    CanvasType.HBlock,
                    3,
                    2,
                    listOf(
                        listOf(BlockCellMother.Black, BlockCellMother.Black, BlockCellMother.WhiteBright),
                        listOf(BlockCellMother.WhiteBright, BlockCellMother.WhiteBright, BlockCellMother.Black),
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
                setOf(
                    TestPencil.Point(5, 2, BlockCellMother.Black),
                    TestPencil.Point(6, 2, BlockCellMother.Black),
                    TestPencil.Point(7, 2, BlockCellMother.WhiteBright),

                    TestPencil.Point(5, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(6, 3, BlockCellMother.WhiteBright),
                    TestPencil.Point(7, 3, BlockCellMother.Black),
                ),
                it.testPoints,
            )
        }
    )
}
