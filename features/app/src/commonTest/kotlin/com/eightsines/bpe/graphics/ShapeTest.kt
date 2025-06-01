package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ShapeTest {
    @Test
    fun shouldPackLinkedPoints() = performTest(
        arrange = {
            val sut = Shape.LinkedPoints(listOf(5 to 8, 3 to 4), BlockCellMother.WhiteBright)
            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
                    TestWare.IntWare(3),
                    TestWare.IntWare(4),
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
    fun shouldUnpackLinkedPoints() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
                    TestWare.IntWare(3),
                    TestWare.IntWare(4),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(Shape_Stuff)

            sut to listOf(
                sut is Shape.LinkedPoints,
                sut.cellType,
                (sut as? Shape.LinkedPoints<*>)?.points,
                (sut as? Shape.LinkedPoints<*>)?.cell,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.LinkedPoints<*>>(sut)
            assertEquals(listOf(true, CellType.Block, listOf(5 to 8, 3 to 4), BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackLine() = performTest(
        arrange = {
            val sut = Shape.Line(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
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
    fun shouldUnpackLine() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(Shape_Stuff)

            sut to listOf(
                sut is Shape.Line,
                sut.cellType,
                (sut as? Shape.Line<*>)?.sx,
                (sut as? Shape.Line<*>)?.sy,
                (sut as? Shape.Line<*>)?.ex,
                (sut as? Shape.Line<*>)?.ey,
                (sut as? Shape.Line<*>)?.cell,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.Line<*>>(sut)
            assertEquals(listOf(true, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackFillBox() = performTest(
        arrange = {
            val sut = Shape.FillBox(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(3),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
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
    fun shouldUnpackFillBox() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(3),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(Shape_Stuff)

            sut to listOf(
                sut is Shape.FillBox,
                sut.cellType,
                (sut as? Shape.FillBox<*>)?.sx,
                (sut as? Shape.FillBox<*>)?.sy,
                (sut as? Shape.FillBox<*>)?.ex,
                (sut as? Shape.FillBox<*>)?.ey,
                (sut as? Shape.FillBox<*>)?.cell,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.FillBox<*>>(sut)
            assertEquals(listOf(true, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackStrokeBox() = performTest(
        arrange = {
            val sut = Shape.StrokeBox(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(4),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
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
    fun shouldUnpackStrokeBox() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(4),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(5),
                    TestWare.IntWare(8),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(Shape_Stuff)

            sut to listOf(
                sut is Shape.StrokeBox,
                sut.cellType,
                (sut as? Shape.StrokeBox<*>)?.sx,
                (sut as? Shape.StrokeBox<*>)?.sy,
                (sut as? Shape.StrokeBox<*>)?.ex,
                (sut as? Shape.StrokeBox<*>)?.ey,
                (sut as? Shape.StrokeBox<*>)?.cell,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.StrokeBox<*>>(sut)
            assertEquals(listOf(true, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackCells() = performTest(
        arrange = {
            val sut = Shape.Cells(
                1,
                2,
                Crate(
                    CanvasType.HBlock,
                    2,
                    1,
                    listOf(listOf(BlockCellMother.Black, BlockCellMother.WhiteBright)),
                )
            )

            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(5),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1),
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
    fun shouldUnpackCells() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(5),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(Shape_Stuff)

            sut to listOf(
                sut is Shape.Cells,
                sut.cellType,
                (sut as? Shape.Cells<*>)?.x,
                (sut as? Shape.Cells<*>)?.y,
                (sut as? Shape.Cells<*>)?.crate,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.Cells<*>>(sut)

            assertEquals(
                listOf(
                    true,
                    CellType.Block,
                    1,
                    2, Crate(
                        CanvasType.HBlock,
                        2,
                        1,
                        listOf(listOf(BlockCellMother.Black, BlockCellMother.WhiteBright)),
                    )
                ),
                props,
            )
        }
    )
}
