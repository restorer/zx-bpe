package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Selection

data class HistoryStep(val perform: HistoryAction, val undo: HistoryAction)

sealed interface HistoryAction {
    data class Select(val selection: Selection) : HistoryAction
    data object Deselect : HistoryAction

    data class Graphics(val actions: List<GraphicsAction>) : HistoryAction {
        constructor(action: GraphicsAction) : this(listOf(action))
    }
}
