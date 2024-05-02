package com.eightsines.bpe.engine

import com.eightsines.bpe.engine.data.BlockDrawingCell
import com.eightsines.bpe.engine.data.SciiCell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight
import com.eightsines.bpe.engine.layer.LayerType

sealed interface HistoricalAction {
    data class SetBorder(val border: SciiColor) : HistoricalAction
    data class SetBorderVisible(val isVisible: Boolean) : HistoricalAction
    data class SetBackgroundColor(val color: SciiColor) : HistoricalAction
    data class SetBackgroundBright(val bright: SciiLight) : HistoricalAction
    data class SetBackgroundVisible(val isVisible: Boolean) : HistoricalAction
    data class CreateNewLayer(val layerType: LayerType) : HistoricalAction
    data class DeleteLayer(val layerUuid: String) : HistoricalAction
    data class SetLayerVisible(val layerUuid: String, val isVisible: Boolean) : HistoricalAction
    data class SetLayerLocked(val layerUuid: String, val isVisible: Boolean) : HistoricalAction
    data class MoveLayer(val onTopOfLayerUuid: String?) : HistoricalAction
    data class DrawScii(val layerUuid: String, val drawingX: Int, val drawingY: Int, val cell: SciiCell)
    data class DrawBlock(val layerUuid: String, val drawingX: Int, val drawingY: Int, val cell: BlockDrawingCell)
    data class UndoScii(val layerUuid: String, val sciiX: Int, val sciiY: Int, val cell: SciiCell)
}
