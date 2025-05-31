package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

interface Layer {
    val uid: LayerUid
    val isVisible: Boolean
    val isLocked: Boolean

    fun copyMutable(): Layer
}

@BagStuff(unpacker = "_")
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
            character = if (color == SciiColor.Transparent) SciiChar.Transparent else SciiChar.Space,
            ink = color,
            paper = color,
            bright = bright,
            flash = SciiLight.Off,
        )

    override fun copyMutable(): MutableBackgroundLayer
}

@BagStuff(unpacker = "MutableCanvasLayer")
@BagStuffWare(1, field = "uid")
@BagStuffWare(2, field = "isVisible")
@BagStuffWare(3, field = "isLocked")
interface CanvasLayer<T : Cell> : Layer {
    @BagStuffWare(5, version = 2)
    val isMasked: Boolean

    @BagStuffWare(4)
    val canvas: Canvas<T>

    val canvasType: CanvasType

    override fun copyMutable(): MutableCanvasLayer<T>

    fun isOpaque(drawingX: Int, drawingY: Int): Boolean
}
