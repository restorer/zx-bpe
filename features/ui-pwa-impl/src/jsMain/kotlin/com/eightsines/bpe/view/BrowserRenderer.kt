package com.eightsines.bpe.view

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.BlockCell
import com.eightsines.bpe.foundation.Canvas
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Layer
import com.eightsines.bpe.foundation.SciiCell
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.isTransparent
import com.eightsines.bpe.presentation.UiArea
import com.eightsines.bpe.presentation.UiAreaType
import com.eightsines.bpe.presentation.UiSpec
import com.eightsines.bpe.util.ElapsedTimeProvider
import com.eightsines.bpe.util.Material
import com.eightsines.bpe.util.SpecScii
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class BrowserRenderer(private val elapsedTimeProvider: ElapsedTimeProvider) {
    fun renderPreview(htmlCanvas: HTMLCanvasElement, layer: Layer) {
        val htmlContext = htmlCanvas.getContext("2d", GET_CONTEXT_OPTIONS) as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, PICTURE_WIDTH, PICTURE_HEIGHT)

        @Suppress("UNCHECKED_CAST")
        when (layer) {
            is BackgroundLayer -> {
                renderBackgroundBorder(htmlContext, layer, BACKGROUND_PREVIEW_PICTURE_WIDTH, BACKGROUND_PREVIEW_PICTURE_HEIGHT)
                renderBackgroundPaper(htmlContext, layer, BACKGROUND_PREVIEW_PICTURE_WIDTH, BACKGROUND_PREVIEW_PICTURE_HEIGHT)
            }

            is CanvasLayer<*> -> when (layer.canvas.type) {
                is CanvasType.Scii -> renderSciiCanvas(htmlContext, layer.canvas as Canvas<SciiCell>, 0.0, 0.0)
                is CanvasType.HBlock -> renderHBlockCanvas(htmlContext, layer.canvas as Canvas<BlockCell>, 0.0, 0.0)
                is CanvasType.VBlock -> renderVBlockCanvas(htmlContext, layer.canvas as Canvas<BlockCell>, 0.0, 0.0)
                is CanvasType.QBlock -> renderQBlockCanvas(htmlContext, layer.canvas as Canvas<BlockCell>, 0.0, 0.0)
            }
        }
    }

    fun renderSheet(htmlCanvas: HTMLCanvasElement, backgroundLayer: BackgroundLayer, canvas: Canvas<SciiCell>) {
        val htmlContext = htmlCanvas.getContext("2d", GET_CONTEXT_OPTIONS) as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, FULL_WIDTH, FULL_HEIGHT)

        if (backgroundLayer.isVisible && !backgroundLayer.border.isTransparent) {
            renderBackgroundBorder(htmlContext, backgroundLayer, PICTURE_WIDTH, PICTURE_HEIGHT)
        } else {
            renderTransparent(htmlContext, TRANSPARENT_COLORS_BORDER, 0.0, 0.0, FULL_WIDTH, BORDER_SIZE)
            renderTransparent(htmlContext, TRANSPARENT_COLORS_BORDER, 0.0, BORDER_SIZE, BORDER_SIZE, PICTURE_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_COLORS_BORDER, PICTURE_WIDTH + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, PICTURE_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_COLORS_BORDER, 0.0, PICTURE_HEIGHT + BORDER_SIZE, FULL_WIDTH, BORDER_SIZE)
        }

        if (!backgroundLayer.isVisible || backgroundLayer.color.isTransparent) {
            renderTransparent(htmlContext, TRANSPARENT_COLORS_SCREEN, BORDER_SIZE, BORDER_SIZE, PICTURE_WIDTH, PICTURE_HEIGHT)
        }

        renderSciiCanvas(htmlContext, canvas, BORDER_SIZE, BORDER_SIZE)
    }

    fun renderAreas(htmlCanvas: HTMLCanvasElement, areas: List<UiArea>) {
        val htmlContext = htmlCanvas.getContext("2d", GET_CONTEXT_OPTIONS) as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, FULL_WIDTH, FULL_HEIGHT)

        for (area in areas) {
            AREA_COLORS[area.type]?.let { renderSingleArea(htmlContext, area, it) }
        }
    }

    private fun renderSingleArea(htmlContext: CanvasRenderingContext2D, area: UiArea, colors: List<String>) {
        val dash = if (area.pointerWidth <= AREA_DASH_MD && area.pointerHeight <= AREA_DASH_MD) {
            AREA_DASH_XS
        } else {
            AREA_DASH_MD
        }

        val sx = area.pointerX
        val sy = area.pointerY
        val ex = sx + area.pointerWidth
        val ey = sy + area.pointerHeight

        var x = sx
        var y = sy
        var colorIndex = 0

        while (x < ex) {
            htmlContext.fillStyle = colors[colorIndex]
            htmlContext.fillRect(x.toDouble(), y.toDouble(), minOf(dash, ex - x).toDouble(), 1.0)

            x += dash
            colorIndex = 1 - colorIndex
        }

        x = ex - 1
        colorIndex = 1 - colorIndex

        while (y < ey) {
            htmlContext.fillStyle = colors[colorIndex]
            htmlContext.fillRect(x.toDouble(), y.toDouble(), 1.0, minOf(dash, ey - y).toDouble())

            y += dash
            colorIndex = 1 - colorIndex
        }

        y = ey - 1
        x = ex
        colorIndex = 1 - colorIndex

        while (x > sx) {
            val tx = maxOf(sx, x - dash)

            htmlContext.fillStyle = colors[colorIndex]
            htmlContext.fillRect(tx.toDouble(), y.toDouble(), minOf(dash, x - tx).toDouble(), 1.0)

            x -= dash
            colorIndex = 1 - colorIndex
        }

        x = sx
        y = ey
        colorIndex = 1 - colorIndex

        while (y > sy) {
            val ty = maxOf(sy, y - dash)

            htmlContext.fillStyle = colors[colorIndex]
            htmlContext.fillRect(x.toDouble(), ty.toDouble(), 1.0, minOf(dash, y - ty).toDouble())

            y -= dash
            colorIndex = 1 - colorIndex
        }
    }

    private fun renderSciiCanvas(htmlContext: CanvasRenderingContext2D, canvas: Canvas<SciiCell>, top: Double, left: Double) {
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
    private inline fun renderHBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: Canvas<BlockCell>, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 8.0, 4.0)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun renderVBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: Canvas<BlockCell>, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 4.0, 8.0)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun renderQBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: Canvas<BlockCell>, top: Double, left: Double) =
        renderBlockCanvas(htmlContext, canvas, top, left, 4.0, 4.0)

    private fun renderBlockCanvas(
        htmlContext: CanvasRenderingContext2D,
        canvas: Canvas<BlockCell>,
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
                    htmlContext.fillStyle = color.value

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

    private fun renderBackgroundBorder(htmlContext: CanvasRenderingContext2D, layer: BackgroundLayer, fullWidth: Double, fullHeight: Double) {
        val borderColor = getColor(layer.border, SciiLight.Off)

        if (borderColor != null) {
            htmlContext.fillStyle = borderColor.value

            htmlContext.fillRect(0.0, 0.0, fullWidth + BORDER_SIZE * 2.0, BORDER_SIZE)
            htmlContext.fillRect(0.0, BORDER_SIZE, BORDER_SIZE, fullHeight)
            htmlContext.fillRect(fullWidth + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, fullHeight)
            htmlContext.fillRect(0.0, fullHeight + BORDER_SIZE, fullWidth + BORDER_SIZE * 2.0, BORDER_SIZE)
        }
    }

    private fun renderBackgroundPaper(htmlContext: CanvasRenderingContext2D, layer: BackgroundLayer, fullWidth: Double, fullHeight: Double) {
        val paperColor = getColor(layer.color, layer.bright)

        if (paperColor != null) {
            htmlContext.fillStyle = paperColor.value
            htmlContext.fillRect(BORDER_SIZE, BORDER_SIZE, fullWidth, fullHeight)
        }
    }

    private fun renderSciiCell(pixelsData: Uint8ClampedArray, elapsedTimeMs: Long, x: Int, y: Int, cell: SciiCell) {
        var inkColor = getColor(cell.ink, cell.bright)
        var paperColor = getColor(cell.paper, cell.bright)

        if (inkColor == null && paperColor == null) {
            return
        }

        val charValue = cell.character.value - SciiChar.Space.value

        if (charValue < 0) {
            return
        }

        val charRow = charValue / SPECSCII_CHAR_COLS

        if (charRow >= SPECSCII_CHAR_ROWS) {
            return
        }

        if (cell.flash == SciiLight.On && elapsedTimeMs % FLASH_CYCLE_MS > FLASH_MS) {
            val tmpColor = inkColor
            inkColor = paperColor
            paperColor = tmpColor
        }

        val charX = (charValue % SPECSCII_CHAR_COLS) * UiSpec.SCII_CELL_SIZE
        val charY = charRow * UiSpec.SCII_CELL_SIZE

        val pixelsDataDyn = pixelsData.asDynamic()
        val charsData = SpecScii.DATA

        for (row in 0..<UiSpec.SCII_CELL_SIZE) {
            var pixelsIndex = ((y + row) * UiSpec.PICTURE_WIDTH + x) * 4
            var charsIndex = (charY + row) * SpecScii.WIDTH + charX

            @Suppress("Unused")
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
        color.isTransparent -> null
        light == SciiLight.On -> COLORS[color.value + 8]
        else -> COLORS[color.value]
    }

    @OptIn(ExperimentalStdlibApi::class)
    private data class UiColor(val r: Int, val g: Int, val b: Int) {
        val value = "#${r.toByte().toHexString()}${g.toByte().toHexString()}${b.toByte().toHexString()}"
    }

    companion object {
        private val COLOR_HEX_SHORT_REGEX = Regex("^#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])$")
        private val COLOR_HEX_LONG_REGEX = Regex("^#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$")
        private val COLOR_RGB_REGEX = Regex("^rgb\\(([0-9.]+),([0-9.]+),([0-9.]+)\\)$")
        private val COLOR_RGBA_REGEX = Regex("^rgba\\(([0-9.]+),([0-9.]+),([0-9]+),[0-9.]+\\)$")

        private const val PICTURE_WIDTH = UiSpec.PICTURE_WIDTH.toDouble()
        private const val PICTURE_HEIGHT = UiSpec.PICTURE_HEIGHT.toDouble()
        private const val BORDER_SIZE = UiSpec.BORDER_SIZE.toDouble()
        private const val FULL_WIDTH = UiSpec.FULL_WIDTH.toDouble()
        private const val FULL_HEIGHT = UiSpec.FULL_HEIGHT.toDouble()

        private const val SPECSCII_CHAR_ROWS = SpecScii.HEIGHT / UiSpec.SCII_CELL_SIZE
        private const val SPECSCII_CHAR_COLS = SpecScii.WIDTH / UiSpec.SCII_CELL_SIZE

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
        private val TRANSPARENT_COLORS_SCREEN = listOf(Material.Gray700, Material.Gray500)
        private val TRANSPARENT_COLORS_BORDER = listOf(Material.Gray800, Material.Gray600)

        private val AREA_COLORS = buildMap {
            put(UiAreaType.Selection, listOf(Material.Amber900, Material.Amber50))
            put(UiAreaType.SecondaryCursor, listOf(rgba(Material.BlueGray800, 0.5), rgba(Material.BlueGray100, 0.5)))
            put(UiAreaType.PrimaryCursor, listOf(Material.BlueGray900, Material.BlueGray50))
        }

        private const val AREA_DASH_XS = 2
        private const val AREA_DASH_MD = 4

        const val FLASH_MS = 16000 / 50
        private const val FLASH_CYCLE_MS = FLASH_MS * 2

        private val GET_CONTEXT_OPTIONS: dynamic = object {}
            .also { it.asDynamic()["willReadFrequently"] = true }

        private fun rgba(color: String, a: Double): String {
            val matchShort = COLOR_HEX_SHORT_REGEX.matchEntire(color)

            if (matchShort != null) {
                val r = matchShort.groupValues[1].toInt(16)
                val g = matchShort.groupValues[2].toInt(16)
                val b = matchShort.groupValues[3].toInt(16)

                return "rgba(${r + 16 * r},${g + 16 * g},${b + 16 * b},$a)"
            }

            val matchLong = COLOR_HEX_LONG_REGEX.matchEntire(color)

            if (matchLong != null) {
                val r = matchLong.groupValues[1].toInt(16)
                val g = matchLong.groupValues[2].toInt(16)
                val b = matchLong.groupValues[3].toInt(16)

                return "rgba($r,$g,$b,$a)"
            }

            val matchRgb = COLOR_RGB_REGEX.matchEntire(color)

            if (matchRgb != null) {
                val r = matchRgb.groupValues[1].toDouble()
                val g = matchRgb.groupValues[2].toDouble()
                val b = matchRgb.groupValues[3].toDouble()

                return "rgba($r,$g,$b,$a)"
            }

            val matchRgba = COLOR_RGBA_REGEX.matchEntire(color)

            if (matchRgba != null) {
                val r = matchRgba.groupValues[1].toDouble()
                val g = matchRgba.groupValues[2].toDouble()
                val b = matchRgba.groupValues[3].toDouble()

                return "rgba($r,$g,$b,$a)"
            }

            return color
        }
    }
}
