package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ShapeTest {
    @Test
    fun shouldPackPoint() = performTest(
        arrange = {
            val sut = Shape.Points(listOf(5 to 8), BlockCellMother.WhiteBright)
            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i1u1i1i5I08u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackPoint() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i1u1i1i5I08u1i2u1i7i1") },
        act = {
            val sut = it.getStuff(Shape)

            sut to listOf(
                sut.type,
                sut.cellType,
                (sut as? Shape.Points<*>)?.points,
                (sut as? Shape.Points<*>)?.cell,
            )
        },
        assert = { (sut, props) ->
            assertIs<Shape.Points<*>>(sut)
            assertEquals(listOf(ShapeType.Points, CellType.Block, listOf(5 to 8), BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackLine() = performTest(
        arrange = {
            val sut = Shape.Line(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i2u1i1i2i5I08u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackLine() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i2u1i1i2i5I08u1i2u1i7i1") },
        act = {
            val sut = it.getStuff(Shape)

            sut to listOf(
                sut.type,
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
            assertEquals(listOf(ShapeType.Line, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackFillBox() = performTest(
        arrange = {
            val sut = Shape.FillBox(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i3u1i1i2i5I08u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackFillBox() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i3u1i1i2i5I08u1i2u1i7i1") },
        act = {
            val sut = it.getStuff(Shape)

            sut to listOf(
                sut.type,
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
            assertEquals(listOf(ShapeType.FillBox, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
        }
    )

    @Test
    fun shouldPackStrokeBox() = performTest(
        arrange = {
            val sut = Shape.StrokeBox(1, 2, 5, 8, BlockCellMother.WhiteBright)
            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i4u1i1i2i5I08u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackStrokeBox() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i4u1i1i2i5I08u1i2u1i7i1") },
        act = {
            val sut = it.getStuff(Shape)

            sut to listOf(
                sut.type,
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
            assertEquals(listOf(ShapeType.StrokeBox, CellType.Block, 1, 2, 5, 8, BlockCellMother.WhiteBright), props)
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

            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Shape, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i5u1i1i2u1i2i2i1u1i2u1i0iFu1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpackCells() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i5u1i1i2u1i2i2i1u1i2u1i0iFu1i2u1i7i1") },
        act = {
            val sut = it.getStuff(Shape)

            sut to listOf(
                sut.type,
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
                    ShapeType.Cells,
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
