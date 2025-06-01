package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

interface MutableLayer : Layer

@BagStuff(packer = "BackgroundLayer_Stuff")
class MutableBackgroundLayer(
    @BagStuffWare(1) override var isVisible: Boolean = true,
    @BagStuffWare(2) override var isLocked: Boolean = false,
    @BagStuffWare(3) override var border: SciiColor,
    @BagStuffWare(4) override var color: SciiColor,
    @BagStuffWare(5) override var bright: SciiLight,
) : BackgroundLayer, MutableLayer {
    override fun copyMutable() = MutableBackgroundLayer(
        isVisible = isVisible,
        isLocked = isLocked,
        border = border,
        color = color,
        bright = bright,
    )

    override fun toString() = "BackgroundLayer(isVisible=$isVisible, isLocked=$isLocked, border=$border, color=$color, bright=$bright)"
}

@BagStuff(packer = "CanvasLayer_Stuff")
class MutableCanvasLayer<T : Cell>(
    @BagStuffWare(1) override val uid: LayerUid,
    @BagStuffWare(2) override var isVisible: Boolean = true,
    @BagStuffWare(3) override var isLocked: Boolean = false,
    isMasked: Boolean = false,
    @BagStuffWare(4) override val canvas: MutableCanvas<T>,
) : CanvasLayer<T>, MutableLayer {
    override val canvasType: CanvasType = canvas.type

    @BagStuffWare(5, unpacker = "getMaskedOutOfTheBag", version = 2)
    override var isMasked: Boolean = isMasked
        set(value) {
            field = value
            refreshOpaqueness()
        }

    private val opaqueness = MutableList(canvas.drawingHeight) { MutableList(canvas.drawingWidth) { true } }

    init {
        refreshOpaqueness()
    }

    override fun copyMutable() = MutableCanvasLayer(
        uid = uid,
        isVisible = isVisible,
        isLocked = isLocked,
        isMasked = isMasked,
        canvas = canvas.copyMutable(),
    )

    override fun isOpaque(drawingX: Int, drawingY: Int) =
        if (drawingX >= 0 && drawingX < canvas.drawingWidth && drawingY >= 0 && drawingY < canvas.drawingHeight) {
            opaqueness[drawingY][drawingX]
        } else {
            false
        }

    override fun toString() =
        "CanvasLayer(uid=$uid, isVisible=$isVisible, isLocked=$isLocked, isMasked=$isMasked, canvas=$canvas)"

    private fun refreshOpaqueness() {
        val isOpaque = !isMasked

        for (y in 0..<canvas.drawingHeight) {
            for (x in 0..<canvas.drawingWidth) {
                opaqueness[y][x] = isOpaque || !canvas.getDrawingCell(x, y).isTransparent
            }
        }
    }

    internal companion object {
        @Suppress("NOTHING_TO_INLINE")
        internal inline fun getMaskedOutOfTheBag(version: Int, bag: UnpackableBag): Boolean =
            if (version >= 2) bag.getBoolean() else false
    }
}
