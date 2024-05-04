package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface Layer {
    val uid: String
    val isVisible: Boolean
    val isLocked: Boolean
}

interface BackgroundLayer : Layer {
    val color: SciiColor
    val bright: SciiLight
}

interface CanvasLayer<T : Cell> : Layer {
    val canvas: Canvas<T>
}
