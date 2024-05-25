package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.graphics.Selection
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.state.CanvasView
import com.eightsines.bpe.state.LayerView

data class BpeState(
    val background: LayerView<BackgroundLayer>,
    val canvas: CanvasView<SciiCanvas>,
    val drawingType: CanvasType?,

    val paletteInk: SciiColor,
    val palettePaper: SciiColor?,
    val paletteBright: SciiLight?,
    val paletteFlash: SciiLight?,
    val paletteChar: SciiChar?,

    val layers: List<LayerView<*>>,
    val layersCurrentUid: LayerUid,

    val toolboxTool: BpeTool?,
    val toolboxShape: BpeShape?,
    val toolboxCanSelect: Boolean,
    val toolboxCanPaste: Boolean,
    val toolboxCanUndo: Boolean,
    val toolboxCanRedo: Boolean,

    val selection: Selection?,
    val selectionCanCut: Boolean,
    val selectionCanCopy: Boolean,
    val selectionCanFloat: Boolean,
    val selectionCanAnchor: Boolean,
    val selectionIsFloating: Boolean,
)

enum class BpeShape {
    Point,
    Line,
    FillBox,
    StrokeBox,
}

enum class BpeTool {
    Paint,
    Erase,
    Select,
    PickColor,
}
