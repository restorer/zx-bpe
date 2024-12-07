package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

interface MutableLayer : Layer

data class MutableBackgroundLayer(
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

data class MutableCanvasLayer<T : Cell>(
    override val uid: LayerUid,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    override var isPixelsLocked: Boolean = false,
    override val canvas: MutableCanvas<T>,
) : CanvasLayer<T>, MutableLayer {
    override val canvasType: CanvasType = canvas.type

    override fun copyMutable() = MutableCanvasLayer(
        uid = uid,
        isVisible = isVisible,
        isLocked = isLocked,
        isPixelsLocked = isPixelsLocked,
        canvas = canvas.copyMutable(),
    )

    override fun toString() =
        "CanvasLayer(uid=$uid, isVisible=$isVisible, isLocked=$isLocked, isPixelsLocked=$isPixelsLocked, canvas=$canvas)"

    companion object : BagStuffUnpacker<MutableCanvasLayer<*>> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableCanvasLayer<*> {
            requireSupportedStuffVersion("MutableBackgroundLayer", 2, version)

            // v1
            val uid = LayerUid(bag.getString())
            val isVisible = bag.getBoolean()
            val isLocked = bag.getBoolean()
            val canvas = bag.getStuff(MutableCanvas)

            val isPixelsLocked = if (version >= 2) {
                bag.getBoolean()
            } else {
                false
            }

            return MutableCanvasLayer(
                uid = uid,
                isVisible = isVisible,
                isLocked = isLocked,
                canvas = canvas,
                isPixelsLocked = isPixelsLocked,
            )
        }
    }
}
