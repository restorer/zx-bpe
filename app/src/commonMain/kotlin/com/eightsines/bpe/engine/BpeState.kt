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
    val drawingType: CanvasType,

    val paletteInk: SciiColor = SciiColor.Transparent,
    val palettePaper: SciiColor? = null,
    val paletteBright: SciiLight? = null,
    val paletteFlash: SciiLight? = null,
    val paletteChar: SciiChar? = null,

    val layers: List<LayerView<*>>,
    val layersCurrentUid: LayerUid = LayerUid.Background,

    val toolboxTool: BpeTool? = null,
    val toolboxPaintShape: BpeShape? = null,
    val toolboxEraseShape: BpeShape? = null,
    val toolboxCanSelect: Boolean = false,
    val toolboxCanPaste: Boolean = false,
    val toolboxCanUndo: Boolean = false,
    val toolboxCanRedo: Boolean = false,

    val selection: Selection? = null,
    val selectionCanCut: Boolean = false,
    val selectionCanCopy: Boolean = false,
    val selectionCanFloat: Boolean = false,
    val selectionCanAnchor: Boolean = false,
    val selectionIsFloating: Boolean = false,
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
