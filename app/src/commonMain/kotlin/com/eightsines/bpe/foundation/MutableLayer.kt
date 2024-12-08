package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

interface MutableLayer : Layer

class MutableBackgroundLayer(
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var border: SciiColor,
    override var color: SciiColor,
    override var bright: SciiLight,
) : BackgroundLayer, MutableLayer {
    override fun copyMutable() = MutableBackgroundLayer(
        isVisible = isVisible,
        isLocked = isLocked,
        border = border,
        color = color,
        bright = bright,
    )

    override fun toString() = "BackgroundLayer(isVisible=$isVisible, isLocked=$isLocked, border=$border, color=$color, bright=$bright)"

    companion object : BagStuffUnpacker<MutableBackgroundLayer> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableBackgroundLayer {
            requireSupportedStuffVersion("MutableBackgroundLayer", 1, version)

            return MutableBackgroundLayer(
                isVisible = bag.getBoolean(),
                isLocked = bag.getBoolean(),
                border = SciiColor(bag.getInt()),
                color = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
            )
        }
    }
}

class MutableCanvasLayer<T : Cell>(
    override val uid: LayerUid,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    isMasked: Boolean = false,
    override val canvas: MutableCanvas<T>,
) : CanvasLayer<T>, MutableLayer {
    override val canvasType: CanvasType = canvas.type

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

    override fun isOpaque(drawingX: Int, drawingY: Int) = opaqueness[drawingY][drawingX]

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

    companion object : BagStuffUnpacker<MutableCanvasLayer<*>> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableCanvasLayer<*> {
            requireSupportedStuffVersion("MutableBackgroundLayer", 2, version)

            // v1
            val uid = LayerUid(bag.getString())
            val isVisible = bag.getBoolean()
            val isLocked = bag.getBoolean()
            val canvas = bag.getStuff(MutableCanvas)

            val isMasked = if (version >= 2) {
                bag.getBoolean()
            } else {
                false
            }

            return MutableCanvasLayer(
                uid = uid,
                isVisible = isVisible,
                isLocked = isLocked,
                canvas = canvas,
                isMasked = isMasked,
            )
        }
    }
}
