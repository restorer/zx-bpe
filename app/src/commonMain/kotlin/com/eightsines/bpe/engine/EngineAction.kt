package com.eightsines.bpe.engine

/*
import com.eightsines.bpe.engine.cell.BlockDrawingCell
import com.eightsines.bpe.engine.data.*

sealed interface EngineAction {
    sealed interface Tool : EngineAction {
        data class SetInk(val color: SciiColor) : Tool
        data class SetPaper(val color: SciiColor) : Tool
        data class SetBright(val light: SciiLight) : Tool
        data class SetFlash(val light: SciiLight) : Tool
        data class SetCharacter(val character: SciiChar) : Tool

        data class PickInk(val drawingX: Int, val drawingY: Int) : Tool
        data class PickPaper(val drawingX: Int, val drawingY: Int) : Tool
        data class PickBright(val drawingX: Int, val drawingY: Int) : Tool
        data class PickFlash(val drawingX: Int, val drawingY: Int) : Tool
        data class PickCharacter(val drawingX: Int, val drawingY: Int) : Tool

        data class SelectLayer(val layerUid: String) : Tool
    }

    sealed interface Graphics : EngineAction {
        data class SetBorder(val color: SciiColor) : Graphics
        data class SetBorderVisible(val isVisible: Boolean) : Graphics
        data class SetBackgroundColor(val color: SciiColor) : Graphics
        data class SetBackgroundBright(val light: SciiLight) : Graphics
        data class SetBackgroundVisible(val isVisible: Boolean) : Graphics
        data class CreateNewLayer(val layerType: LayerType, val onTopOfLayerUid: String?) : Graphics
        data class DeleteLayerInternal(val layerUid: String) : Graphics
        data class SetLayerVisible(val layerUid: String, val isVisible: Boolean) : Graphics
        data class SetLayerLocked(val layerUid: String, val isLocked: Boolean) : Graphics
        data class MoveLayer(val layerUid: String, val onTopOfLayerUid: String?) : Graphics

        data class DrawSciiInternal(
            val layerUid: String,
            val drawingX: Int,
            val drawingY: Int,
            val cell: SciiCell,
        ) : Graphics

        data class DrawBlockInternal(
            val layerUid: String,
            val drawingX: Int,
            val drawingY: Int,
            val cell: BlockDrawingCell
        ) : Graphics

        data class UndoSciiInternal(val layerUid: String, val sciiX: Int, val sciiY: Int, val cell: SciiCell) : Graphics
        data class UndoLayerInternal(val layer: ImmutableLayer<*>, val onTopOfLayerUid: String?) : Graphics
    }

    sealed interface Composite : EngineAction {
        data class DeleteLayer(val layerUid: String) : Composite
        data object DeleteSelectedLayer : Composite
        data class SetSelectedLayerVisible(val isVisible: Boolean) : Composite
        data class SetSelectedLayerLocked(val isVisible: Boolean) : Composite
        data class MoveSelectedLayer(val onTopOfLayerUid: String?) : Composite
        data class DrawCurrent(val drawingX: Int, val drawingY: Int) : Composite
    }

    data object Undo : EngineAction
    data object Redo : EngineAction
}
*/
