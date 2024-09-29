package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box

data class Selection(
    val canvasType: CanvasType,
    val drawingBox: Box,
) {
    val sciiBox: Box = computeSciiBox()

    fun copyWithOffset(drawingX: Int, drawingY: Int) = copy(drawingBox = drawingBox.copyWithOffset(drawingX, drawingY))
    fun copyWithOffset(point: Pair<Int, Int>) = copy(drawingBox = drawingBox.copyWithOffset(point))

    private fun computeSciiBox(): Box {
        val (sx, sy) = canvasType.toSciiPosition(drawingBox.x, drawingBox.y)
        val (ex, ey) = canvasType.toSciiPosition(drawingBox.ex, drawingBox.ey)

        return Box.of(sx, sy, ex, ey)
    }
}
