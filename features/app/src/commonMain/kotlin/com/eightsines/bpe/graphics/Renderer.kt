package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.Canvas
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.MutableSciiCanvas

class Renderer {
    fun render(
        destination: MutableSciiCanvas,
        backgroundLayer: BackgroundLayer,
        layers: List<CanvasLayer<*>>,
        box: Box,
    ) {
        val backgroundCell = if (backgroundLayer.isVisible) backgroundLayer.sciiCell else SciiCell.Transparent
        val canvases = layers.mapNotNull { if (it.isVisible) it.canvas else null }

        destination.mutate {
            for (sciiY in box.ly..box.ry) {
                for (sciiX in box.lx..box.rx) {
                    it.replaceSciiCell(sciiX, sciiY, mergeCell(backgroundCell, canvases, sciiX, sciiY))
                }
            }
        }
    }

    internal fun mergeCell(
        backgroundCell: SciiCell,
        canvases: List<Canvas<*>>,
        sciiX: Int,
        sciiY: Int,
    ): SciiCell {
        var result = backgroundCell

        for (canvas in canvases) {
            result = canvas.getSciiCell(sciiX, sciiY).merge(result)
        }

        return result
    }
}
