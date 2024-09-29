package com.eightsines.bpe.presentation

import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.middlware.BpeShape

sealed interface UiAction {
    data class SheetEnter(val pointerX: Int, val pointerY: Int) : UiAction
    data class SheetDown(val pointerX: Int, val pointerY: Int) : UiAction
    data class SheetMove(val pointerX: Int, val pointerY: Int) : UiAction
    data class SheetUp(val pointerX: Int, val pointerY: Int) : UiAction
    data object SheetLeave : UiAction

    data object PaletteColorClick : UiAction
    data object PaletteInkClick : UiAction
    data object PalettePaperClick : UiAction
    data object PaletteBrightClick : UiAction
    data object PaletteFlashClick : UiAction
    data object PaletteCharClick : UiAction

    data object SelectionCutClick : UiAction
    data object SelectionCopyClick : UiAction
    data object LayersClick : UiAction

    data object ToolboxPaintClick : UiAction
    data object ToolboxShapeClick : UiAction
    data object ToolboxEraseClick : UiAction
    data object ToolboxSelectClick : UiAction
    data object ToolboxPickColorClick : UiAction

    data object ToolboxPasteClick : UiAction
    data object ToolboxUndoClick : UiAction
    data object ToolboxRedoClick : UiAction
    data object MenuClick : UiAction

    data class ColorsItemClick(val color: SciiColor) : UiAction
    data class LightsItemClick(val light: SciiLight) : UiAction
    data class CharsItemClick(val character: SciiChar) : UiAction
    data class ShapesItemClick(val shape: BpeShape) : UiAction

    data class LayerItemClick(val layerUid: LayerUid) : UiAction
    data class LayerItemVisibleClick(val layerUid: LayerUid, val isVisible: Boolean) : UiAction
    data class LayerItemLockedClick(val layerUid: LayerUid, val isLocked: Boolean) : UiAction
    data object LayerCreateClick : UiAction
    data object LayerMergeClick : UiAction
    data object LayerConvertClick : UiAction
    data object LayerDeleteClick : UiAction
    data object LayerMoveUpClick : UiAction
    data object LayerMoveDownClick : UiAction
    data class LayerTypeClick(val type: CanvasType) : UiAction
}
