package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.graphics.GraphicsAction

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
        val layerUid: LayerUid,
        val crate: Crate<Cell>,
        val overlayActions: Pair<GraphicsAction, GraphicsAction>,
    ) : BpeSelectionState
}

data class BpeClipboard(val drawingX: Int, val drawingY: Int, val crate: Crate<*>)
