package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.Crate
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
    val layersCanMoveUp: Boolean,
    val layersCanMoveDown: Boolean,
    val layersCanDelete: Boolean,
    val layersCanMerge: Boolean,
    val layersCanConvert: Boolean,

    val toolboxTool: BpeTool,
    val toolboxShape: BpeShape?,
    val toolboxAvailTools: Set<BpeTool>,
    val toolboxCanSelect: Boolean,
    val toolboxCanPaste: Boolean,
    val toolboxCanUndo: Boolean,
    val toolboxCanRedo: Boolean,

    val selection: Selection?,
    val selectionCanCut: Boolean,
    val selectionCanCopy: Boolean,
    val selectionIsFloating: Boolean,
)

enum class BpeShape {
    Point,
    Line,
    FillBox,
    StrokeBox,
}

enum class BpeTool {
    None,
    Paint,
    Erase,
    Select,
    PickColor,
}

sealed interface BpeSelectionState {
    data object None : BpeSelectionState
    data class Selected(val selection: Selection) : BpeSelectionState

    data class Floating(
        val selection: Selection,
        val offset: Pair<Int, Int>,
        val layerUid: LayerUid,
        val crate: Crate<*>,
        val cutAction: GraphicsAction?,
        val undoCutAction: GraphicsAction?,
        val overlayAction: GraphicsAction,
        val undoOverlayAction: GraphicsAction,
    ) : BpeSelectionState
}

data class BpeClipboard(val drawingX: Int, val drawingY: Int, val crate: Crate<*>)
