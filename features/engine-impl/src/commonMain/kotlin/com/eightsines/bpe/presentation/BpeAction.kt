package com.eightsines.bpe.presentation

import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight

sealed interface BpeAction {
    data class PaletteSetBackgroundBorder(val color: SciiColor) : BpeAction
    data class PaletteSetBackgroundPaper(val color: SciiColor) : BpeAction
    data class PaletteSetBackgroundBright(val light: SciiLight) : BpeAction

    data class PaletteSetPaintSciiInk(val color: SciiColor) : BpeAction
    data class PaletteSetPaintSciiPaper(val color: SciiColor) : BpeAction
    data class PaletteSetPaintSciiBright(val light: SciiLight) : BpeAction
    data class PaletteSetPaintSciiFlash(val light: SciiLight) : BpeAction
    data class PaletteSetPaintSciiChar(val character: SciiChar) : BpeAction
    data class PaletteSetPaintBlockColor(val color: SciiColor) : BpeAction
    data class PaletteSetPaintBlockBright(val light: SciiLight) : BpeAction

    data class PaletteSetEraseSciiInk(val shouldEraseColor: Boolean) : BpeAction
    data class PaletteSetEraseSciiPaper(val shouldEraseColor: Boolean) : BpeAction
    data class PaletteSetEraseSciiBright(val shouldEraseLight: Boolean) : BpeAction
    data class PaletteSetEraseSciiFlash(val shouldEraseLight: Boolean) : BpeAction
    data class PaletteSetEraseSciiChar(val shouldEraseCharacter: Boolean) : BpeAction
    data class PaletteSetEraseBlockColor(val shouldEraseColor: Boolean) : BpeAction
    data class PaletteSetEraseBlockBright(val shouldEraseLight: Boolean) : BpeAction

    data class LayersSetCurrent(val layerUid: LayerUid) : BpeAction
    data class LayersSetVisible(val layerUid: LayerUid, val isVisible: Boolean) : BpeAction
    data class LayersSetLocked(val layerUid: LayerUid, val isLocked: Boolean) : BpeAction
    data class LayersSetMasked(val layerUid: LayerUid, val isMasked: Boolean) : BpeAction
    data object LayersMoveUp : BpeAction
    data object LayersMoveDown : BpeAction
    data class LayersCreate(val canvasType: CanvasType) : BpeAction
    data object LayersDelete : BpeAction
    data object LayersMerge : BpeAction
    data class LayersConvert(val canvasType: CanvasType) : BpeAction

    data class ToolboxSetTool(val tool: BpeTool) : BpeAction
    data class ToolboxSetShape(val shape: BpeShape) : BpeAction
    data object ToolboxPaste : BpeAction
    data object ToolboxUndo : BpeAction
    data object ToolboxRedo : BpeAction

    data object SelectionDeselect : BpeAction
    data object SelectionCut : BpeAction
    data object SelectionCopy : BpeAction
    data object SelectionFlipHorizontal : BpeAction
    data object SelectionFlipVertical : BpeAction
    data object SelectionRotateCw : BpeAction
    data object SelectionRotateCcw : BpeAction
    data object SelectionFill : BpeAction
    data object SelectionClear : BpeAction

    data class CanvasDown(val drawingX: Int, val drawingY: Int) : BpeAction
    data class CanvasMove(val drawingX: Int, val drawingY: Int) : BpeAction
    data class CanvasUp(val drawingX: Int, val drawingY: Int) : BpeAction
    data object CanvasCancel : BpeAction

    data class SetPaintingMode(val mode: BpePaintingMode) : BpeAction
}
