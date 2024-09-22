package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Box
import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.Selection
import com.eightsines.bpe.graphics.Shape
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.Layer
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.state.BackgroundLayerView
import com.eightsines.bpe.state.CanvasLayerView
import com.eightsines.bpe.state.CanvasView
import com.eightsines.bpe.util.UidFactory

class BpeEngine(
    private val uidFactory: UidFactory,
    private val graphicsEngine: GraphicsEngine,
    private val historyMaxSteps: Int = 10000,
) {
    private var paletteInk: SciiColor = SciiColor.Transparent
    private var palettePaper: SciiColor = SciiColor.Transparent
    private var paletteBright: SciiLight = SciiLight.Transparent
    private var paletteFlash: SciiLight = SciiLight.Transparent
    private var paletteChar: SciiChar = SciiChar.Transparent

    private var toolboxTool: BpeTool = BpeTool.None
    private var toolboxPaintShape: BpeShape = BpeShape.Point
    private var toolboxEraseShape: BpeShape = BpeShape.Point

    private var selectionState: BpeSelectionState = BpeSelectionState.None
    private var clipboard: BpeClipboard? = null

    private var history: MutableList<HistoryStep> = mutableListOf()
    private var historyPosition: Int = 0

    private var currentLayer: Layer = graphicsEngine.state.backgroundLayer
    private var currentCanvasAction: BpeCanvasAction? = null

    private var cachedMoveUpOnTopOfLayer: Layer? = null
    private var cachedMoveDownOnTopOfLayer: Layer? = null
    private var cachedLayerBelow: Layer? = null

    private var shouldRefresh: Boolean = false
    private val historyPendingActions = mutableListOf<HistoryAction>()
    private val historyPendingUndoActions = mutableListOf<HistoryAction>()

    var state: BpeState = refresh()
        private set

    fun execute(action: BpeAction) {
        when (action) {
            is BpeAction.PaletteSetInk -> executePaletteSetInk(action)
            is BpeAction.PaletteSetPaper -> executePaletteSetPaper(action)
            is BpeAction.PaletteSetBright -> executePaletteSetBright(action)
            is BpeAction.PaletteSetFlash -> executePaletteSetFlash(action)
            is BpeAction.PaletteSetChar -> executePaletteSetChar(action)

            is BpeAction.LayersSetCurrent -> executeLayersSetCurrent(action)
            is BpeAction.LayersSetVisible -> executeLayersSetVisible(action)
            is BpeAction.LayersSetLocked -> executeLayersSetLocked(action)
            is BpeAction.LayersMoveUp -> executeLayersMoveUp()
            is BpeAction.LayersMoveDown -> executeLayersMoveDown()
            is BpeAction.LayersCreate -> executeLayersCreate(action)
            is BpeAction.LayersDelete -> executeLayersDelete()
            is BpeAction.LayersMerge -> executeLayersMerge()
            is BpeAction.LayersConvert -> executeLayersConvert(action)

            is BpeAction.ToolboxSetTool -> executeToolboxSetTool(action)
            is BpeAction.ToolboxSetShape -> executeToolboxSetShape(action)
            is BpeAction.ToolboxPaste -> executeToolboxPaste()
            is BpeAction.ToolboxUndo -> executeToolboxUndo()
            is BpeAction.ToolboxRedo -> executeToolboxRedo()

            is BpeAction.SelectionDeselect -> executeSelectionDeselect()
            is BpeAction.SelectionCut -> executeSelectionCut()
            is BpeAction.SelectionCopy -> executeSelectionCopy()

            is BpeAction.CanvasDown -> executeCanvasDown(action)
            is BpeAction.CanvasMove -> executeCanvasMove(action)
            is BpeAction.CanvasUp -> executeCanvasUp(action)
            is BpeAction.CanvasCancel -> executeCanvasCancel()
        }

        historyPendingApply()

        if (shouldRefresh) {
            shouldRefresh = false
            state = refresh()
        }
    }

    //
    // Palette
    //

    private fun executePaletteSetInk(action: BpeAction.PaletteSetInk) =
        if (currentLayer is BackgroundLayer) {
            executeGraphicsAction(GraphicsAction.SetBackgroundColor(action.color))
        } else {
            paletteInk = action.color
            shouldRefresh = true
        }

    private fun executePaletteSetPaper(action: BpeAction.PaletteSetPaper) =
        if (currentLayer is BackgroundLayer) {
            executeGraphicsAction(GraphicsAction.SetBackgroundBorder(action.color))
        } else {
            palettePaper = action.color
            shouldRefresh = true
        }

    private fun executePaletteSetBright(action: BpeAction.PaletteSetBright) =
        if (currentLayer is BackgroundLayer) {
            executeGraphicsAction(GraphicsAction.SetBackgroundBright(action.light))
        } else {
            paletteBright = action.light
            shouldRefresh = true
        }

    private fun executePaletteSetFlash(action: BpeAction.PaletteSetFlash) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            paletteFlash = action.light
            shouldRefresh = true
        }
    }

    private fun executePaletteSetChar(action: BpeAction.PaletteSetChar) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            paletteChar = action.character
            shouldRefresh = true
        }
    }

    //
    // Layers
    //

    private fun executeLayersSetCurrent(action: BpeAction.LayersSetCurrent) {
        if (action.layerUid != currentLayer.uid) {
            anchorSelection()
            currentLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
            shouldRefresh = true
        }
    }

    private fun executeLayersSetVisible(action: BpeAction.LayersSetVisible) {
        if (action.layerUid != currentLayer.uid || action.isVisible != currentLayer.isVisible) {
            anchorSelection()

            executeGraphicsAction(
                if (action.layerUid == LayerUid.Background) {
                    GraphicsAction.SetBackgroundVisible(action.isVisible)
                } else {
                    GraphicsAction.SetLayerVisible(layerUid = action.layerUid, isVisible = action.isVisible)
                }
            )
        }
    }

    private fun executeLayersSetLocked(action: BpeAction.LayersSetLocked) {
        if (action.layerUid != currentLayer.uid || action.isLocked != currentLayer.isLocked) {
            anchorSelection()

            executeGraphicsAction(
                if (action.layerUid == LayerUid.Background) {
                    GraphicsAction.SetBackgroundLocked(action.isLocked)
                } else {
                    GraphicsAction.SetLayerLocked(layerUid = action.layerUid, isLocked = action.isLocked)
                }
            )
        }
    }

    private fun executeLayersMoveUp() {
        cachedMoveUpOnTopOfLayer?.let {
            anchorSelection()
            executeGraphicsAction(GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid))
        }
    }

    private fun executeLayersMoveDown() {
        cachedMoveDownOnTopOfLayer?.let {
            anchorSelection()
            executeGraphicsAction(GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid))
        }
    }

    private fun executeLayersCreate(action: BpeAction.LayersCreate) {
        anchorSelection()
        val newLayerUid = LayerUid(uidFactory.createUid())

        executeGraphicsAction(
            GraphicsAction.CreateLayer(canvasType = action.canvasType, layerUid = newLayerUid, onTopOfLayerUid = currentLayer.uid),
        ) { graphicsAction, undoGraphicsAction ->
            val prevCurrentLayer = currentLayer
            currentLayer = graphicsEngine.state.canvasLayersMap[newLayerUid.value] ?: graphicsEngine.state.backgroundLayer

            HistoryAction.Composite(listOf(graphicsAction, HistoryAction.CurrentLayer(newLayerUid))) to
                    HistoryAction.Composite(listOf(undoGraphicsAction, HistoryAction.CurrentLayer(prevCurrentLayer.uid)))
        }
    }

    private fun executeLayersDelete() {
        if (currentLayer !is CanvasLayer<*>) {
            return
        }

        anchorSelection()
        val layerBelow = cachedLayerBelow

        executeGraphicsAction(GraphicsAction.DeleteLayer(currentLayer.uid)) { graphicsAction, undoGraphicsAction ->
            if (layerBelow != null) {
                val prevCurrentLayer = currentLayer
                currentLayer = layerBelow

                HistoryAction.Composite(listOf(graphicsAction, HistoryAction.CurrentLayer(layerBelow.uid))) to
                        HistoryAction.Composite(listOf(undoGraphicsAction, HistoryAction.CurrentLayer(prevCurrentLayer.uid)))
            } else {
                graphicsAction to undoGraphicsAction
            }
        }
    }

    private fun executeLayersMerge() {
        val layerBelow = cachedLayerBelow as? CanvasLayer<*> ?: return
        anchorSelection()

        executeGraphicsAction(
            GraphicsAction.MergeLayers(layerUid = currentLayer.uid, ontoLayerUid = layerBelow.uid),
        ) { graphicsAction, undoGraphicsAction ->
            val prevCurrentLayer = currentLayer
            currentLayer = graphicsEngine.state.canvasLayersMap[layerBelow.uid.value] ?: graphicsEngine.state.backgroundLayer

            HistoryAction.Composite(listOf(graphicsAction, HistoryAction.CurrentLayer(layerBelow.uid))) to
                    HistoryAction.Composite(listOf(undoGraphicsAction, HistoryAction.CurrentLayer(prevCurrentLayer.uid)))
        }
    }

    private fun executeLayersConvert(action: BpeAction.LayersConvert) {
        if (currentLayer is CanvasLayer<*>) {
            anchorSelection()
            executeGraphicsAction(GraphicsAction.ConvertLayer(layerUid = currentLayer.uid, canvasType = action.canvasType))
        }
    }

    //
    // Toolbox
    //

    private fun executeToolboxSetTool(action: BpeAction.ToolboxSetTool) {
        if (action.tool == BpeTool.None || action.tool == BpeTool.PickColor || currentLayer is CanvasLayer<*>) {
            toolboxTool = action.tool
            shouldRefresh = true
        }
    }

    private fun executeToolboxSetShape(action: BpeAction.ToolboxSetShape) {
        when (toolboxTool) {
            BpeTool.Paint -> {
                toolboxPaintShape = action.shape
                shouldRefresh = true
            }

            BpeTool.Erase -> {
                toolboxEraseShape = action.shape
                shouldRefresh = true
            }

            else -> Unit
        }
    }

    private fun executeToolboxPaste() {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return
        val clipboard = this.clipboard

        if (clipboard == null || clipboard.crate.canvasType != currentCanvasLayer.canvasType) {
            return
        }

        anchorSelection()

        @Suppress("UNCHECKED_CAST")
        val overlayAction = GraphicsAction.MergeShape(
            currentCanvasLayer.uid,
            Shape.Cells(clipboard.drawingX, clipboard.drawingY, clipboard.crate as Crate<Cell>),
        )

        val undoOverlayAction = graphicsEngine.execute(overlayAction) ?: return

        selectionState = BpeSelectionState.Floating(
            selection = Selection(
                clipboard.crate.canvasType,
                Box(clipboard.drawingX, clipboard.drawingY, clipboard.crate.width, clipboard.crate.height),
            ),
            offset = 0 to 0,
            layerUid = currentCanvasLayer.uid,
            crate = clipboard.crate,
            cutAction = null,
            undoCutAction = null,
            overlayAction = overlayAction,
            undoOverlayAction = undoOverlayAction,
        )
    }

    private fun executeToolboxUndo() {
        val floatingState = selectionState as? BpeSelectionState.Floating

        if (floatingState != null) {
            graphicsEngine.execute(floatingState.undoOverlayAction)
            floatingState.undoCutAction?.let(graphicsEngine::execute)
            selectionState = BpeSelectionState.Selected(floatingState.selection)
            shouldRefresh = true
        } else if (historyPosition > 0) {
            executeHistoryAction(history[--historyPosition].undoAction)
        }
    }

    private fun executeToolboxRedo() {
        if (selectionState !is BpeSelectionState.Floating && historyPosition < history.size) {
            executeHistoryAction(history[historyPosition++].action)
        }
    }

    //
    // Selection
    //

    private fun executeSelectionDeselect() {
        anchorSelection()

        if (selectionState !is BpeSelectionState.None) {
            selectionState = BpeSelectionState.None
            shouldRefresh = true
        }
    }

    private fun executeSelectionCut() {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return

        val selection = when (val selectionState = this.selectionState) {
            is BpeSelectionState.Selected -> selectionState.selection
            is BpeSelectionState.Floating -> selectionState.selection.copyWithOffset(selectionState.offset)
            else -> null
        } ?: return

        anchorSelection()
        val selectionBox = selection.drawingBox

        clipboard = BpeClipboard(
            drawingX = selectionBox.x,
            drawingY = selectionBox.y,
            crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
        )

        executeGraphicsAction(
            GraphicsAction.ReplaceShape(
                currentCanvasLayer.uid,
                Shape.FillBox(selectionBox.x, selectionBox.y, selectionBox.ex, selectionBox.ey, currentCanvasLayer.canvasType.transparentCell),
            )
        )

        selectionState = BpeSelectionState.Selected(selection)
        shouldRefresh = true
    }

    private fun executeSelectionCopy() {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return

        val selection = when (val selectionState = this.selectionState) {
            is BpeSelectionState.Selected -> selectionState.selection
            is BpeSelectionState.Floating -> selectionState.selection.copyWithOffset(selectionState.offset)
            else -> null
        } ?: return

        anchorSelection()
        val selectionBox = selection.drawingBox

        clipboard = BpeClipboard(
            drawingX = selectionBox.x,
            drawingY = selectionBox.y,
            crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
        )

        selectionState = BpeSelectionState.Selected(selection)
        shouldRefresh = true
    }

    //
    // Canvas
    //

    private fun executeCanvasDown(action: BpeAction.CanvasDown) {
        val currentLayer = this.currentLayer

        when (toolboxTool) {
            BpeTool.None -> Unit

            BpeTool.Paint -> if (currentLayer is CanvasLayer<*>) {
                currentCanvasAction = if (toolboxPaintShape == BpeShape.Point) {
                    BpeCanvasAction.Multiple(BpeTool.Paint, listOf(action.drawingX to action.drawingY))
                } else {
                    BpeCanvasAction.Single(BpeTool.Paint, toolboxPaintShape, action.drawingX, action.drawingY)
                }

                refreshCanvasAction(action.drawingX, action.drawingY)
            }

            BpeTool.Erase -> if (currentLayer is CanvasLayer<*>) {
                currentCanvasAction = if (toolboxEraseShape == BpeShape.Point) {
                    BpeCanvasAction.Multiple(BpeTool.Erase, listOf(action.drawingX to action.drawingY))
                } else {
                    BpeCanvasAction.Single(BpeTool.Erase, toolboxEraseShape, action.drawingX, action.drawingY)
                }

                refreshCanvasAction(action.drawingX, action.drawingY)
            }

            BpeTool.Select -> if (currentLayer is CanvasLayer<*>) {
                currentCanvasAction = BpeCanvasAction.Select(currentLayer.canvasType, action.drawingX, action.drawingY)
                refreshCanvasAction(action.drawingX, action.drawingY)
            }

            BpeTool.PickColor -> {
                when (currentLayer) {
                    is BackgroundLayer -> paletteInk = currentLayer.color

                    is CanvasLayer<*> -> when (val cell = currentLayer.canvas.getDrawingCell(action.drawingX, action.drawingY)) {
                        is SciiCell -> {
                            paletteInk = cell.ink
                            palettePaper = cell.paper
                            paletteBright = cell.bright
                            paletteFlash = cell.flash
                            paletteChar = cell.character
                        }

                        is BlockCell -> {
                            paletteInk = cell.color
                            paletteBright = cell.bright
                        }
                    }
                }

                shouldRefresh = true
            }
        }
    }

    private fun executeCanvasMove(action: BpeAction.CanvasMove) {
        val currentLayer = this.currentLayer

        when (toolboxTool) {
            BpeTool.Paint, BpeTool.Erase, BpeTool.Select -> if (currentLayer is CanvasLayer<*>) {
                refreshCanvasAction(action.drawingX, action.drawingY)
            }

            BpeTool.None, BpeTool.PickColor -> Unit
        }
    }

    private fun executeCanvasUp(action: BpeAction.CanvasUp) {
        val currentLayer = this.currentLayer

        when (toolboxTool) {
            BpeTool.Paint, BpeTool.Erase, BpeTool.Select -> if (currentLayer is CanvasLayer<*>) {
                refreshCanvasAction(action.drawingX, action.drawingY)
                // TODO: нужно за-apply-ить текущий action
            }

            BpeTool.None, BpeTool.PickColor -> Unit
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun executeCanvasCancel() = cancelCanvasAction()

    //
    // Utils
    //

    private fun executeHistoryAction(action: HistoryAction): Unit = when (action) {
        is HistoryAction.CurrentLayer -> {
            currentLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
            shouldRefresh = true
        }

        is HistoryAction.SelectionState -> {
            selectionState = action.selectionState
            shouldRefresh = true
        }

        is HistoryAction.Graphics -> shouldRefresh = graphicsEngine.execute(action.graphicsAction) != null || shouldRefresh
        is HistoryAction.Composite -> action.actions.forEach(::executeHistoryAction)
    }

    private fun refreshCanvasAction(drawingX: Int, drawingY: Int) {
        when (val action = currentCanvasAction) {
            null -> Unit

            is BpeCanvasAction.Single -> {
                // TODO
            }

            is BpeCanvasAction.Multiple -> {
                // TODO
            }

            is BpeCanvasAction.Select -> {
                val newSelectionState = BpeSelectionState.Selected(
                    Selection(action.canvasType, Box.of(action.startX, action.startY, drawingX, drawingY))
                )

                if (selectionState != newSelectionState) {
                    selectionState = newSelectionState
                    shouldRefresh = true
                }
            }
        }
    }

    private fun cancelCanvasAction() {
        if (currentCanvasAction != null) {
            // TODO
            currentCanvasAction = null
        }
    }

    private fun anchorSelection() {
        val floatingState = selectionState as? BpeSelectionState.Floating ?: return

        historyPendingAppend(
            HistoryAction.Composite(
                listOfNotNull(
                    floatingState.cutAction?.let(HistoryAction::Graphics),
                    HistoryAction.Graphics(floatingState.overlayAction),
                    HistoryAction.SelectionState(BpeSelectionState.None),
                )
            ),
            HistoryAction.Composite(
                listOfNotNull(
                    HistoryAction.Graphics(floatingState.undoOverlayAction),
                    floatingState.undoCutAction?.let(HistoryAction::Graphics),
                    HistoryAction.SelectionState(floatingState),
                )
            )
        )

        selectionState = BpeSelectionState.None
        shouldRefresh = true
    }

    private fun executeGraphicsAction(
        graphicsAction: GraphicsAction,
        historyTransformer: ((HistoryAction, HistoryAction) -> Pair<HistoryAction, HistoryAction>)? = null,
    ) {
        val undoAction = graphicsEngine.execute(graphicsAction)

        if (undoAction != null) {
            if (historyTransformer != null) {
                val (transformedAction, transformedUndoAction) = historyTransformer(
                    HistoryAction.Graphics(graphicsAction),
                    HistoryAction.Graphics(undoAction),
                )

                historyPendingAppend(transformedAction, transformedUndoAction)
            } else {
                historyPendingAppend(HistoryAction.Graphics(graphicsAction), HistoryAction.Graphics(undoAction))
            }

            shouldRefresh = true
        }
    }

    private fun historyPendingAppend(action: HistoryAction?, undoAction: HistoryAction?) {
        action?.let(historyPendingActions::add)
        undoAction?.let(historyPendingUndoActions::add)
    }

    private fun historyPendingApply() {
        if (historyPendingActions.isEmpty() && historyPendingUndoActions.isEmpty()) {
            return
        }

        val action = when (historyPendingActions.size) {
            0 -> HistoryAction.Composite(emptyList())
            1 -> historyPendingActions.first()
            else -> HistoryAction.Composite(historyPendingActions)
        }

        val undoAction = when (historyPendingUndoActions.size) {
            0 -> HistoryAction.Composite(emptyList())
            1 -> historyPendingUndoActions.first()
            else -> HistoryAction.Composite(historyPendingUndoActions.reversed())
        }

        val step = HistoryStep(action, undoAction)

        if (historyPosition == historyMaxSteps) {
            history = history.subList(1, historyPosition).also { it.add(step) }
        } else if (historyPosition == history.size) {
            history.add(step)
        } else {
            history = history.subList(0, historyPosition).also { it.add(step) }
        }

        historyPosition = history.size

        historyPendingActions.clear()
        historyPendingUndoActions.clear()
    }

    private fun refresh(): BpeState {
        val graphicsState = graphicsEngine.state
        val currentLayer = this.currentLayer

        val currentCanvasLayerIndex = if (currentLayer is CanvasLayer<*>) {
            graphicsState.canvasLayers.indexOfFirst { it.uid.value == currentLayer.uid.value }
        } else {
            -1
        }

        val moveUpOnTopOfLayer = if (currentCanvasLayerIndex < graphicsState.canvasLayers.size - 1) {
            graphicsState.canvasLayers[currentCanvasLayerIndex + 1]
        } else {
            null
        }

        val moveDownOnTopOfLayer = when {
            currentCanvasLayerIndex > 1 -> graphicsState.canvasLayers[currentCanvasLayerIndex - 2]
            currentCanvasLayerIndex == 1 -> graphicsState.backgroundLayer
            else -> null
        }

        val layerBelow = when {
            currentCanvasLayerIndex > 0 -> graphicsState.canvasLayers[currentCanvasLayerIndex - 1]
            currentCanvasLayerIndex == 0 -> graphicsState.backgroundLayer
            else -> null
        }

        val backgroundLayerView = BackgroundLayerView(graphicsState.backgroundLayer)

        cachedMoveUpOnTopOfLayer = moveUpOnTopOfLayer
        cachedMoveDownOnTopOfLayer = moveDownOnTopOfLayer
        cachedLayerBelow = layerBelow

        return BpeState(
            background = backgroundLayerView,
            canvas = CanvasView(graphicsState.preview),
            drawingType = (currentLayer as? CanvasLayer<*>)?.canvasType,

            paletteInk = if (currentLayer is BackgroundLayer) currentLayer.color else paletteInk,

            palettePaper = when {
                currentLayer is BackgroundLayer -> currentLayer.border
                (currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii -> palettePaper
                else -> null
            },

            paletteBright = if (currentLayer is BackgroundLayer) currentLayer.bright else paletteBright,
            paletteFlash = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) paletteFlash else null,
            paletteChar = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) paletteChar else null,

            layers = graphicsState.canvasLayers.reversed().map(::CanvasLayerView) + listOf(backgroundLayerView),
            layersCurrentUid = currentLayer.uid,

            layersCanMoveUp = moveUpOnTopOfLayer != null && graphicsEngine.canExecute(
                GraphicsAction.MoveLayer(
                    layerUid = currentLayer.uid,
                    onTopOfLayerUid = moveUpOnTopOfLayer.uid,
                ),
            ),

            layersCanMoveDown = moveDownOnTopOfLayer != null && graphicsEngine.canExecute(
                GraphicsAction.MoveLayer(
                    layerUid = currentLayer.uid,
                    onTopOfLayerUid = moveDownOnTopOfLayer.uid,
                ),
            ),

            layersCanDelete = graphicsEngine.canExecute(GraphicsAction.DeleteLayer(currentLayer.uid)),

            layersCanMerge = layerBelow is CanvasLayer<*> && graphicsEngine.canExecute(
                GraphicsAction.MergeLayers(
                    layerUid = currentLayer.uid,
                    ontoLayerUid = layerBelow.uid,
                ),
            ),

            layersCanConvert = graphicsEngine.canExecute(GraphicsAction.ConvertLayer(currentLayer.uid, CanvasType.Scii)) ||
                    graphicsEngine.canExecute(GraphicsAction.ConvertLayer(currentLayer.uid, CanvasType.HBlock)) ||
                    graphicsEngine.canExecute(GraphicsAction.ConvertLayer(currentLayer.uid, CanvasType.VBlock)) ||
                    graphicsEngine.canExecute(GraphicsAction.ConvertLayer(currentLayer.uid, CanvasType.QBlock)),

            toolboxTool = if (currentLayer is CanvasLayer<*>) toolboxTool else BpeTool.PickColor,

            toolboxShape = when {
                currentLayer !is CanvasLayer<*> -> null
                toolboxTool == BpeTool.Paint -> toolboxPaintShape
                toolboxTool == BpeTool.Erase -> toolboxEraseShape
                else -> null
            },

            toolboxAvailTools = setOfNotNull(
                BpeTool.None,
                if (currentLayer is CanvasLayer<*>) BpeTool.Paint else null,
                if (currentLayer is CanvasLayer<*>) BpeTool.Erase else null,
                if (currentLayer is CanvasLayer<*>) BpeTool.Select else null,
                BpeTool.PickColor,
            ),

            toolboxCanSelect = currentLayer is CanvasLayer<*>,
            toolboxCanPaste = clipboard?.let { it.crate.canvasType == (currentLayer as? CanvasLayer<*>)?.canvasType } ?: false,
            toolboxCanUndo = selectionState is BpeSelectionState.Floating || historyPosition > 0,
            toolboxCanRedo = selectionState !is BpeSelectionState.Floating && historyPosition < history.size,

            selection = when (val selectionState = this.selectionState) {
                is BpeSelectionState.Selected -> selectionState.selection
                is BpeSelectionState.Floating -> selectionState.selection
                else -> null
            },

            selectionCanCut = selectionState !is BpeSelectionState.None && currentLayer is CanvasLayer<*>,
            selectionCanCopy = selectionState !is BpeSelectionState.None && currentLayer is CanvasLayer<*>,
            selectionIsFloating = selectionState is BpeSelectionState.Floating,
        )
    }
}

private sealed interface BpeCanvasAction {
    data class Single(val tool: BpeTool, val shape: BpeShape, val startX: Int, val startY: Int) : BpeCanvasAction
    data class Multiple(val tool: BpeTool, var points: List<Pair<Int, Int>>) : BpeCanvasAction
    data class Select(val canvasType: CanvasType, val startX: Int, val startY: Int) : BpeCanvasAction
}
