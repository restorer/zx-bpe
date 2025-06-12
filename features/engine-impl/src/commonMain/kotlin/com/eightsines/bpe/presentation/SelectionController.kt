package com.eightsines.bpe.presentation

import com.eightsines.bpe.foundation.Box
import com.eightsines.bpe.foundation.Cell
import com.eightsines.bpe.foundation.toRect
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.foundation.TransformType
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

    val informer: BpeInformer?
        get() = selection?.let { BpeInformer(it.canvasType, it.drawingBox.toRect()) }

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
            drawingX = selectionBox.lx,
            drawingY = selectionBox.ly,
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

    fun copy(currentCanvasLayer: CanvasLayer<*>) = when (val selectionState = selectionState) {
        is BpeSelectionState.None -> SelectionResult.Empty

        is BpeSelectionState.Selected -> {
            val selectionBox = selectionState.selection.drawingBox

            val clipboard = BpeClipboard(
                drawingX = selectionBox.lx,
                drawingY = selectionBox.ly,
                crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
            )

            SelectionResult(shouldRefresh = true, historyStep = HistoryStep.Empty, clipboard = clipboard)
        }

        is BpeSelectionState.Floating -> {
            val selectionBox = selectionState.selection.drawingBox

            val clipboard = BpeClipboard(
                drawingX = selectionBox.lx,
                drawingY = selectionBox.ly,
                crate = selectionState.crate,
            )

            SelectionResult(shouldRefresh = true, historyStep = HistoryStep.Empty, clipboard = clipboard)
        }
    }

    fun transform(currentCanvasLayer: CanvasLayer<*>, transformType: TransformType): SelectionResult {
        val floatResult = tryFloatUp(currentCanvasLayer) ?: return SelectionResult.Empty

        val cutActions = floatResult.cutActions
        val floatingState = floatResult.floatingState

        val floatHistoryStep = if (cutActions != null) {
            HistoryStep(
                listOfNotNull(
                    HistoryAction.Graphics(cutActions.action),
                    HistoryAction.Graphics(floatingState.overlayActions.action),
                    HistoryAction.SelectionState(floatingState),
                ),
                listOfNotNull(
                    HistoryAction.Graphics(floatingState.overlayActions.undoAction),
                    HistoryAction.Graphics(cutActions.undoAction),
                    HistoryAction.SelectionState(BpeSelectionState.Selected(floatingState.selection)),
                ),
            )
        } else {
            HistoryStep.Empty
        }

        transformFromHistory(transformType)

        val historyStep = floatHistoryStep.merge(
            HistoryStep(
                listOf(HistoryAction.SelectionTransform(transformType)),
                listOf(HistoryAction.SelectionTransform(transformType.inverse())),
            )
        )

        return SelectionResult(shouldRefresh = true, historyStep = historyStep)
    }

    fun transformFromHistory(transformType: TransformType) {
        val floatingState = selectionState as? BpeSelectionState.Floating ?: return

        var x = floatingState.selection.drawingBox.lx
        var y = floatingState.selection.drawingBox.ly
        val crate = floatingState.crate.copyTransformed(transformType)

        if (transformType == TransformType.RotateCw || transformType == TransformType.RotateCcw) {
            val mx = x + floatingState.crate.width / 2
            val my = y + floatingState.crate.height / 2

            x = mx - crate.width / 2
            y = my - crate.height / 2
        }

        graphicsEngine.execute(floatingState.overlayActions.undoAction)

        val overlayActions = graphicsEngine.executePair(
            GraphicsAction.MergeShape(floatingState.layerUid, Shape.Cells(x, y, crate)),
        ) ?: return

        selectionState = BpeSelectionState.Floating(
            selection = Selection(floatingState.selection.canvasType, Box.ofSize(x, y, crate.width, crate.height)),
            layerUid = floatingState.layerUid,
            crate = crate,
            overlayActions = overlayActions,
        )
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
                Box.ofSize(clipboard.drawingX, clipboard.drawingY, clipboard.crate.width, clipboard.crate.height),
            ),
            layerUid = currentCanvasLayer.uid,
            crate = clipboard.crate,
            overlayActions = overlayActions,
        )

        return SelectionResult(
            shouldRefresh = true,
            historyStep = deselectResult.historyStep.merge(
                HistoryStep(
                    listOf(
                        HistoryAction.Graphics(overlayActions.action),
                        HistoryAction.SelectionState(selectionState),
                    ),
                    listOf(
                        HistoryAction.Graphics(overlayActions.undoAction),
                        HistoryAction.SelectionState(BpeSelectionState.None),
                    ),
                ),
            ),
        )
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

    fun ongoingFloatUp(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int) = executeFloatUp(currentCanvasLayer) {
        it.contains(drawingX, drawingY)
    }

    fun ongoingSelectedUpdate(selection: Selection?): Boolean {
        val newSelectionState = selection?.let { BpeSelectionState.Selected(it) } ?: BpeSelectionState.None

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
                Shape.Cells(newSelectionBox.lx, newSelectionBox.ly, initialState.crate),
            ),
        ) ?: return null

        val newState = initialState.copy(
            selection = initialState.selection.copy(drawingBox = newSelectionBox),
            overlayActions = overlayActions,
        )

        selectionState = newState
        return newState
    }

    private fun tryFloatUp(currentCanvasLayer: CanvasLayer<*>) =
        (selectionState as? BpeSelectionState.Floating)
            ?.let { SelectionFloatResult(cutActions = null, floatingState = it) }
            ?: executeFloatUp(currentCanvasLayer) { true }

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

    private fun executeFloatUp(currentCanvasLayer: CanvasLayer<*>, hitCheck: (Box) -> Boolean): SelectionFloatResult? {
        val selectionState = this.selectionState as? BpeSelectionState.Selected ?: return null

        if (selectionState.selection.canvasType != currentCanvasLayer.canvasType) {
            return null
        }

        val selectionBox = selectionState.selection.drawingBox

        if (!hitCheck(selectionBox)) {
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
                Shape.Cells(selectionBox.lx, selectionBox.ly, crate as Crate<Cell>),
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

    private fun executeFloatingAnchor(floatingState: BpeSelectionState.Floating): SelectionResult {
        this.selectionState = BpeSelectionState.None

        return SelectionResult(
            shouldRefresh = true,
            historyStep = HistoryStep(
                listOf(
                    // No need to apply overlayActions.action.
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
    val cutActions: GraphicsActionPair?,
    val floatingState: BpeSelectionState.Floating,
)
