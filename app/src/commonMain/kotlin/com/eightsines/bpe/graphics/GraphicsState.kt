package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.SciiCanvas

interface GraphicsState {
    val backgroundLayer: BackgroundLayer
    val canvasLayers: List<CanvasLayer<*>>
    val canvasLayersMap: Map<String, CanvasLayer<*>>
    val preview: SciiCanvas
}
