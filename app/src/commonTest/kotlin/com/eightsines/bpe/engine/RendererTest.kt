package com.eightsines.bpe.engine

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.foundation.MutableHBlockCanvas
import com.eightsines.bpe.foundation.MutableQBlockCanvas
import com.eightsines.bpe.foundation.MutableSciiCanvas
import com.eightsines.bpe.foundation.MutableVBlockCanvas
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.MutableBackgroundLayer
import com.eightsines.bpe.foundation.MutableCanvasLayer
import com.eightsines.bpe.core.HBlockMergeCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.core.VBlockMergeCell
import com.eightsines.bpe.graphics.Renderer
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RendererTest {
    @Test
    fun shouldGroupLayers() = performTest(
        arrange = {
            Renderer() to listOf(
                MutableCanvasLayer(
                    uid = LayerUid("SCII_1_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableSciiCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("SCII_2_HIDDEN"),
                    isVisible = false,
                    isLocked = false,
                    canvas = MutableSciiCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("HBLOCK_1_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableHBlockCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("HBLOCK_2_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableHBlockCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("VBLOCK_1_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableVBlockCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("QBLOCK_1_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableQBlockCanvas(1, 1)
                ),
                MutableCanvasLayer(
                    uid = LayerUid("SCII_3_VISIBLE"),
                    isVisible = true,
                    isLocked = false,
                    canvas = MutableSciiCanvas(1, 1)
                ),
            )
        },
        act = { (sut, layers) -> sut.groupLayers(layers) to layers },
        assert = { (actual, layers) ->
            val expected = listOf(
                Renderer.MergeType.Scii to listOf(
                    layers.first { it.uid.value == "SCII_1_VISIBLE" }.canvas,
                ),
                Renderer.MergeType.HBlock to listOf(
                    layers.first { it.uid.value == "HBLOCK_1_VISIBLE" }.canvas,
                    layers.first { it.uid.value == "HBLOCK_2_VISIBLE" }.canvas,
                ),
                Renderer.MergeType.VBlock to listOf(
                    layers.first { it.uid.value == "VBLOCK_1_VISIBLE" }.canvas,
                ),
                Renderer.MergeType.Scii to listOf(
                    layers.first { it.uid.value == "QBLOCK_1_VISIBLE" }.canvas,
                    layers.first { it.uid.value == "SCII_3_VISIBLE" }.canvas,
                ),
            )

            assertEquals(expected, actual)
        },
    )

    @Test
    fun shouldMergeCellScii() = performTest(
        arrange = {
            Renderer() to listOf(
                Renderer.MergeType.Scii to listOf(
                    MutableSciiCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop) }
                    },
                    MutableSciiCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
                    },
                ),
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.Space,
                    ink = SciiColor.White,
                    paper = SciiColor.Red,
                    bright = SciiLight.On,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldMergeCellHBlock() = performTest(
        arrange = {
            Renderer() to listOf(
                Renderer.MergeType.HBlock to listOf(
                    MutableHBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateHBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                HBlockMergeCell(
                                    topColor = SciiColor.Red,
                                    bottomColor = SciiColor.Transparent,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                    MutableHBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateHBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                HBlockMergeCell(
                                    topColor = SciiColor.Transparent,
                                    bottomColor = SciiColor.Yellow,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                ),
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.BlockHorizontalTop,
                    ink = SciiColor.Red,
                    paper = SciiColor.Yellow,
                    bright = SciiLight.Off,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldMergeCellVBlock() = performTest(
        arrange = {
            Renderer() to listOf(
                Renderer.MergeType.VBlock to listOf(
                    MutableVBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateVBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                VBlockMergeCell(
                                    leftColor = SciiColor.Red,
                                    rightColor = SciiColor.Transparent,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                    MutableVBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateVBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                VBlockMergeCell(
                                    leftColor = SciiColor.Transparent,
                                    rightColor = SciiColor.Yellow,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                ),
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.BlockVerticalLeft,
                    ink = SciiColor.Red,
                    paper = SciiColor.Yellow,
                    bright = SciiLight.Off,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldMergeCellQBlock() = performTest(
        arrange = {
            Renderer() to listOf(
                Renderer.MergeType.Scii to listOf(
                    MutableQBlockCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceDrawingCell(0, 0, BlockCellMother.White) }
                    },
                    MutableQBlockCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceDrawingCell(1, 1, BlockCellMother.Black) }
                    },
                ),
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar(SciiChar.BLOCK_VALUE_FIRST + SciiChar.BLOCK_BIT_BR),
                    ink = SciiColor.Black,
                    paper = SciiColor.Blue,
                    bright = SciiLight.On,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldMergeCellMulti() = performTest(
        arrange = {
            Renderer() to listOf(
                Renderer.MergeType.Scii to listOf(
                    MutableSciiCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
                    },
                ),
                Renderer.MergeType.HBlock to listOf(
                    MutableHBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateHBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                HBlockMergeCell(
                                    topColor = SciiColor.Red,
                                    bottomColor = SciiColor.Transparent,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                ),
                Renderer.MergeType.VBlock to listOf(
                    MutableVBlockCanvas(1, 1).also { canvas ->
                        canvas.mutateVBlock {
                            it.replaceMergeCell(
                                0,
                                0,
                                VBlockMergeCell(
                                    leftColor = SciiColor.Transparent,
                                    rightColor = SciiColor.Yellow,
                                    bright = SciiLight.Transparent,
                                ),
                            )
                        }
                    },
                ),
                Renderer.MergeType.Scii to listOf(
                    MutableQBlockCanvas(1, 1).also { canvas ->
                        canvas.mutate { it.replaceDrawingCell(1, 1, BlockCellMother.Black) }
                    },
                ),
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar(SciiChar.BLOCK_VALUE_FIRST + SciiChar.BLOCK_BIT_BR),
                    ink = SciiColor.Black,
                    paper = SciiColor.Yellow,
                    bright = SciiLight.Off,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    @Test
    fun shouldRender() = performTest(
        arrange = {
            Renderer() to RenderData(
                backgroundLayer = MutableBackgroundLayer(
                    isVisible = true,
                    isLocked = false,
                    border = SciiColor.Black,
                    color = SciiColor.Blue,
                    bright = SciiLight.On,
                ),
                layers = listOf(
                    MutableCanvasLayer(
                        uid = LayerUid("TEST"),
                        isVisible = true,
                        isLocked = false,
                        canvas = MutableSciiCanvas(1, 1).also { canvas ->
                            canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
                        },
                    ),
                ),
                box = Box(0, 0, 1, 1),
            )
        },
        act = { (sut, data) ->
            val destination = MutableSciiCanvas(1, 1)
            sut.render(destination, data.backgroundLayer, data.layers, data.box)
            destination.getSciiCell(0, 0)
        },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar.Space,
                    ink = SciiColor.Blue,
                    paper = SciiColor.Red,
                    bright = SciiLight.On,
                    flash = SciiLight.Off,
                ),
                it,
            )
        },
    )

    private data class RenderData(
        val backgroundLayer: BackgroundLayer,
        val layers: List<CanvasLayer<*>>,
        val box: Box,
    )
}
