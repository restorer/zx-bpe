package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer

interface GraphicsState {
    val backgroundLayer: BackgroundLayer
    val canvasLayers: List<CanvasLayer<*>>
    val preview: SciiCanvas
}
