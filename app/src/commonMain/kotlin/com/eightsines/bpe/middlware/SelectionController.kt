package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Shape
import com.eightsines.bpe.graphics.executePair

class SelectionController(private val graphicsEngine: GraphicsEngine) {
    var selectionState: BpeSelectionState = BpeSelectionState.None
        private set

    val isSelected
        get() = selectionState !is BpeSelectionState.None

    val isFloating
        get() = selectionState is BpeSelectionState.Floating

    val selection
        get() = when (val selectionState = this.selectionState) {
            is BpeSelectionState.Selected -> selectionState.selection
            is BpeSelectionState.Floating -> selectionState.selection
            else -> null
        }

    fun restoreFromHistory(state: BpeSelectionState) {
        selectionState = state
    }

    fun anchor(layerCanvasType: CanvasType?): SelectionResult = when (val selectionState = this.selectionState) {
        is BpeSelectionState.None -> SelectionResult.Empty

        is BpeSelectionState.Selected -> if (layerCanvasType == selectionState.selection.canvasType) {
            SelectionResult.Empty
        } else {
            executeSelectedDeselect(selectionState)
        }

        is BpeSelectionState.Floating -> executeFloatingAnchor(selectionState)
    }

    fun deselect(): SelectionResult = when (val selectionState = this.selectionState) {
        is BpeSelectionState.None -> SelectionResult.Empty
        is BpeSelectionState.Selected -> executeSelectedDeselect(selectionState)
        is BpeSelectionState.Floating -> executeFloatingAnchor(selectionState)
    }

    fun cut(currentCanvasLayer: CanvasLayer<*>): SelectionResult {
        val selection = this.selection ?: return SelectionResult.Empty

        val deselectResult = deselect()
        val selectionBox = selection.drawingBox

        val clipboard = BpeClipboard(
            drawingX = selectionBox.x,
            drawingY = selectionBox.y,
            crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
        )

        val cutActions = graphicsEngine.executePair(
            GraphicsAction.ReplaceShape(
                currentCanvasLayer.uid,
                Shape.FillBox(selectionBox, currentCanvasLayer.canvasType.transparentCell),
            )
        ) ?: return deselectResult

        selectionState = BpeSelectionState.Selected(selection)

        return SelectionResult(
            shouldRefresh = true,
            historyStep = HistoryStep(
                actions = deselectResult.historyStep.actions + listOf(HistoryAction.Graphics(cutActions.action)),
                undoActions = listOf(HistoryAction.Graphics(cutActions.undoAction)) + deselectResult.historyStep.undoActions,
            ),
            clipboard = clipboard,
        )
    }

    fun copy(currentCanvasLayer: CanvasLayer<*>): SelectionResult {
        val selection = this.selection ?: return SelectionResult.Empty

        val deselectResult = deselect()
        val selectionBox = selection.drawingBox

        val clipboard = BpeClipboard(
            drawingX = selectionBox.x,
            drawingY = selectionBox.y,
            crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
        )

        selectionState = BpeSelectionState.Selected(selection)
        return SelectionResult(shouldRefresh = true, historyStep = deselectResult.historyStep, clipboard = clipboard)
    }

    fun paste(currentCanvasLayer: CanvasLayer<*>, clipboard: BpeClipboard): SelectionResult {
        if (clipboard.crate.canvasType != currentCanvasLayer.canvasType) {
            return SelectionResult.Empty
        }

        val deselectResult = deselect()

        val overlayActions = graphicsEngine.executePair(
            @Suppress("UNCHECKED_CAST")
            GraphicsAction.MergeShape(
                currentCanvasLayer.uid,
                Shape.Cells(clipboard.drawingX, clipboard.drawingY, clipboard.crate as Crate<Cell>),
            )
        ) ?: return deselectResult

        selectionState = BpeSelectionState.Floating(
            selection = Selection(
                clipboard.crate.canvasType,
                Box(clipboard.drawingX, clipboard.drawingY, clipboard.crate.width, clipboard.crate.height),
            ),
            layerUid = currentCanvasLayer.uid,
            crate = clipboard.crate,
            overlayActions = overlayActions,
        )

        return SelectionResult(shouldRefresh = true, historyStep = deselectResult.historyStep)
    }

    fun ongoingCancel(state: BpeSelectionState) {
        (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.undoAction) }
        selectionState = state
        (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.action) }
    }

    fun ongoingFloatMove(drawingX: Int, drawingY: Int): BpeSelectionState.Floating? {
        val selectionState = this.selectionState

        return if (selectionState is BpeSelectionState.Floating && selectionState.selection.drawingBox.contains(drawingX, drawingY)) {
            selectionState
        } else {
            null
        }
    }

    fun ongoingFloatUp(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int): SelectionFloatResult? {
        val selectionState = this.selectionState as? BpeSelectionState.Selected ?: return null

        if (selectionState.selection.canvasType != currentCanvasLayer.canvasType) {
            return null
        }

        val selectionBox = selectionState.selection.drawingBox

        if (!selectionBox.contains(drawingX, drawingY)) {
            return null
        }

        val crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox)

        val cutActions = graphicsEngine.executePair(
            GraphicsAction.ReplaceShape(
                currentCanvasLayer.uid,
                Shape.FillBox(selectionBox, currentCanvasLayer.canvasType.transparentCell),
            ),
        ) ?: return null

        val overlayActions = graphicsEngine.executePair(
            @Suppress("UNCHECKED_CAST")
            GraphicsAction.MergeShape(
                currentCanvasLayer.uid,
                Shape.Cells(selectionBox.x, selectionBox.y, crate as Crate<Cell>),
            ),
        ) ?: return null

        val floatingState = BpeSelectionState.Floating(
            selection = selectionState.selection,
            layerUid = currentCanvasLayer.uid,
            crate = crate,
            overlayActions = overlayActions,
        )

        this.selectionState = floatingState
        return SelectionFloatResult(cutActions = cutActions, floatingState = floatingState)
    }

    fun ongoingSelectedUpdate(selection: Selection): Boolean {
        val newSelectionState = BpeSelectionState.Selected(selection)

        return if (selectionState != newSelectionState) {
            selectionState = newSelectionState
            true
        } else {
            false
        }
    }

    fun ongoingFloatingUpdate(initialState: BpeSelectionState.Floating, offsetX: Int, offsetY: Int): BpeSelectionState.Floating? {
        (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.undoAction) }
        val newSelectionBox = initialState.selection.drawingBox.copyWithOffset(offsetX, offsetY)

        val overlayActions = graphicsEngine.executePair(
            GraphicsAction.MergeShape(
                initialState.layerUid,
                Shape.Cells(newSelectionBox.x, newSelectionBox.y, initialState.crate),
            ),
        ) ?: return null

        val newState = initialState.copy(
            selection = initialState.selection.copy(drawingBox = newSelectionBox),
            overlayActions = overlayActions,
        )

        selectionState = newState
        return newState
    }

    private fun executeSelectedDeselect(selectedState: BpeSelectionState): SelectionResult {
        this.selectionState = BpeSelectionState.None

        return SelectionResult(
            shouldRefresh = true,
            historyStep = HistoryStep(
                listOf(HistoryAction.SelectionState(BpeSelectionState.None)),
                listOf(HistoryAction.SelectionState(selectedState)),
            ),
        )
    }

    private fun executeFloatingAnchor(floatingState: BpeSelectionState.Floating): SelectionResult {
        this.selectionState = BpeSelectionState.None

        return SelectionResult(
            shouldRefresh = true,
            historyStep = HistoryStep(
                listOf(
                    HistoryAction.Graphics(floatingState.overlayActions.action),
                    HistoryAction.SelectionState(BpeSelectionState.None),
                ),
                listOf(
                    // Use overlayActions.action in undoActions, because we are
                    // restoring floatingState, and need to re-apply action.
                    HistoryAction.Graphics(floatingState.overlayActions.action),
                    HistoryAction.SelectionState(floatingState),
                ),
            ),
        )
    }
}

data class SelectionResult(
    val shouldRefresh: Boolean,
    val historyStep: HistoryStep,
    val clipboard: BpeClipboard? = null,
) {
    companion object {
        val Empty = SelectionResult(shouldRefresh = false, historyStep = HistoryStep.Empty, clipboard = null)
    }
}

data class SelectionFloatResult(
    val cutActions: GraphicsActionPair,
    val floatingState: BpeSelectionState.Floating,
)
