package com.eightsines.bpe.presentation

import com.eightsines.bpe.data.SpecSciiData
import com.eightsines.bpe.graphics.BlockCanvas
import com.eightsines.bpe.graphics.HBlockCanvas
import com.eightsines.bpe.graphics.QBlockCanvas
import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.graphics.VBlockCanvas
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.Layer
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.util.ElapsedTimeProvider
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class UiRenderer(private val elapsedTimeProvider: ElapsedTimeProvider) {
    private val specSciiCharRows = SpecSciiData.HEIGHT / UiSpec.SCII_CELL_SIZE
    private val specSciiCharColumns = SpecSciiData.WIDTH / UiSpec.SCII_CELL_SIZE

    fun renderPreview(htmlCanvas: HTMLCanvasElement, layer: Layer) {
        val htmlContext = htmlCanvas.getContext("2d", GET_CONTEXT_OPTIONS) as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, PICTURE_WIDTH, PICTURE_HEIGHT)

        when (layer) {
            is BackgroundLayer -> renderBackground(htmlContext, layer, BACKGROUND_PREVIEW_PICTURE_WIDTH, BACKGROUND_PREVIEW_PICTURE_HEIGHT)

            is CanvasLayer<*> -> when (val canvas = layer.canvas) {
                is SciiCanvas -> renderSciiCanvas(htmlContext, canvas, 0.0, 0.0)
                is HBlockCanvas -> renderHBlockCanvas(htmlContext, canvas, 0.0, 0.0)
                is VBlockCanvas -> renderVBlockCanvas(htmlContext, canvas, 0.0, 0.0)
                is QBlockCanvas -> renderQBlockCanvas(htmlContext, canvas, 0.0, 0.0)
            }
        }
    }

    fun renderSheet(htmlCanvas: HTMLCanvasElement, backgroundLayer: BackgroundLayer, canvas: SciiCanvas) {
        val htmlContext = htmlCanvas.getContext("2d", GET_CONTEXT_OPTIONS) as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, FULL_WIDTH, FULL_HEIGHT)

        if (backgroundLayer.border == SciiColor.Transparent) {
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, 0.0, FULL_WIDTH, BORDER_SIZE)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, BORDER_SIZE, BORDER_SIZE, PICTURE_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, PICTURE_WIDTH + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, PICTURE_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, PICTURE_HEIGHT + BORDER_SIZE, FULL_WIDTH, BORDER_SIZE)
        }

        if (backgroundLayer.color == SciiColor.Transparent) {
            renderTransparent(htmlContext, TRANSPARENT_SCREEN, BORDER_SIZE, BORDER_SIZE, PICTURE_WIDTH, PICTURE_HEIGHT)
        }

        renderBackground(htmlContext, backgroundLayer, PICTURE_WIDTH, PICTURE_HEIGHT)
        renderSciiCanvas(htmlContext, canvas, BORDER_SIZE, BORDER_SIZE)
    }

    private fun renderSciiCanvas(htmlContext: CanvasRenderingContext2D, canvas: SciiCanvas, top: Double, left: Double) {
        val imageData = htmlContext.getImageData(top, left, PICTURE_WIDTH, PICTURE_HEIGHT)
        val elapsedTimeMs = elapsedTimeProvider.getElapsedTimeMs()

        for (sciiY in 0..<canvas.sciiHeight) {
            for (sciiX in 0..<canvas.sciiWidth) {
                renderSciiCell(
                    imageData.data,
                    elapsedTimeMs,
                    sciiX * UiSpec.SCII_CELL_SIZE,
                    sciiY * UiSpec.SCII_CELL_SIZE,
                    canvas.getSciiCell(sciiX, sciiY),
                )
            }
        }

        htmlContext.putImageData(imageData, top, left)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun renderHBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: HBlockCanvas, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 8.0, 4.0)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun renderVBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: VBlockCanvas, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 4.0, 8.0)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun renderQBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: QBlockCanvas, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 4.0, 4.0)

    private fun renderBlockCanvas(
        htmlContext: CanvasRenderingContext2D,
        canvas: BlockCanvas,
        top: Double,
        left: Double,
        blockWidth: Double,
        blockHeight: Double,
    ) {
        for (drawingY in 0..<canvas.drawingHeight) {
            for (drawingX in 0..<canvas.drawingWidth) {
                val cell = canvas.getDrawingCell(drawingX, drawingY)
                val color = getColor(cell.color, cell.bright)

                if (color != null) {
                    htmlContext.fillStyle = color

                    htmlContext.fillRect(
                        left + drawingX.toDouble() * blockWidth,
                        top + drawingY.toDouble() * blockHeight,
                        blockWidth,
                        blockHeight,
                    )
                }
            }
        }
    }

    private fun renderBackground(htmlContext: CanvasRenderingContext2D, layer: BackgroundLayer, fullWidth: Double, fullHeight: Double) {
        val borderColor = getColor(layer.border, SciiLight.Off)
        val paperColor = getColor(layer.color, layer.bright)

        if (borderColor != null) {
            htmlContext.fillStyle = borderColor.value

            htmlContext.fillRect(0.0, 0.0, fullWidth + BORDER_SIZE * 2.0, BORDER_SIZE)
            htmlContext.fillRect(0.0, BORDER_SIZE, BORDER_SIZE, fullHeight)
            htmlContext.fillRect(fullWidth + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, fullHeight)
            htmlContext.fillRect(0.0, fullHeight + BORDER_SIZE, fullWidth + BORDER_SIZE * 2.0, BORDER_SIZE)
        }

        if (paperColor != null) {
            htmlContext.fillStyle = paperColor.value
            htmlContext.fillRect(BORDER_SIZE, BORDER_SIZE, fullWidth, fullHeight)
        }
    }

    private fun renderSciiCell(pixelsData: Uint8ClampedArray, elapsedTimeMs: Int, x: Int, y: Int, cell: SciiCell) {
        var inkColor = getColor(cell.ink, cell.bright)
        var paperColor = getColor(cell.paper, cell.bright)

        if (inkColor == null && paperColor == null) {
            return
        }

        val charValue = cell.character.value - SciiChar.Space.value

        if (charValue < 0) {
            return
        }

        val charRow = charValue / specSciiCharColumns

        if (charRow >= specSciiCharRows) {
            return
        }

        if (elapsedTimeMs % FLASH_FULL_MS > FLASH_MS) {
            val tmpColor = inkColor
            inkColor = paperColor
            paperColor = tmpColor
        }

        val charX = (charValue % specSciiCharColumns) * UiSpec.SCII_CELL_SIZE
        val charY = charRow * UiSpec.SCII_CELL_SIZE

        val pixelsDataDyn = pixelsData.asDynamic()
        val charsData = SpecSciiData.DATA

        for (row in 0..<UiSpec.SCII_CELL_SIZE) {
            var pixelsIndex = ((y + row) * UiSpec.PICTURE_WIDTH + x) * 4
            var charsIndex = (charY + row) * SpecSciiData.WIDTH + charX

            for (col in 0..<UiSpec.SCII_CELL_SIZE) {
                val color = if (charsData[charsIndex] == 1) inkColor else paperColor

                if (color != null) {
                    pixelsDataDyn[pixelsIndex] = color.r
                    pixelsDataDyn[pixelsIndex + 1] = color.g
                    pixelsDataDyn[pixelsIndex + 2] = color.b
                    pixelsDataDyn[pixelsIndex + 3] = 255
                }

                pixelsIndex += 4
                ++charsIndex
            }
        }
    }

    private fun renderTransparent(
        htmlContext: CanvasRenderingContext2D,
        colors: List<String>,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
    ) {
        val ex = x + width
        val ey = y + height

        var sy = y
        var ty = 0

        while (sy < ey) {
            var sx = x
            var tx = 1

            while (sx < ex) {
                htmlContext.fillStyle = colors[(ty + tx) % 2]
                htmlContext.fillRect(sx, sy, TRANSPARENT_SIZE, TRANSPARENT_SIZE)

                sx += TRANSPARENT_SIZE
                tx = 1 - tx
            }

            sy += TRANSPARENT_SIZE
            ty = 1 - ty
        }
    }

    private fun getColor(color: SciiColor, light: SciiLight) = when {
        color == SciiColor.Transparent -> null
        light == SciiLight.On -> COLORS[color.value + 8]
        else -> COLORS[color.value]
    }

    @OptIn(ExperimentalStdlibApi::class)
    private data class UiColor(val r: Int, val g: Int, val b: Int) {
        val value = "#${r.toByte().toHexString()}${g.toByte().toHexString()}${b.toByte().toHexString()}"
    }

    private companion object {
        private const val PICTURE_WIDTH = UiSpec.PICTURE_WIDTH.toDouble()
        private const val PICTURE_HEIGHT = UiSpec.PICTURE_HEIGHT.toDouble()
        private const val BORDER_SIZE = UiSpec.BORDER_SIZE.toDouble()
        private const val FULL_WIDTH = UiSpec.FULL_WIDTH.toDouble()
        private const val FULL_HEIGHT = UiSpec.FULL_HEIGHT.toDouble()

        private const val BACKGROUND_PREVIEW_PICTURE_WIDTH = (UiSpec.PICTURE_WIDTH - UiSpec.BORDER_SIZE * 2).toDouble()
        private const val BACKGROUND_PREVIEW_PICTURE_HEIGHT = (UiSpec.PICTURE_HEIGHT - UiSpec.BORDER_SIZE * 2).toDouble()

        private val COLORS = listOf(
            UiColor(0, 0, 0),
            UiColor(0, 0, 192),
            UiColor(192, 0, 0),
            UiColor(192, 0, 192),
            UiColor(0, 192, 0),
            UiColor(0, 192, 192),
            UiColor(192, 192, 0),
            UiColor(192, 192, 192),
            UiColor(0, 0, 0),
            UiColor(0, 0, 255),
            UiColor(255, 0, 0),
            UiColor(255, 0, 255),
            UiColor(0, 255, 0),
            UiColor(0, 255, 255),
            UiColor(255, 255, 0),
            UiColor(255, 255, 255),
        )

        private const val TRANSPARENT_SIZE = UiSpec.BLOCK_CELL_SIZE.toDouble()
        private val TRANSPARENT_SCREEN = listOf("#9e9e9e", "#e0e0e0")
        private val TRANSPARENT_BORDER = listOf("#757575", "#bdbdbd")

        private const val FLASH_MS = 16000 / 50
        private const val FLASH_FULL_MS = 16000 / 50

        private val GET_CONTEXT_OPTIONS: dynamic = object {}
            .apply { asDynamic()["willReadFrequently"] = true }
    }
}
