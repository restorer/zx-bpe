package com.eightsines.bpe.presentation

import com.eightsines.bpe.graphics.HBlockCanvas
import com.eightsines.bpe.graphics.QBlockCanvas
import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.graphics.VBlockCanvas
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.Layer
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class UiRenderer {
    fun renderPreview(htmlCanvas: HTMLCanvasElement, layer: Layer) {
        val htmlContext = htmlCanvas.getContext("2d") as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, SCREEN_WIDTH, SCREEN_HEIGHT)

        when (layer) {
            is BackgroundLayer -> renderBackground(htmlContext, layer, SCREEN_WIDTH - BORDER_SIZE * 2.0, SCREEN_HEIGHT - BORDER_SIZE * 2.0)

            is CanvasLayer<*> -> when (val canvas = layer.canvas) {
                is SciiCanvas -> renderSciiCanvas(htmlContext, canvas, 0.0, 0.0)
                is HBlockCanvas -> renderHBlockCanvas(htmlContext, canvas, 0.0, 0.0)
                is VBlockCanvas -> renderVBlockCanvas(htmlContext, canvas, 0.0, 0.0)
                is QBlockCanvas -> renderQBlockCanvas(htmlContext, canvas, 0.0, 0.0)
            }
        }
    }

    fun renderSheet(htmlCanvas: HTMLCanvasElement, backgroundLayer: BackgroundLayer, canvas: SciiCanvas) {
        val htmlContext = htmlCanvas.getContext("2d") as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, SCREEN_WIDTH + BORDER_SIZE * 2.0, SCREEN_HEIGHT + BORDER_SIZE * 2.0)

        if (backgroundLayer.border == SciiColor.Transparent) {
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, 0.0, SCREEN_WIDTH + BORDER_SIZE * 2.0, BORDER_SIZE)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, BORDER_SIZE, BORDER_SIZE, SCREEN_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, SCREEN_WIDTH + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, SCREEN_HEIGHT)
            renderTransparent(htmlContext, TRANSPARENT_BORDER, 0.0, SCREEN_HEIGHT + BORDER_SIZE, SCREEN_WIDTH + BORDER_SIZE * 2.0, BORDER_SIZE)
        }

        if (backgroundLayer.color == SciiColor.Transparent) {
            renderTransparent(htmlContext, TRANSPARENT_SCREEN, BORDER_SIZE, BORDER_SIZE, SCREEN_WIDTH, SCREEN_HEIGHT)
        }

        renderBackground(htmlContext, backgroundLayer, SCREEN_WIDTH, SCREEN_HEIGHT)
        renderSciiCanvas(htmlContext, canvas, BORDER_SIZE, BORDER_SIZE)
    }

    private fun renderSciiCanvas(htmlContext: CanvasRenderingContext2D, canvas: SciiCanvas, top: Double, left: Double) {
    }

    private fun renderHBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: HBlockCanvas, top: Double, left: Double) {
    }

    private fun renderVBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: VBlockCanvas, top: Double, left: Double) {
    }

    private fun renderQBlockCanvas(htmlContext: CanvasRenderingContext2D, canvas: QBlockCanvas, top: Double, left: Double) {
    }

    private fun renderBackground(htmlContext: CanvasRenderingContext2D, layer: BackgroundLayer, width: Double, height: Double) {
        if (layer.border != SciiColor.Transparent) {
            htmlContext.fillStyle = getColor(layer.border, SciiLight.Off)

            htmlContext.fillRect(0.0, 0.0, width + BORDER_SIZE * 2.0, BORDER_SIZE)
            htmlContext.fillRect(0.0, BORDER_SIZE, BORDER_SIZE, height)
            htmlContext.fillRect(width + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, height)
            htmlContext.fillRect(0.0, height + BORDER_SIZE, width + BORDER_SIZE * 2.0, BORDER_SIZE)
        }

        if (layer.color != SciiColor.Transparent) {
            htmlContext.fillStyle = getColor(layer.color, layer.bright)
            htmlContext.fillRect(BORDER_SIZE, BORDER_SIZE, width, height)
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
        color == SciiColor.Transparent -> "transparent"
        light == SciiLight.On -> COLORS[color.value + 8]
        else -> COLORS[color.value]
    }

    private companion object {
        private const val SCREEN_WIDTH = 256.0
        private const val SCREEN_HEIGHT = 192.0
        private const val BORDER_SIZE = 32.0

        private val COLORS = listOf(
            "#000000",
            "#0000c0",
            "#c00000",
            "#c000c0",
            "#00c000",
            "#00c0c0",
            "#c0c000",
            "#c0c0c0",
            "#000000",
            "#0000ff",
            "#ff0000",
            "#ff00ff",
            "#00ff00",
            "#00ffff",
            "#ffff00",
            "#ffffff",
        )

        private val TRANSPARENT_SIZE = 4.0
        private val TRANSPARENT_SCREEN = listOf("#9e9e9e", "#e0e0e0")
        private val TRANSPARENT_BORDER = listOf("#757575", "#bdbdbd")
    }
}
