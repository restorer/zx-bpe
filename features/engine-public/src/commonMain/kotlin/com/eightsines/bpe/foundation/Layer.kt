package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import kotlin.jvm.JvmInline

@JvmInline
value class LayerUid(val value: String) {
    override fun toString() = "LayerUid($value)"

    companion object {
        val Background = LayerUid("")
    }
}

interface Layer {
    val uid: LayerUid
    val isVisible: Boolean
    val isLocked: Boolean
}

@BagStuff(unpacker = "MutableBackgroundLayer_Stuff")
@BagStuffWare(1, field = "isVisible")
@BagStuffWare(2, field = "isLocked")
interface BackgroundLayer : Layer {
    override val uid: LayerUid
        get() = LayerUid.Background

    @BagStuffWare(3)
    val border: SciiColor

    @BagStuffWare(4)
    val color: SciiColor

    @BagStuffWare(5)
    val bright: SciiLight

    val sciiCell: SciiCell
        get() = SciiCell(
            character = if (color.value < 0) SciiChar.Transparent else SciiChar.Space,
            ink = color,
            paper = color,
            bright = bright,
            flash = SciiLight.Off,
        )
}

@BagStuffWare(1, field = "uid")
@BagStuffWare(2, field = "isVisible")
@BagStuffWare(3, field = "isLocked")
interface CanvasLayer<T : Cell> : Layer {
    @BagStuffWare(5, version = 2)
    val isMasked: Boolean

    @BagStuffWare(4)
    val canvas: Canvas<T>

    val canvasType: CanvasType

    fun isOpaque(drawingX: Int, drawingY: Int): Boolean
}
