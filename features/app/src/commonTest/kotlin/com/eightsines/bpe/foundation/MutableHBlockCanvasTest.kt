package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.PackableTestBag
import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.TestWare
import com.eightsines.bpe.testing.UnpackableTestBag
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame

class MutableHBlockCanvasTest {
    // Non-mutable

    @Test
    fun shouldHaveProperDrawingSize() = performTest(
        arrange = { MutableHBlockCanvas(4, 3) },
        act = { it.drawingWidth to it.drawingHeight },
        assert = { assertEquals(4 to 6, it) },
    )

    @Test
    fun shouldMapPosition() = performTest(
        arrange = { MutableHBlockCanvas(4, 4) },
        act = {
            listOf(
                it.type.toSciiPosition(0, 0),
                it.type.toSciiPosition(0, 1),
                it.type.toSciiPosition(3, 2),
                it.type.toSciiPosition(3, 3),
            )
        },
        assert = { assertEquals(listOf(0 to 0, 0 to 0, 3 to 1, 3 to 1), it) },
    )

    @Test
    fun shouldGetInitialDrawingCell() = performTest(
        arrange = { MutableHBlockCanvas(1, 1) },
        act = {
            listOf(
                it.getDrawingCell(0, 0),
                it.getDrawingCell(0, 1),
            )
        },
        assert = { assertEquals(listOf(BlockCell.Transparent, BlockCell.Transparent), it) },
    )

    @Test
    fun shouldGetInitialSciiCell() = performTest(
        arrange = { MutableHBlockCanvas(1, 1) },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldGetTransparentDrawingOutside() = performTest(
        arrange = {
            MutableHBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = {
            listOf(
                it.getDrawingCell(-1, -1),
                it.getDrawingCell(1, 2),
            )
        },
        assert = {
            assertEquals(
                listOf(BlockCell.Transparent, BlockCell.Transparent),
                it,
            )
        },
    )

    @Test
    fun shouldGetTransparentSciiOutside() = performTest(
        arrange = {
            MutableHBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                    it.replaceSciiCell(1, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = {
            listOf(
                it.getSciiCell(-1, -1),
                it.getSciiCell(2, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(SciiCell.Transparent, SciiCell.Transparent),
                it,
            )
        },
    )

    @Test
    fun shouldGetNonInitialSciiCell() = performTest(
        arrange = {
            MutableHBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.mergeDrawingCell(0, 0, BlockCellMother.WhiteBright)
                    it.mergeDrawingCell(0, 1, BlockCellMother.Black)
                }
            }
        },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCellMother.BlockHorizontalTop, it) },
    )

    // Mutable

    @Test
    fun shouldChangeMutations() = performTest(
        arrange = {
            val sut = MutableHBlockCanvas(1, 1)
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop) }
            sut to sut.mutations
        },
        act = { (sut, initialMutations) ->
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop) }
            initialMutations to sut.mutations
        },
        assert = { (initialMutations, actualMutations) ->
            assertNotSame(initialMutations, actualMutations)
        },
    )

    @Test
    fun shouldClear() = performTest(
        arrange = {
            MutableHBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                    it.replaceSciiCell(1, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = { sut ->
            sut.mutate { it.clear() }

            listOf(
                sut.getSciiCell(0, 0),
                sut.getSciiCell(1, 0),
            )
        },
        assert = { assertEquals(listOf(SciiCell.Transparent, SciiCell.Transparent), it) },
    )

    @Test
    fun shouldMergeDrawingCell() = performTest(
        arrange = {
            MutableHBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, BlockCellMother.WhiteBright)
                    it.replaceDrawingCell(1, 0, BlockCellMother.Black)
                }
            }
        },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(
                    0,
                    0,
                    BlockCell(color = SciiColor.Transparent, bright = SciiLight.Off),
                )

                it.mergeDrawingCell(1, 1, BlockCellMother.WhiteBright)
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCell(color = SciiColor.White, bright = SciiLight.Off),
                    BlockCell(color = SciiColor.Black, bright = SciiLight.On),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceDrawingCell() = performTest(
        arrange = {
            MutableHBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, BlockCellMother.WhiteBright)
                }
            }
        },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(
                    0,
                    0,
                    BlockCell(color = SciiColor.Transparent, bright = SciiLight.Off),
                )
            }

            sut.getDrawingCell(0, 0)
        },
        assert = {
            assertEquals(BlockCell.Transparent, it)
        },
    )

    @Test
    fun shouldReplaceSciiCell() = performTest(
        arrange = { MutableHBlockCanvas(2, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                it.replaceSciiCell(1, 0, SciiCellMother.RedSpace)
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCellMother.WhiteBright,
                    BlockCell(color = SciiColor.Black, bright = SciiLight.On),
                    BlockCell(color = SciiColor.Red, bright = SciiLight.Transparent),
                    BlockCell(color = SciiColor.Red, bright = SciiLight.Transparent),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldNotMergeDrawingOutside() = performTest(
        arrange = { MutableHBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(-1, -1, BlockCellMother.WhiteBright)
                it.mergeDrawingCell(1, 2, BlockCellMother.WhiteBright)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldNotReplaceDrawingOutside() = performTest(
        arrange = { MutableHBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(-1, -1, BlockCellMother.WhiteBright)
                it.replaceDrawingCell(1, 2, BlockCellMother.WhiteBright)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldNotReplaceSciiOutside() = performTest(
        arrange = { MutableHBlockCanvas(2, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(-1, -1, SciiCellMother.BlockHorizontalTop)
                it.replaceSciiCell(2, 1, SciiCellMother.BlockHorizontalTop)
            }

            listOf(
                sut.getSciiCell(0, 0),
                sut.getSciiCell(1, 0),
            )
        },
        assert = {
            assertEquals(
                listOf(SciiCell.Transparent, SciiCell.Transparent),
                it,
            )
        },
    )

    // Bag

    @Test
    fun shouldPack() = performTest(
        arrange = {
            val sut = MutableHBlockCanvas(2, 1)

            sut.mutate {
                it.replaceDrawingCell(0, 0, BlockCellMother.Black)
                it.replaceDrawingCell(1, 1, BlockCellMother.WhiteBright)
            }

            sut to PackableTestBag()
        },
        act = { (sut, bag) ->
            bag.put(Canvas_Stuff, sut)
            bag.wares
        },
        assert = {
            assertEquals(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
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
    fun shouldUnpack() = performTest(
        arrange = {
            UnpackableTestBag(
                listOf(
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(-1),
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
            val sut = it.getStuff(MutableCanvas)

            sut to listOf(
                sut.sciiWidth to sut.sciiHeight,
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableHBlockCanvas>(sut)

            assertEquals(
                listOf(
                    2 to 1,
                    BlockCellMother.Black,
                    BlockCell(color = SciiColor.Transparent, bright = SciiLight.On),
                    BlockCell.Transparent,
                    BlockCellMother.WhiteBright,
                ),
                props,
            )
        },
    )
}
