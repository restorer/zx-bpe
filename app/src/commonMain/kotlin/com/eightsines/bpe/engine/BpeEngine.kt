package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.Selection

class BpeEngine(
    private val graphicsEngine: GraphicsEngine,
    private val historyMaxSteps: Int = 10000,
) {
    private val clipboard: Crate<*>? = null
    private val history: List<HistoryStep> = emptyList()

    // val revertFloatingAction: GraphicsAction? = null,
}

sealed interface HistoryAction {
    data class Select(val selection: Selection) : HistoryAction
    data object Deselect : HistoryAction
    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction
}

data class HistoryStep(val action: HistoryAction, val undoAction: HistoryAction)
