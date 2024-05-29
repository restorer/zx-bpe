package com.eightsines.bpe.engine

data class HistoryStep(val action: HistoryAction, val undoAction: HistoryAction)

sealed interface HistoryAction {
    data class SelectionState(val selectionState: BpeSelectionState) : HistoryAction
    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction
    data class Composite(val actions: List<HistoryAction>) : HistoryAction
}
