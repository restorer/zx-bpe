package com.eightsines.bpe.middlware

import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair

data class HistoryStep(val actions: List<HistoryAction>, val undoActions: List<HistoryAction>) {
    companion object {
        val Empty = HistoryStep(emptyList(), emptyList())
    }
}

sealed interface HistoryAction {
    data class CurrentLayer(val layerUid: LayerUid) : HistoryAction
    data class SelectionState(val selectionState: BpeSelectionState) : HistoryAction
    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction
}

fun HistoryStep.merge(innerStep: HistoryStep) = HistoryStep(actions + innerStep.actions, innerStep.undoActions + undoActions)

fun GraphicsActionPair?.toHistoryStep() = if (this == null) {
    HistoryStep.Empty
} else {
    HistoryStep(listOf(HistoryAction.Graphics(action)), listOf(HistoryAction.Graphics(undoAction)))
}
