package com.eightsines.bpe.middlware

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Layer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.executePair
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.UidFactory

class BpeEngine(
    private val logger: Logger,
    private val uidFactory: UidFactory,
    private val graphicsEngine: GraphicsEngine,
    private val selectionController: SelectionController,
    private val paintingController: PaintingController,
    private val historyMaxSteps: Int = 10000,
) {
    private val palette = MutablePalette()

    private var toolboxTool: BpeTool = BpeTool.Paint
    private var toolboxPaintShape: BpeShape = BpeShape.Point
    private var toolboxEraseShape: BpeShape = BpeShape.Point

    private var clipboard: BpeClipboard? = null

    private var history: MutableList<HistoryStep> = mutableListOf()
    private var historyPosition: Int = 0

    private var currentLayer: Layer = graphicsEngine.state.backgroundLayer

    private var cachedMoveUpOnTopOfLayer: Layer? = null
    private var cachedMoveDownOnTopOfLayer: Layer? = null
    private var cachedLayerBelow: Layer? = null

    private var shouldRefresh: Boolean = false

    var state: BpeState = refresh()
        private set

    fun execute(action: BpeAction) {
        logger.note("BpeEngine.execute:begin") {
            put("action", action.toString())
        }

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

        // historyPendingApply()

        if (shouldRefresh) {
            shouldRefresh = false
            state = refresh()
        }

        logger.note("BpeEngine.execute:end") {
            put("state", state.toString())
        }
    }

    //
    // Palette
    //

    private fun executePaletteSetInk(action: BpeAction.PaletteSetInk) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundColor(action.color)).toHistoryStep())
        } else {
            palette.ink = action.color
        }

        shouldRefresh = true
    }

    private fun executePaletteSetPaper(action: BpeAction.PaletteSetPaper) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundBorder(action.color)).toHistoryStep())
        } else {
            palette.paper = action.color
        }

        shouldRefresh = true
    }

    private fun executePaletteSetBright(action: BpeAction.PaletteSetBright) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundBright(action.light)).toHistoryStep())
        } else {
            palette.bright = action.light
        }

        shouldRefresh = true
    }

    private fun executePaletteSetFlash(action: BpeAction.PaletteSetFlash) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.flash = action.light
            shouldRefresh = true
        }
    }

    private fun executePaletteSetChar(action: BpeAction.PaletteSetChar) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.character = action.character
            shouldRefresh = true
        }
    }

    //
    // Layers
    //

    private fun executeLayersSetCurrent(action: BpeAction.LayersSetCurrent) {
        if (action.layerUid != currentLayer.uid) {
            val initialLayerUid = currentLayer.uid
            val newLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
            val cancelStep = cancelPaintingAndFloating((newLayer as? CanvasLayer<*>)?.canvasType)

            currentLayer = newLayer
            shouldRefresh = true

            val actionStep = HistoryStep(
                listOf(HistoryAction.CurrentLayer(newLayer.uid)),
                listOf(HistoryAction.CurrentLayer(initialLayerUid)),
            )

            appendHistoryStep(cancelStep.merge(actionStep))
        }
    }

    private fun executeLayersSetVisible(action: BpeAction.LayersSetVisible) {
        if (action.layerUid == currentLayer.uid && action.isVisible == currentLayer.isVisible) {
            return
        }

        val cancelStep = cancelPaintingAndFloating(selectionController.selection?.canvasType)

        val actionStep = graphicsEngine.executePair(
            if (action.layerUid == LayerUid.Background) {
                GraphicsAction.SetBackgroundVisible(action.isVisible)
            } else {
                GraphicsAction.SetLayerVisible(layerUid = action.layerUid, isVisible = action.isVisible)
            }
        ).toHistoryStep()

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersSetLocked(action: BpeAction.LayersSetLocked) {
        if (action.layerUid == currentLayer.uid && action.isLocked == currentLayer.isLocked) {
            return
        }

        val cancelStep = cancelPaintingAndFloating(selectionController.selection?.canvasType)

        val actionStep = graphicsEngine.executePair(
            if (action.layerUid == LayerUid.Background) {
                GraphicsAction.SetBackgroundLocked(action.isLocked)
            } else {
                GraphicsAction.SetLayerLocked(layerUid = action.layerUid, isLocked = action.isLocked)
            }
        ).toHistoryStep()

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersMoveUp() {
        cachedMoveUpOnTopOfLayer?.let {
            val cancelStep = cancelPaintingAndFloating(selectionController.selection?.canvasType)

            val actionStep = graphicsEngine.executePair(
                GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid)
            ).toHistoryStep()

            appendHistoryStep(cancelStep.merge(actionStep))
            shouldRefresh = true
        }
    }

    private fun executeLayersMoveDown() {
        cachedMoveDownOnTopOfLayer?.let {
            val cancelStep = cancelPaintingAndFloating(selectionController.selection?.canvasType)

            val actionStep = graphicsEngine.executePair(
                GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid)
            ).toHistoryStep()

            appendHistoryStep(cancelStep.merge(actionStep))
            shouldRefresh = true
        }
    }

    private fun executeLayersCreate(action: BpeAction.LayersCreate) {
        val cancelStep = cancelPaintingAndFloating(action.canvasType)
        val newLayerUid = LayerUid(uidFactory.createUid())

        val actionStep = graphicsEngine.executePair(
            GraphicsAction.CreateLayer(canvasType = action.canvasType, layerUid = newLayerUid, onTopOfLayerUid = currentLayer.uid),
        )?.let {
            val prevCurrentLayer = currentLayer
            currentLayer = graphicsEngine.state.canvasLayersMap[newLayerUid.value] ?: graphicsEngine.state.backgroundLayer

            HistoryStep(
                listOf(HistoryAction.Graphics(it.action), HistoryAction.CurrentLayer(newLayerUid)),
                listOf(HistoryAction.Graphics(it.undoAction), HistoryAction.CurrentLayer(prevCurrentLayer.uid)),
            )
        } ?: HistoryStep.Empty

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersDelete() {
        if (currentLayer !is CanvasLayer<*>) {
            return
        }

        val layerBelow = cachedLayerBelow
        val cancelStep = cancelPaintingAndFloating((layerBelow as? CanvasLayer<*>)?.canvasType)

        val actionStep = graphicsEngine.executePair(GraphicsAction.DeleteLayer(currentLayer.uid))?.let {
            if (layerBelow != null) {
                val prevCurrentLayer = currentLayer
                currentLayer = layerBelow

                HistoryStep(
                    listOf(HistoryAction.Graphics(it.action), HistoryAction.CurrentLayer(layerBelow.uid)),
                    listOf(HistoryAction.Graphics(it.undoAction), HistoryAction.CurrentLayer(prevCurrentLayer.uid)),
                )
            } else {
                it.toHistoryStep()
            }
        } ?: HistoryStep.Empty

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersMerge() {
        val layerBelow = cachedLayerBelow as? CanvasLayer<*> ?: return
        val cancelStep = cancelPaintingAndFloating(layerBelow.canvasType)

        val actionStep = graphicsEngine.executePair(
            GraphicsAction.MergeLayers(layerUid = currentLayer.uid, ontoLayerUid = layerBelow.uid),
        )?.let {
            val prevCurrentLayer = currentLayer
            currentLayer = graphicsEngine.state.canvasLayersMap[layerBelow.uid.value] ?: graphicsEngine.state.backgroundLayer

            HistoryStep(
                listOf(HistoryAction.Graphics(it.action), HistoryAction.CurrentLayer(layerBelow.uid)),
                listOf(HistoryAction.Graphics(it.undoAction), HistoryAction.CurrentLayer(prevCurrentLayer.uid)),
            )
        } ?: HistoryStep.Empty

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersConvert(action: BpeAction.LayersConvert) {
        if (currentLayer !is CanvasLayer<*>) {
            return
        }

        val cancelStep = cancelPaintingAndFloating(action.canvasType)

        val actionStep = graphicsEngine.executePair(
            GraphicsAction.ConvertLayer(layerUid = currentLayer.uid, canvasType = action.canvasType)
        ).toHistoryStep()

        currentLayer = graphicsEngine.state.canvasLayersMap[currentLayer.uid.value] ?: graphicsEngine.state.backgroundLayer
        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
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
        val clipboard = this.clipboard ?: return

        processSelectionResult(selectionController.paste(currentCanvasLayer, clipboard))
    }

    private fun executeToolboxUndo() {
        cancelPainting()
        // no need to reset selection, because selection will be undo-ed to its previous state via historical action

        if (historyPosition > 0) {
            performHistoryActions(history[--historyPosition].undoActions)
        }
    }

    private fun executeToolboxRedo() {
        if (historyPosition < history.size) {
            cancelPainting()
            performHistoryActions(history[historyPosition++].actions)
        }
    }

    //
    // Selection
    //

    private fun executeSelectionDeselect() {
        cancelPainting()
        processSelectionResult(selectionController.deselect())
    }

    private fun executeSelectionCut() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.cut(it))
    }

    private fun executeSelectionCopy() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.copy(it))
    }

    //
    // Canvas
    //

    private fun executeCanvasDown(action: BpeAction.CanvasDown) {
        if (
            paintingController.start(
                tool = toolboxTool,
                paintShape = toolboxPaintShape,
                eraseShape = toolboxEraseShape,
                currentLayer = currentLayer,
                palette = palette,
                drawingX = action.drawingX,
                drawingY = action.drawingY,
                historyActionsPerformer = ::performHistoryActions,
            )
        ) {
            shouldRefresh = true
        }
    }

    private fun executeCanvasMove(action: BpeAction.CanvasMove) {
        if (paintingController.update(action.drawingX, action.drawingY)) {
            shouldRefresh = true
        }
    }

    private fun executeCanvasUp(action: BpeAction.CanvasUp) {
        val result = paintingController.finish(action.drawingX, action.drawingY)
        appendHistoryStep(result.historyStep)

        if (result.shouldRefresh) {
            shouldRefresh = true
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun executeCanvasCancel() = cancelPainting()

    //
    // Utils
    //

    @Suppress("NOTHING_TO_INLINE")
    private inline fun cancelPainting() {
        if (paintingController.cancel(::performHistoryActions)) {
            shouldRefresh = true
        }
    }

    private fun cancelPaintingAndFloating(layerCanvasType: CanvasType?): HistoryStep {
        cancelPainting()
        val result = selectionController.anchor(layerCanvasType)

        if (result.shouldRefresh) {
            shouldRefresh = true
        }

        return result.historyStep
    }

    private fun processSelectionResult(result: SelectionResult) {
        appendHistoryStep(result.historyStep)

        if (result.clipboard != null) {
            clipboard = result.clipboard
        }

        if (result.shouldRefresh) {
            shouldRefresh = true
        }
    }

    private fun performHistoryActions(actions: List<HistoryAction>) {
        logger.note("BpeEngine.performHistoryActions:begin") {
            put("actions", actions.toString())
        }

        for (action in actions) {
            logger.note("BpeEngine.performHistoryActions:actionBegin") {
                put("action", action.toString())
            }

            when (action) {
                is HistoryAction.CurrentLayer -> {
                    currentLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
                    shouldRefresh = true
                }

                is HistoryAction.SelectionState -> {
                    selectionController.restoreFromHistory(action.selectionState)
                    shouldRefresh = true
                }

                is HistoryAction.Graphics -> if (graphicsEngine.execute(action.graphicsAction) != null) {
                    shouldRefresh = true
                }
            }

            logger.note("BpeEngine.performHistoryActions:actionEnd") {
                put("action", action.toString())
            }
        }

        logger.note("BpeEngine.performHistoryActions:end") {
            put("actions", actions.toString())
        }
    }

    private fun appendHistoryStep(step: HistoryStep) {
        if (step.actions.isEmpty() && step.undoActions.isEmpty()) {
            return
        }

        logger.note("BpeEngine.appendHistoryStep") {
            put("step", step.toString())
        }

        when (historyPosition) {
            historyMaxSteps -> history = history.subList(1, historyPosition).also { it.add(step) }
            history.size -> history.add(step)
            else -> history = history.subList(0, historyPosition).also { it.add(step) }
        }

        historyPosition = history.size
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

            paletteInk = if (currentLayer is BackgroundLayer) currentLayer.color else palette.ink,

            palettePaper = when {
                currentLayer is BackgroundLayer -> currentLayer.border
                (currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii -> palette.paper
                else -> null
            },

            paletteBright = if (currentLayer is BackgroundLayer) currentLayer.bright else palette.bright,
            paletteFlash = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.flash else null,
            paletteChar = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.character else null,

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
            toolboxCanUndo = selectionController.isFloating || historyPosition > 0,
            toolboxCanRedo = historyPosition < history.size,

            selection = selectionController.selection,
            selectionCanCut = selectionController.isSelected && currentLayer is CanvasLayer<*>,
            selectionCanCopy = selectionController.isSelected && currentLayer is CanvasLayer<*>,
            selectionIsFloating = selectionController.isFloating,
        )
    }
}
