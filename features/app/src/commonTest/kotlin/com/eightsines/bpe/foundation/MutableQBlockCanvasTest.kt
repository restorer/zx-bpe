package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
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

class MutableQBlockCanvasTest {
    // Non-mutable

    @Test
    fun shouldHaveProperDrawingSize() = performTest(
        arrange = { MutableQBlockCanvas(4, 3) },
        act = { it.drawingWidth to it.drawingHeight },
        assert = { assertEquals(8 to 6, it) },
    )

    @Test
    fun shouldMapPosition() = performTest(
        arrange = { MutableQBlockCanvas(4, 4) },
        act = {
            listOf(
                it.type.toSciiPosition(0, 0),
                it.type.toSciiPosition(0, 1),
                it.type.toSciiPosition(3, 2),
                it.type.toSciiPosition(3, 3),
            )
        },
        assert = { assertEquals(listOf(0 to 0, 0 to 0, 1 to 1, 1 to 1), it) },
    )

    @Test
    fun shouldGetInitialDrawingCell() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = {
            listOf(
                it.getDrawingCell(0, 0),
                it.getDrawingCell(1, 0),
                it.getDrawingCell(0, 1),
                it.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldGetInitialSciiCell() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldGetTransparentDrawingOutside() = performTest(
        arrange = {
            MutableQBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.RedSpace)
                }
            }
        },
        act = {
            listOf(
                it.getDrawingCell(-1, -1),
                it.getDrawingCell(2, 2),
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
            MutableQBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.RedSpace)
                    it.replaceSciiCell(1, 0, SciiCellMother.RedSpace)
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
            MutableQBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.mergeDrawingCell(0, 0, BlockCellMother.Black)
                    it.mergeDrawingCell(1, 1, BlockCellMother.WhiteBright)
                }
            }
        },
        act = { it.getSciiCell(0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar(SciiChar.BLOCK_VALUE_FIRST + SciiChar.BLOCK_BIT_TL + SciiChar.BLOCK_BIT_BR),
                    ink = SciiColor.White,
                    paper = SciiColor.Transparent,
                    bright = SciiLight.On,
                    flash = SciiLight.Transparent,
                ),
                it,
            )
        },
    )

    // Mutable

    @Test
    fun shouldChangeMutations() = performTest(
        arrange = {
            val sut = MutableQBlockCanvas(1, 1)
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
            sut to sut.mutations
        },
        act = { (sut, initialMutations) ->
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
            initialMutations to sut.mutations
        },
        assert = { (initialMutations, actualMutations) ->
            assertNotSame(initialMutations, actualMutations)
        },
    )

    @Test
    fun shouldClear() = performTest(
        arrange = {
            MutableQBlockCanvas(2, 1).also { sut ->
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
            MutableQBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, BlockCellMother.WhiteBright)
                    it.replaceDrawingCell(2, 0, BlockCellMother.Black)
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

                it.mergeDrawingCell(2, 1, BlockCellMother.WhiteBright)
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(2, 0),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCell(color = SciiColor.White, bright = SciiLight.Off),
                    BlockCellMother.WhiteBright,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceDrawingCell() = performTest(
        arrange = {
            MutableQBlockCanvas(1, 1).also { sut ->
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
    fun shouldReplaceSciiCellTransparent() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.BlockHorizontalTop,
                        ink = SciiColor.Transparent,
                        paper = SciiColor.Transparent,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    ),
                )
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceSciiCellInk() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.BlockHorizontalTop,
                        ink = SciiColor.White,
                        paper = SciiColor.Transparent,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    ),
                )
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCellMother.WhiteBright,
                    BlockCellMother.WhiteBright,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceSciiCellPaper() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.BlockHorizontalTop,
                        ink = SciiColor.Transparent,
                        paper = SciiColor.White,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    ),
                )
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCellMother.WhiteBright,
                    BlockCellMother.WhiteBright,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceSciiBoth() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.BlockHorizontalTop,
                        ink = SciiColor.White,
                        paper = SciiColor.Black,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    ),
                )
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    BlockCellMother.WhiteBright,
                    BlockCellMother.WhiteBright,
                    BlockCellMother.WhiteBright,
                    BlockCellMother.WhiteBright,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldNotMergeDrawingOutside() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(-1, -1, BlockCellMother.WhiteBright)
                it.mergeDrawingCell(2, 2, BlockCellMother.WhiteBright)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldNotReplaceDrawingOutside() = performTest(
        arrange = { MutableQBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(-1, -1, BlockCellMother.WhiteBright)
                it.replaceDrawingCell(2, 2, BlockCellMother.WhiteBright)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    // Bag

    @Test
    fun shouldPack() = performTest(
        arrange = {
            val sut = MutableQBlockCanvas(2, 1)

            sut.mutate {
                it.replaceDrawingCell(0, 0, BlockCellMother.WhiteBright)
                it.replaceDrawingCell(2, 0, BlockCellMother.Black)
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
                    TestWare.IntWare(4),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1)
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
                    TestWare.IntWare(4),
                    TestWare.IntWare(2),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(true),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.BooleanWare(false),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(7),
                    TestWare.IntWare(1),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(2),
                    TestWare.StuffWare(1),
                    TestWare.IntWare(0),
                    TestWare.IntWare(-1),
                ),
            )
        },
        act = {
            val sut = it.getStuff(MutableCanvas)

            sut to listOf(
                sut.sciiWidth to sut.sciiHeight,
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(1, 0),
                sut.getDrawingCell(2, 0),
                sut.getDrawingCell(3, 0),
                sut.getDrawingCell(0, 1),
                sut.getDrawingCell(1, 1),
                sut.getDrawingCell(2, 1),
                sut.getDrawingCell(3, 1),
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableQBlockCanvas>(sut)

            assertEquals(
                listOf(
                    2 to 1,
                    BlockCellMother.WhiteBright,
                    BlockCell.Transparent,
                    BlockCellMother.Black,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                    BlockCell.Transparent,
                ),
                props,
            )
        },
    )
}
