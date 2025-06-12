package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.Box
import com.eightsines.bpe.foundation.SciiCell
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.MutableBackgroundLayer
import com.eightsines.bpe.foundation.MutableCanvasLayer
import com.eightsines.bpe.foundation.MutableHBlockCanvas
import com.eightsines.bpe.foundation.MutableQBlockCanvas
import com.eightsines.bpe.foundation.MutableSciiCanvas
import com.eightsines.bpe.foundation.MutableVBlockCanvas
import com.eightsines.bpe.testing.BlockCellMother
import com.eightsines.bpe.testing.SciiCellMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RendererTest {
    @Test
    fun shouldMergeCellScii() = performTest(
        arrange = {
            Renderer() to listOf(
                MutableSciiCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.BlockHorizontalTop) }
                },
                MutableSciiCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
                },
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
    fun shouldMergeCellQBlock() = performTest(
        arrange = {
            Renderer() to listOf(
                MutableQBlockCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceDrawingCell(0, 0, BlockCellMother.WhiteBright) }
                },
                MutableQBlockCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceDrawingCell(1, 1, BlockCellMother.Black) }
                },
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
                MutableSciiCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceSciiCell(0, 0, SciiCellMother.RedSpace) }
                },
                MutableHBlockCanvas(1, 1).also { canvas ->
                    canvas.mutate {
                        it.replaceSciiCell(
                            0,
                            0,
                            SciiCell(
                                character = SciiChar.BlockHorizontalTop,
                                ink = SciiColor.Red,
                                paper = SciiColor.Transparent,
                                bright = SciiLight.Transparent,
                                flash = SciiLight.Transparent,
                            ),
                        )
                    }
                },
                MutableVBlockCanvas(1, 1).also { canvas ->
                    canvas.mutate {
                        it.replaceSciiCell(
                            0,
                            0,
                            SciiCell(
                                character = SciiChar.BlockVerticalLeft,
                                ink = SciiColor.Transparent,
                                paper = SciiColor.Yellow,
                                bright = SciiLight.Transparent,
                                flash = SciiLight.Transparent,
                            ),
                        )
                    }
                },
                MutableQBlockCanvas(1, 1).also { canvas ->
                    canvas.mutate { it.replaceDrawingCell(1, 1, BlockCellMother.Black) }
                },
            )
        },
        act = { (sut, groups) -> sut.mergeCell(SciiCellMother.BlueSpace, groups, 0, 0) },
        assert = {
            assertEquals(
                SciiCell(
                    character = SciiChar(SciiChar.BLOCK_VALUE_FIRST + SciiChar.BLOCK_BIT_BR),
                    ink = SciiColor.Black,
                    paper = SciiColor.Red,
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
                box = Box.ofSize(0, 0, 1, 1),
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
