package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface Layer {
    val uid: LayerUid
    val isVisible: Boolean
    val isLocked: Boolean
}

interface BackgroundLayer : Layer, BagStuff {
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
}

interface CanvasLayer<T : Cell> : Layer, BagStuff {
    val canvas: Canvas<T>

    fun copyMutable(): MutableCanvasLayer<T>
}
