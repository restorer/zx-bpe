package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.graphics.Selection
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.state.CanvasView
import com.eightsines.bpe.state.LayerView

data class BpeState(
    val layers: List<LayerView<*>>,
    val preview: CanvasView<SciiCanvas>,
    val borderColor: SciiColor,

    val paletteInk: SciiColor = SciiColor.White,
    val palettePaper: SciiColor = SciiColor.Transparent,
    val paletteBright: SciiLight = SciiLight.Transparent,
    val paletteFlash: SciiLight = SciiLight.Transparent,
    val paletteChar: SciiChar = SciiChar.Copyright,

    val paintShape: BpeShape = BpeShape.Point,
    val eraseShape: BpeShape = BpeShape.Point,

    val currentTool: BpeTool = BpeTool.Paint,
    val currentLayerUid: LayerUid = LayerUid.Background,

    val selection: Selection? = null,
    val clipboard: Crate<*>? = null,
    val isInFloatingMode: Boolean = false,

    val canUndo: Boolean = false,
    val canRedo: Boolean = false,

    // val history: List<BpeStep> = emptyList(),
    // val revertFloatingAction: GraphicsAction? = null,
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

data class BpeStep(val action: GraphicsAction, val undoAction: GraphicsAction)
