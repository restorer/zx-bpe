package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.engine.canvas.SciiCanvas
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.layer.BackgroundLayer
import com.eightsines.bpe.engine.layer.CanvasLayer

interface GraphicsState {
    val borderColor: SciiColor
    val backgroundLayer: BackgroundLayer
    val canvasLayers: List<CanvasLayer<*>>
    val preview: SciiCanvas
}
