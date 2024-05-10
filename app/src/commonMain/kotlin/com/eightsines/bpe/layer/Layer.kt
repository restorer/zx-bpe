package com.eightsines.bpe.layer

import com.eightsines.bpe.graphics.Canvas
import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.PackableBag

interface Layer {
    val uid: LayerUid
    val isVisible: Boolean
    val isLocked: Boolean
}

interface BackgroundLayer : Layer {
    override val uid: LayerUid
        get() = LayerUid.Background

    val color: SciiColor
    val bright: SciiLight

    val sciiCell: SciiCell
        get() = SciiCell(
            character = if (color == SciiColor.Transparent) SciiChar.Transparent else SciiChar.Space,
            ink = SciiColor.Transparent,
            paper = color,
            bright = bright,
            flash = SciiLight.Transparent,
        )

    companion object : BagStuffPacker<BackgroundLayer> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BackgroundLayer) {
            bag.put(value.isVisible)
            bag.put(value.isLocked)
            bag.put(value.color.value)
            bag.put(value.bright.value)
        }
    }
}

interface CanvasLayer<T : Cell> : Layer {
    val canvas: Canvas<T>

    fun copyMutable(): MutableCanvasLayer<T>

    companion object : BagStuffPacker<CanvasLayer<*>> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: CanvasLayer<*>) {
            bag.put(value.uid.value)
            bag.put(value.isVisible)
            bag.put(value.isLocked)
            bag.put(Canvas, value.canvas)
        }
    }
}
