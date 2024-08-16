package com.eightsines.bpe.engine

import com.eightsines.bpe.layer.LayerUid

data class HistoryStep(val action: HistoryAction, val undoAction: HistoryAction)

sealed interface HistoryAction {
    data class CurrentLayer(val layerUid: LayerUid) : HistoryAction
    data class SelectionState(val selectionState: BpeSelectionState) : HistoryAction
    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction
    data class Composite(val actions: List<HistoryAction>) : HistoryAction
}
