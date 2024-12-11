package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireNoIllegalArgumentException

data class Selection(
    val canvasType: CanvasType,
    val drawingBox: Box,
) {
    val sciiBox: Box = computeSciiBox()

    fun copyWithOffset(drawingX: Int, drawingY: Int) = copy(drawingBox = drawingBox.copyWithOffset(drawingX, drawingY))
    fun copyWithOffset(point: Pair<Int, Int>) = copy(drawingBox = drawingBox.copyWithOffset(point))

    private fun computeSciiBox(): Box {
        val (sx, sy) = canvasType.toSciiPosition(drawingBox.lx, drawingBox.ly)
        val (ex, ey) = canvasType.toSciiPosition(drawingBox.rx, drawingBox.ry)

        return Box.ofCoords(sx, sy, ex, ey)
    }

    companion object : BagStuffPacker<Selection>, BagStuffUnpacker<Selection> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Selection) {
            bag.put(value.canvasType.value)
            bag.put(Box, value.drawingBox)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Selection {
            val canvasType = requireNoIllegalArgumentException { CanvasType.of(bag.getInt()) }
            val drawingBox = bag.getStuff(Box)

            return Selection(canvasType, drawingBox)
        }
    }
}
