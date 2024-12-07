package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.PackableBag

interface Layer {
    val uid: LayerUid
    val isVisible: Boolean
    val isLocked: Boolean

    fun copyMutable(): Layer
}

interface BackgroundLayer : Layer {
    override val uid: LayerUid
        get() = LayerUid.Background

    val border: SciiColor
    val color: SciiColor
    val bright: SciiLight

    val sciiCell: SciiCell
        get() = SciiCell(
            character = if (color == SciiColor.Transparent) SciiChar.Transparent else SciiChar.Space,
            ink = color,
            paper = color,
            bright = bright,
            flash = SciiLight.Off,
        )

    override fun copyMutable(): MutableBackgroundLayer

    companion object : BagStuffPacker<BackgroundLayer> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BackgroundLayer) {
            bag.put(value.isVisible)
            bag.put(value.isLocked)
            bag.put(value.border.value)
            bag.put(value.color.value)
            bag.put(value.bright.value)
        }
    }
}

interface CanvasLayer<T : Cell> : Layer {
    val isPixelsLocked: Boolean

    val canvas: Canvas<T>
    val canvasType: CanvasType

    override fun copyMutable(): MutableCanvasLayer<T>

    companion object : BagStuffPacker<CanvasLayer<*>> {
        override val putInTheBagVersion = 2

        override fun putInTheBag(bag: PackableBag, value: CanvasLayer<*>) {
            // v1
            bag.put(value.uid.value)
            bag.put(value.isVisible)
            bag.put(value.isLocked)
            bag.put(Canvas, value.canvas)

            // v2
            bag.put(value.isPixelsLocked)
        }
    }
}
