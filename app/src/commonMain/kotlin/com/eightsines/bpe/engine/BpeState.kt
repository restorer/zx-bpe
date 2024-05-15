package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.graphics.ShapeType
import com.eightsines.bpe.layer.Layer
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

interface BpeState {
    val borderColor: SciiColor
    val layers: List<Layer>
    val preview: SciiCanvas

    val paletteChar: SciiChar
    val paletteInk: SciiColor
    val palettePaper: SciiColor
    val paletteBright: SciiLight
    val paletteFlash: SciiLight

    val currentTool: BpeTool
    val currentLayerUid: LayerUid
}

sealed interface BpeTool {
    data object Select : BpeTool
    data object Move : BpeTool
    data object ColorPicker : BpeTool
    data class Eraser(val shapeType: ShapeType) : BpeTool
    data class Brush(val shapeType: ShapeType) : BpeTool
}
