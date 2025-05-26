package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireNoIllegalArgumentException

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
