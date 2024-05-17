package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

sealed interface BpeAction {
    data class PaletteSetInk(val color: SciiColor) : BpeAction
    data class PaletteSetPaper(val color: SciiColor) : BpeAction
    data class PaletteSetBright(val light: SciiLight) : BpeAction
    data class PaletteSetFlash(val light: SciiLight) : BpeAction
    data class PaletteSetChar(val character: SciiChar) : BpeAction

    data class LayersSetCurrent(val layerUid: LayerUid) : BpeAction
    data class LayersSetVisible(val layerUid: LayerUid, val isVisible: Boolean) : BpeAction
    data class LayersSetLocked(val layerUid: LayerUid, val isLocked: Boolean) : BpeAction
    data object LayersMoveUp : BpeAction
    data object LayersMoveDown : BpeAction
    data class LayersCreate(val canvasType: CanvasType) : BpeAction
    data object LayersDelete : BpeAction
    data object LayersMerge : BpeAction
    data class LayersConvert(val canvasType: CanvasType) : BpeAction

    data class ToolboxSetTool(val tool: BpeTool) : BpeAction
    data class ToolboxSetPaintShape(val shape: BpeShape) : BpeAction
    data class ToolboxSetEraseShape(val shape: BpeShape) : BpeAction
    data object ToolboxPaste : BpeAction
    data object ToolboxUndo : BpeAction
    data object ToolboxRedo : BpeAction

    data object SelectionCut : BpeAction
    data object SelectionCopy : BpeAction
    data object SelectionFloat : BpeAction
    data object SelectionAnchor : BpeAction

    data class CanvasDown(val drawingX: Int, val drawingY: Int) : BpeAction
    data class CanvasUp(val drawingX: Int, val drawingY: Int) : BpeAction
    data object CanvasCancel : BpeAction
}
