package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.core.Box

@BagStuff
data class Selection(
    @BagStuffWare(1) val canvasType: CanvasType,
    @BagStuffWare(2) val drawingBox: Box,
) {
    val sciiBox: Box = computeSciiBox()

    fun copyWithOffset(drawingX: Int, drawingY: Int) = copy(drawingBox = drawingBox.copyWithOffset(drawingX, drawingY))
    fun copyWithOffset(point: Pair<Int, Int>) = copy(drawingBox = drawingBox.copyWithOffset(point))

    private fun computeSciiBox(): Box {
        val (sx, sy) = canvasType.toSciiPosition(drawingBox.lx, drawingBox.ly)
        val (ex, ey) = canvasType.toSciiPosition(drawingBox.rx, drawingBox.ry)

        return Box.ofCoords(sx, sy, ex, ey)
    }
}
