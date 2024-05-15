package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.test.SciiCellMother
import com.eightsines.bpe.test.performTest
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame

class MutableSciiCanvasTest {
    // Non-mutable

    @Test
    fun shouldHaveProperDrawingSize() = performTest(
        arrange = { MutableSciiCanvas(4, 3) },
        act = { it.drawingWidth to it.drawingHeight },
        assert = { assertEquals(4 to 3, it) },
    )

    @Test
    fun shouldMapPosition() = performTest(
        arrange = { MutableSciiCanvas(4, 2) },
        act = {
            listOf(
                it.toSciiPosition(0, 0),
                it.toSciiPosition(0, 1),
                it.toSciiPosition(3, 0),
                it.toSciiPosition(3, 1),
            )
        },
        assert = { assertEquals(listOf(0 to 0, 0 to 1, 3 to 0, 3 to 1), it) },
    )

    @Test
    fun shouldGetInitialDrawingCell() = performTest(
        arrange = { MutableSciiCanvas(1, 1) },
        act = { it.getDrawingCell(0, 0) },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldGetInitialSciiCell() = performTest(
        arrange = { MutableSciiCanvas(1, 1) },
        act = { it.getSciiCell(0, 0) },
        assert = { assertEquals(SciiCell.Transparent, it) },
    )

    @Test
    fun shouldGetTransparentDrawingOutside() = performTest(
        arrange = {
            MutableSciiCanvas(2, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                    it.replaceSciiCell(1, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = {
            listOf(
                it.getDrawingCell(-1, -1),
                it.getDrawingCell(2, 1),
            )
        },
        assert = { assertEquals(listOf(SciiCell.Transparent, SciiCell.Transparent), it) },
    )

    @Test
    fun shouldGetTransparentSciiOutside() = performTest(
        arrange = {
            MutableSciiCanvas(2, 1).also { sut ->
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
        assert = { assertEquals(listOf(SciiCell.Transparent, SciiCell.Transparent), it) },
    )

    // Mutable

    @Test
    fun shouldChangeMutations() = performTest(
        arrange = {
            val sut = MutableSciiCanvas(1, 1)
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
            MutableSciiCanvas(2, 1).also { sut ->
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
            MutableSciiCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.Transparent,
                        ink = SciiColor.White,
                        paper = SciiColor.Red,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    )
                )
            }
            sut.getDrawingCell(0, 0)
        },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.BlockHorizontalTop,
                    ink = SciiColor.White,
                    paper = SciiColor.Red,
                    bright = SciiLight.On,
                    flash = SciiLight.Transparent,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceDrawingCell() = performTest(
        arrange = {
            MutableSciiCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceDrawingCell(0, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(
                    0,
                    0,
                    SciiCell(
                        character = SciiChar.Transparent,
                        ink = SciiColor.White,
                        paper = SciiColor.Red,
                        bright = SciiLight.On,
                        flash = SciiLight.Transparent,
                    )
                )
            }

            sut.getDrawingCell(0, 0)
        },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.Transparent,
                    ink = SciiColor.White,
                    paper = SciiColor.Red,
                    bright = SciiLight.On,
                    flash = SciiLight.Transparent,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldReplaceSciiCell() = performTest(
        arrange = {
            MutableSciiCanvas(1, 1).also { sut ->
                sut.mutate {
                    it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                }
            }
        },
        act = { sut ->
            sut.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
            sut.getDrawingCell(0, 0)
        },
        assert = { assertEquals(SciiCellMother.RedSpace, it) },
    )

    @Test
    fun shouldNotMergeDrawingOutside() = performTest(
        arrange = { MutableSciiCanvas(2, 1) },
        act = { sut ->
            sut.mutate {
                it.mergeDrawingCell(-1, -1, SciiCellMother.BlockHorizontalTop)
                it.mergeDrawingCell(2, 1, SciiCellMother.BlockHorizontalTop)
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

    @Test
    fun shouldNotReplaceDrawingOutside() = performTest(
        arrange = { MutableSciiCanvas(2, 1) },
        act = { sut ->
            sut.mutate {
                it.replaceDrawingCell(-1, -1, SciiCellMother.BlockHorizontalTop)
                it.replaceDrawingCell(2, 1, SciiCellMother.BlockHorizontalTop)
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

    @Test
    fun shouldNotReplaceSciiOutside() = performTest(
        arrange = { MutableSciiCanvas(2, 1) },
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
            val sut = MutableSciiCanvas(2, 1)

            sut.mutate {
                it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop)
                it.replaceSciiCell(1, 0, SciiCellMother.RedSpace)
            }

            sut to PackableStringBag()
        },
        act = { (sut, bag) ->
            bag.put(Canvas, sut)
            bag.toString()
        },
        assert = { assertEquals("BAG1u1i1i2i1u1u1i1u1n0083i7i0i1iFu1i1u1I20iFi2iFiF", it) },
    )

    @Test
    fun shouldUnpack() = performTest(
        arrange = { UnpackableStringBag("BAG1u1i1i2i1u1u1i1u1n0083i7i0i1iFu1i1u1I20iFi2iFiF") },
        act = {
            val sut = it.getStuff(MutableCanvas)

            sut to listOf(
                sut.sciiWidth to sut.sciiHeight,
                sut.getSciiCell(0, 0),
                sut.getSciiCell(1, 0),
            )
        },
        assert = { (sut, props) ->
            assertIs<MutableSciiCanvas>(sut)

            assertEquals(
                listOf(
                    2 to 1,
                    SciiCellMother.BlockHorizontalTop,
                    SciiCellMother.RedSpace,
                ),
                props,
            )
        },
    )
}
