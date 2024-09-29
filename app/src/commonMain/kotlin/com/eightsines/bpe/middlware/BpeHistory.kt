package com.eightsines.bpe.middlware

import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.foundation.LayerUid

data class HistoryStep(val action: HistoryAction, val undoAction: HistoryAction)

sealed interface HistoryAction {
    data class CurrentLayer(val layerUid: LayerUid) : HistoryAction
    data class SelectionState(val selectionState: BpeSelectionState) : HistoryAction
    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction
    data class Composite(val actions: List<HistoryAction>) : HistoryAction
}
