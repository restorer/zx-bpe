package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.model.VBlockMergeCell
import com.eightsines.bpe.test.BlockCellMother
import com.eightsines.bpe.test.SciiCellMother
import com.eightsines.bpe.test.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame

class MutableVBlockCanvasTest {
    // Non-mutable

    @Test
    fun shouldHaveProperDrawingSize() = performTest(
        arrange = { MutableVBlockCanvas(4, 3) },
        act = { it.drawingWidth to it.drawingHeight },
        assert = { assertEquals(8 to 3, it) },
    )

    @Test
    fun shouldMapPosition() = performTest(
        arrange = { MutableVBlockCanvas(4, 4) },
        act = {
            listOf(
                it.type.toSciiPosition(0, 0),
                it.type.toSciiPosition(0, 1),
                it.type.toSciiPosition(3, 2),
                it.type.toSciiPosition(3, 3),
            )
        },
        assert = { assertEquals(listOf(0 to 0, 0 to 1, 1 to 2, 1 to 3), it) },
    )

    @Test
    fun shouldGetInitialDrawingCell() = performTest(
        arrange = { MutableVBlockCanvas(1, 1) },
        act = {
            listOf(
                it.getDrawingCell(0, 0),
                it.getDrawingCell(1, 0),
            )
        },
        assert = { assertEquals(listOf(BlockCell.Transparent, BlockCell.Transparent), it) },
    )

    @Test
    fun shouldGetInitialSciiCell() = performTest(
        arrange = { MutableVBlockCanvas(1, 1) },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldGetTransparentDrawingOutside() = performTest(
        arrange = {
            MutableVBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft)
                }
            }
        },
        act = {
            listOf(
                it.getDrawingCell(-1, -1),
                it.getDrawingCell(2, 1),
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
            MutableVBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft)
                    it.replaceSciiCell(1, 0, SciiCellMother.BlockVerticalLeft)
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
            MutableVBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.mergeDrawingCell(0, 0, BlockCellMother.Black)
                    it.mergeDrawingCell(1, 0, BlockCellMother.White)
                }
            }
        },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCellMother.BlockVerticalLeft, it) },
    )

    @Test
    fun shouldGetNonInitialMergeCell() = performTest(
        arrange = {
            MutableVBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.mergeDrawingCell(0, 0, BlockCellMother.Black)
                    it.mergeDrawingCell(1, 0, BlockCellMother.White)
                }
            }
        },
        act = { it.getMergeCell(0, 0) },
        assert = {
            assertEquals(
                VBlockMergeCell(leftColor = SciiColor.Black, rightColor = SciiColor.White, bright = SciiLight.On),
                it,
            )
        },
    )

    @Test
    fun shouldNotGetMergeCellOutsize() = performTest(
        arrange = {
            MutableVBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.mergeDrawingCell(0, 0, BlockCellMother.White)
                    it.mergeDrawingCell(1, 0, BlockCellMother.Black)
                }
            }
        },
        act = {
            listOf(
                it.getMergeCell(-1, -1),
                it.getMergeCell(1, 1),
            )
        },
        assert = {
            assertEquals(
                listOf(VBlockMergeCell.Transparent, VBlockMergeCell.Transparent),
                it,
            )
        },
    )

    // Mutable

    @Test
    fun shouldChangeMutations() = performTest(
        arrange = {
            val sut = MutableVBlockCanvas(1, 1)
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft) }
            sut to sut.mutations
        },
        act = { (sut, initialMutations) ->
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft) }
            initialMutations to sut.mutations
        },
        assert = { (initialMutations, actualMutations) ->
            assertNotSame(initialMutations, actualMutations)
        },
    )

    @Test
    fun shouldClear() = performTest(
        arrange = {
            MutableVBlockCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft)
                    it.replaceSciiCell(1, 0, SciiCellMother.BlockVerticalLeft)
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
            MutableVBlockCanvas(1, 2).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, BlockCellMother.White)
                    it.replaceDrawingCell(0, 1, BlockCellMother.Black)
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

                it.mergeDrawingCell(1, 1, BlockCellMother.White)
            }

            listOf(
                sut.getDrawingCell(0, 0),
                sut.getDrawingCell(0, 1),
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
            MutableVBlockCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, BlockCellMother.White)
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
        arrange = { MutableVBlockCanvas(1, 2) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(0, 0, SciiCellMother.BlockVerticalLeft)
                it.replaceSciiCell(0, 1, SciiCellMother.RedSpace)
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
                    BlockCell(color = SciiColor.Black, bright = SciiLight.On),
                    BlockCellMother.White,
                    BlockCell(color = SciiColor.Red, bright = SciiLight.Transparent),
                    BlockCell(color = SciiColor.Red, bright = SciiLight.Transparent),
                ),
                it,
            )
        },
    )

    @Test
    fun shouldNotMergeDrawingOutside() = performTest(
        arrange = { MutableVBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(-1, -1, BlockCellMother.White)
                it.mergeDrawingCell(1, 2, BlockCellMother.White)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldNotReplaceDrawingOutside() = performTest(
        arrange = { MutableVBlockCanvas(1, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(-1, -1, BlockCellMother.White)
                it.replaceDrawingCell(1, 2, BlockCellMother.White)
            }

            sut.getSciiCell(0, 0)
        },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldNotReplaceSciiOutside() = performTest(
        arrange = { MutableVBlockCanvas(2, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceSciiCell(-1, -1, SciiCellMother.BlockVerticalLeft)
                it.replaceSciiCell(2, 1, SciiCellMother.BlockVerticalLeft)
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
            val sut = MutableVBlockCanvas(1, 2)

            sut.mutate {
                it.replaceDrawingCell(0, 0, BlockCellMother.Black)
                it.replaceDrawingCell(1, 1, BlockCellMother.White)
            }

            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Canvas, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i3i1i2u1u1i2u1i0iFu1i2u1iFiFu1i2u1iFi1u1i2u1i7i1", it) },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i3i1i2u1u1i2u1i0iFu1i2u1iFiFu1i2u1iFi1u1i2u1i7i1") },
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
            assertIs<MutableVBlockCanvas>(sut)

            assertEquals(
                listOf(
                    1 to 2,
                    BlockCellMother.Black,
                    BlockCell.Transparent,
                    BlockCell(color = SciiColor.Transparent, bright = SciiLight.On),
                    BlockCellMother.White,
                ),
                props,
            )
        },
    )
}
