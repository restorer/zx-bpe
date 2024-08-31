package com.eightsines.bpe.presentation

import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.Layer
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class UiRenderer {
    fun renderLayer(layer: Layer, htmlCanvas: HTMLCanvasElement) {
        val htmlContext = htmlCanvas.getContext("2d") as CanvasRenderingContext2D
        htmlContext.clearRect(0.0, 0.0, SCREEN_WIDTH + BORDER_SIZE * 2.0, SCREEN_HEIGHT + BORDER_SIZE * 2.0)

        when (layer) {
            is BackgroundLayer -> {
                if (layer.border != SciiColor.Transparent) {
                    htmlContext.fillStyle = getColor(layer.color, SciiLight.Off)

                    htmlContext.fillRect(0.0, 0.0, SCREEN_WIDTH + BORDER_SIZE * 2.0, BORDER_SIZE)
                    htmlContext.fillRect(0.0, BORDER_SIZE, BORDER_SIZE, SCREEN_HEIGHT)
                    htmlContext.fillRect(SCREEN_WIDTH + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, SCREEN_HEIGHT)
                    htmlContext.fillRect(0.0, SCREEN_HEIGHT + BORDER_SIZE, SCREEN_WIDTH + BORDER_SIZE * 2.0, BORDER_SIZE)
                }

                if (layer.color != SciiColor.Transparent) {
                    htmlContext.fillStyle = getColor(layer.color, layer.bright)
                    htmlContext.fillRect(BORDER_SIZE, BORDER_SIZE, SCREEN_WIDTH, SCREEN_HEIGHT)
                }
            }
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
    }
}
