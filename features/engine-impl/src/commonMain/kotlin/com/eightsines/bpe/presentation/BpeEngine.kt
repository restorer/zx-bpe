package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.PackableStringBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.UnpackableStringBag
import com.eightsines.bpe.bag.requireNoIllegalArgumentException
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.exporters.ScrExporter
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Layer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.TransformType
import com.eightsines.bpe.foundation.isBlock
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.GraphicsEngine_Stuff
import com.eightsines.bpe.graphics.executePair
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.UidFactory
import kotlin.math.max

class BpeEngine(
    private val logger: Logger,
    private val uidFactory: UidFactory,
    private val graphicsEngine: GraphicsEngine,
    private val selectionController: SelectionController,
    private val paintingController: PaintingController,
    private val scrExporter: ScrExporter,
    private val historyMaxSteps: Int = 10000,
) {
    private val palette = MutablePalette()

    private var toolboxTool: BpeTool = BpeTool.Paint
    private var toolboxPaintShape: BpeShape = BpeShape.Point
    private var toolboxEraseShape: BpeShape = BpeShape.Point

    private var currentLayer: Layer = graphicsEngine.state.backgroundLayer
    private var history: MutableList<HistoryStep> = mutableListOf()
    private var historyPosition: Int = 0
    private var clipboard: BpeClipboard? = null
    private var paintingInformer: BpeInformer? = null

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
            is BpeAction.PaletteSetBackgroundBorder -> executePaletteSetBackgroundBorder(action)
            is BpeAction.PaletteSetBackgroundPaper -> executePaletteSetBackgroundPaper(action)
            is BpeAction.PaletteSetBackgroundBright -> executePaletteSetBackgroundBright(action)

            is BpeAction.PaletteSetPaintSciiInk -> executePaletteSetSciiInk(action)
            is BpeAction.PaletteSetPaintSciiPaper -> executePaletteSetSciiPaper(action)
            is BpeAction.PaletteSetPaintSciiBright -> executePaletteSetSciiBright(action)
            is BpeAction.PaletteSetPaintSciiFlash -> executePaletteSetSciiFlash(action)
            is BpeAction.PaletteSetPaintSciiChar -> executePaletteSetSciiChar(action)
            is BpeAction.PaletteSetPaintBlockColor -> executePaletteSetBlockColor(action)
            is BpeAction.PaletteSetPaintBlockBright -> executePaletteSetBlockBright(action)

            is BpeAction.PaletteSetEraseSciiInk -> executePaletteSetEraseSciiInk(action)
            is BpeAction.PaletteSetEraseSciiPaper -> executePaletteSetEraseSciiPaper(action)
            is BpeAction.PaletteSetEraseSciiBright -> executePaletteSetEraseSciiBright(action)
            is BpeAction.PaletteSetEraseSciiFlash -> executePaletteSetEraseSciiFlash(action)
            is BpeAction.PaletteSetEraseSciiChar -> executePaletteSetEraseSciiChar(action)
            is BpeAction.PaletteSetEraseBlockColor -> executePaletteSetEraseBlockColor(action)
            is BpeAction.PaletteSetEraseBlockBright -> executePaletteSetEraseBlockBright(action)

            is BpeAction.LayersSetCurrent -> executeLayersSetCurrent(action)
            is BpeAction.LayersSetVisible -> executeLayersSetVisible(action)
            is BpeAction.LayersSetLocked -> executeLayersSetLocked(action)
            is BpeAction.LayersSetMasked -> executeLayersSetMasked(action)
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
            is BpeAction.SelectionFlipHorizontal -> executeSelectionFlipHorizontal()
            is BpeAction.SelectionFlipVertical -> executeSelectionFlipVertical()
            is BpeAction.SelectionRotateCw -> executeSelectionRotateCw()
            is BpeAction.SelectionRotateCcw -> executeSelectionRotateCcw()
            is BpeAction.SelectionFill -> executeSelectionFill()
            is BpeAction.SelectionClear -> executeSelectionClear()

            is BpeAction.CanvasDown -> executeCanvasDown(action)
            is BpeAction.CanvasMove -> executeCanvasMove(action)
            is BpeAction.CanvasUp -> executeCanvasUp(action)
            is BpeAction.CanvasCancel -> executeCanvasCancel()

            is BpeAction.SetPaintingMode -> executeSetPaintingMode(action)
        }

        if (shouldRefresh) {
            shouldRefresh = false
            state = refresh()
        }

        logger.trace("BpeEngine.execute:end") {
            put("state", state.toString())
        }
    }

    fun exportToTap(): List<Byte> {
        return emptyList()
    }

    fun exportToScr(): List<Byte> = scrExporter.export(graphicsEngine.state.preview)
    fun selfUnpacker(): BagStuffUnpacker<BpeEngine> = Unpacker()

    fun clear() {
        graphicsEngine.clear()

        palette.clear()
        toolboxTool = BpeTool.Paint
        toolboxPaintShape = BpeShape.Point
        toolboxEraseShape = BpeShape.Point
        currentLayer = graphicsEngine.state.backgroundLayer
        history = mutableListOf()
        historyPosition = 0
        clipboard = null

        state = refresh()
    }

    //
    // Palette background
    //

    private fun executePaletteSetBackgroundBorder(action: BpeAction.PaletteSetBackgroundBorder) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundColor(action.color)).toHistoryStep())
            shouldRefresh = true
        }
    }

    private fun executePaletteSetBackgroundPaper(action: BpeAction.PaletteSetBackgroundPaper) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundBorder(action.color)).toHistoryStep())
            shouldRefresh = true
        }
    }

    private fun executePaletteSetBackgroundBright(action: BpeAction.PaletteSetBackgroundBright) {
        if (currentLayer is BackgroundLayer) {
            appendHistoryStep(graphicsEngine.executePair(GraphicsAction.SetBackgroundBright(action.light)).toHistoryStep())
            shouldRefresh = true
        }
    }

    //
    // Palette paint SCII
    //

    private fun executePaletteSetSciiInk(action: BpeAction.PaletteSetPaintSciiInk) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.paintSciiInk = action.color
            shouldRefresh = true
        }
    }

    private fun executePaletteSetSciiPaper(action: BpeAction.PaletteSetPaintSciiPaper) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.paintSciiPaper = action.color
            shouldRefresh = true
        }
    }

    private fun executePaletteSetSciiBright(action: BpeAction.PaletteSetPaintSciiBright) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.paintSciiBright = action.light
            shouldRefresh = true
        }
    }

    private fun executePaletteSetSciiFlash(action: BpeAction.PaletteSetPaintSciiFlash) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.paintSciiFlash = action.light
            shouldRefresh = true
        }
    }

    private fun executePaletteSetSciiChar(action: BpeAction.PaletteSetPaintSciiChar) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.paintSciiCharacter = action.character
            shouldRefresh = true
        }
    }

    //
    // Palette paint block
    //

    private fun executePaletteSetBlockColor(action: BpeAction.PaletteSetPaintBlockColor) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) {
            palette.paintBlockColor = action.color
            shouldRefresh = true
        }
    }

    private fun executePaletteSetBlockBright(action: BpeAction.PaletteSetPaintBlockBright) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) {
            palette.paintBlockBright = action.light
            shouldRefresh = true
        }
    }

    //
    // Palette erase SCII
    //

    private fun executePaletteSetEraseSciiInk(action: BpeAction.PaletteSetEraseSciiInk) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.eraseSciiInk = action.shouldEraseColor
            shouldRefresh = true
        }
    }

    private fun executePaletteSetEraseSciiPaper(action: BpeAction.PaletteSetEraseSciiPaper) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.eraseSciiPaper = action.shouldEraseColor
            shouldRefresh = true
        }
    }

    private fun executePaletteSetEraseSciiBright(action: BpeAction.PaletteSetEraseSciiBright) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.eraseSciiBright = action.shouldEraseLight
            shouldRefresh = true
        }
    }

    private fun executePaletteSetEraseSciiFlash(action: BpeAction.PaletteSetEraseSciiFlash) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.eraseSciiFlash = action.shouldEraseLight
            shouldRefresh = true
        }
    }

    private fun executePaletteSetEraseSciiChar(action: BpeAction.PaletteSetEraseSciiChar) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) {
            palette.eraseSciiCharacter = action.shouldEraseCharacter
            shouldRefresh = true
        }
    }

    //
    // Palette erase block
    //

    private fun executePaletteSetEraseBlockColor(action: BpeAction.PaletteSetEraseBlockColor) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) {
            palette.eraseBlockColor = action.shouldEraseColor
            shouldRefresh = true
        }
    }

    private fun executePaletteSetEraseBlockBright(action: BpeAction.PaletteSetEraseBlockBright) {
        if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) {
            palette.eraseBlockBright = action.shouldEraseLight
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
            val cancelStep = cancelPaintingAndAnchorFloating((newLayer as? CanvasLayer<*>)?.canvasType)

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

        val cancelStep = cancelPaintingAndAnchorFloating(selectionController.selection?.canvasType)

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

        val cancelStep = cancelPaintingAndAnchorFloating(selectionController.selection?.canvasType)

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

    private fun executeLayersSetMasked(action: BpeAction.LayersSetMasked) {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return

        if (action.layerUid == currentLayer.uid && action.isMasked == currentCanvasLayer.isMasked) {
            return
        }

        val cancelStep = cancelPaintingAndAnchorFloating(selectionController.selection?.canvasType)

        val actionStep = graphicsEngine.executePair(
            GraphicsAction.SetLayerMasked(layerUid = action.layerUid, isMasked = action.isMasked)
        ).toHistoryStep()

        appendHistoryStep(cancelStep.merge(actionStep))
        shouldRefresh = true
    }

    private fun executeLayersMoveUp() {
        cachedMoveUpOnTopOfLayer?.let {
            val cancelStep = cancelPaintingAndAnchorFloating(selectionController.selection?.canvasType)

            val actionStep = graphicsEngine.executePair(
                GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid)
            ).toHistoryStep()

            appendHistoryStep(cancelStep.merge(actionStep))
            shouldRefresh = true
        }
    }

    private fun executeLayersMoveDown() {
        cachedMoveDownOnTopOfLayer?.let {
            val cancelStep = cancelPaintingAndAnchorFloating(selectionController.selection?.canvasType)

            val actionStep = graphicsEngine.executePair(
                GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid)
            ).toHistoryStep()

            appendHistoryStep(cancelStep.merge(actionStep))
            shouldRefresh = true
        }
    }

    private fun executeLayersCreate(action: BpeAction.LayersCreate) {
        val cancelStep = cancelPaintingAndAnchorFloating(action.canvasType)
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
        val cancelStep = cancelPaintingAndAnchorFloating((layerBelow as? CanvasLayer<*>)?.canvasType)

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
        val cancelStep = cancelPaintingAndAnchorFloating(layerBelow.canvasType)

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

        val cancelStep = cancelPaintingAndAnchorFloating(action.canvasType)

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

        toolboxTool = BpeTool.Select
        shouldRefresh = true
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

    private fun executeSelectionFlipHorizontal() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.transform(it, TransformType.FlipHorizontal))
    }

    private fun executeSelectionFlipVertical() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.transform(it, TransformType.FlipVertical))
    }

    private fun executeSelectionRotateCw() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.transform(it, TransformType.RotateCw))
    }

    private fun executeSelectionRotateCcw() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.transform(it, TransformType.RotateCcw))
    }

    private fun executeSelectionFill() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.paint(it.uid, palette.makePaintCell(it.canvasType)))
    }

    private fun executeSelectionClear() = (currentLayer as? CanvasLayer<*>)?.let {
        cancelPainting()
        processSelectionResult(selectionController.paint(it.uid, palette.makeEraseCell(it.canvasType)))
    }

    //
    // Canvas
    //

    private fun executeCanvasDown(action: BpeAction.CanvasDown) {
        processPaintingResult(
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
        )
    }

    private fun executeCanvasMove(action: BpeAction.CanvasMove) {
        processPaintingResult(paintingController.update(action.drawingX, action.drawingY))
    }

    private fun executeCanvasUp(action: BpeAction.CanvasUp) {
        val result = paintingController.finish(action.drawingX, action.drawingY)
        appendHistoryStep(result.historyStep)

        if (result.shouldRefresh) {
            shouldRefresh = true
        }

        if (paintingInformer != null) {
            paintingInformer = null
            shouldRefresh = true
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun executeCanvasCancel() = cancelPainting()

    //
    // Mode
    //

    private fun executeSetPaintingMode(action: BpeAction.SetPaintingMode) {
        processPaintingResult(paintingController.updatePaintingMode(action.mode))
    }

    //
    // Utils
    //

    @Suppress("NOTHING_TO_INLINE")
    private inline fun cancelPainting() {
        if (paintingController.cancel(::performHistoryActions)) {
            shouldRefresh = true
        }

        if (paintingInformer != null) {
            paintingInformer = null
            shouldRefresh = true
        }
    }

    private fun cancelPaintingAndAnchorFloating(layerCanvasType: CanvasType?): HistoryStep {
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

    private fun processPaintingResult(result: PaintingResult) {
        if (result.informer != null) {
            paintingInformer = result.informer
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

                is HistoryAction.SelectionTransform -> {
                    selectionController.transformFromHistory(action.transformType)
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

            paletteBackgroundBorder = if (currentLayer is BackgroundLayer) currentLayer.border else null,
            paletteBackgroundPaper = if (currentLayer is BackgroundLayer) currentLayer.color else null,
            paletteBackgroundBright = if (currentLayer is BackgroundLayer) currentLayer.bright else null,

            palettePaintSciiInk = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.paintSciiInk else null,
            palettePaintSciiPaper = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.paintSciiPaper else null,
            palettePaintSciiBright = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.paintSciiBright else null,
            palettePaintSciiFlash = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.paintSciiFlash else null,
            palettePaintSciiChar = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.paintSciiCharacter else null,
            palettePaintBlockColor = if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) palette.paintBlockColor else null,
            palettePaintBlockBright = if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) palette.paintBlockBright else null,

            paletteEraseSciiInk = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.eraseSciiInk else null,
            paletteEraseSciiPaper = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.eraseSciiPaper else null,
            paletteEraseSciiBright = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.eraseSciiBright else null,
            paletteEraseSciiFlash = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.eraseSciiFlash else null,
            paletteEraseSciiChar = if ((currentLayer as? CanvasLayer<*>)?.canvasType == CanvasType.Scii) palette.eraseSciiCharacter else null,
            paletteEraseBlockColor = if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) palette.eraseBlockColor else null,
            paletteEraseBlockBright = if ((currentLayer as? CanvasLayer<*>)?.canvasType.isBlock) palette.eraseBlockBright else null,

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
            selectionIsActionable = selectionController.isSelected && currentLayer is CanvasLayer<*>,
            selectionIsFloating = selectionController.isFloating,

            paintingMode = paintingController.paintingMode,
            informer = paintingInformer ?: selectionController.informer,
            historySteps = history.size,
            isPainting = paintingController.isActive,
        )
    }

    class Packer(private val historyStepsLimit: Int = -1) : BagStuffPacker<BpeEngine> {
        override val putInTheBagVersion = 2

        override fun putInTheBag(bag: PackableBag, value: BpeEngine) {
            val history: MutableList<HistoryStep>
            val historyPosition: Int

            when {
                historyStepsLimit < 0 || value.history.size <= historyStepsLimit -> {
                    history = value.history
                    historyPosition = value.historyPosition
                }

                historyStepsLimit == 0 -> {
                    history = mutableListOf()
                    historyPosition = 0
                }

                else -> {
                    val sizeDiff = value.history.size - historyStepsLimit
                    history = value.history.subList(sizeDiff, value.history.size)
                    historyPosition = max(0, value.historyPosition - sizeDiff)
                }
            }

            bag.put(MutablePalette_Stuff, value.palette)
            bag.put(value.toolboxTool.value)
            bag.put(value.toolboxPaintShape.value)
            bag.put(value.toolboxEraseShape.value)
            bag.put(value.currentLayer.uid.value)
            bag.putList(history) { bag.put(HistoryStep_Stuff, it) }
            bag.put(historyPosition)
            bag.put(BpeClipboard_Stuff, value.clipboard)

            // Put GraphicsEngine at the end, to be able to recover from unpack error in Unpacker
            bag.put(GraphicsEngine_Stuff, value.graphicsEngine)
        }
    }

    private inner class Unpacker(private val isLegacyMode: Boolean = false) : BagStuffUnpacker<BpeEngine> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BpeEngine {
            requireSupportedStuffVersion("BpeEngine", 2, version)

            if (isLegacyMode) {
                applyStuff(unpackStuff(bag))
                return this@BpeEngine
            }

            if (version < 2) {
                val stableGraphicsEngineBagData = PackableStringBag()
                    .also { it.put(GraphicsEngine_Stuff, graphicsEngine) }
                    .toString()

                graphicsEngine.selfUnpacker().getOutOfTheBag(1, bag)

                return try {
                    bag.getStuff(Unpacker(true))
                } catch (e: BagUnpackException) {
                    UnpackableStringBag(stableGraphicsEngineBagData).getStuff(graphicsEngine.selfUnpacker())
                    throw e
                }
            }

            val stuff = unpackStuff(bag)
            bag.getStuff(graphicsEngine.selfUnpacker())
            applyStuff(stuff)

            return this@BpeEngine
        }

        private fun unpackStuff(bag: UnpackableBag): UnpackerStuff {
            val palette = bag.getStuff(MutablePalette_Stuff)
            val toolboxTool = requireNoIllegalArgumentException { BpeTool.of(bag.getInt()) }
            val toolboxPaintShape = requireNoIllegalArgumentException { BpeShape.of(bag.getInt()) }
            val toolboxEraseShape = requireNoIllegalArgumentException { BpeShape.of(bag.getInt()) }
            val currentLayerUid = LayerUid(bag.getString())
            val history = bag.getList { bag.getStuff(HistoryStep_Stuff) }
            val historyPosition = bag.getInt()
            val clipboard = bag.getStuffOrNull(BpeClipboard_Stuff)

            return UnpackerStuff(
                palette = palette,
                toolboxTool = toolboxTool,
                toolboxPaintShape = toolboxPaintShape,
                toolboxEraseShape = toolboxEraseShape,
                currentLayerUid = currentLayerUid,
                history = history,
                historyPosition = historyPosition,
                clipboard = clipboard,
            )
        }

        private fun applyStuff(stuff: UnpackerStuff) {
            palette.setFrom(stuff.palette)
            toolboxTool = stuff.toolboxTool
            toolboxPaintShape = stuff.toolboxPaintShape
            toolboxEraseShape = stuff.toolboxEraseShape
            currentLayer = graphicsEngine.state.canvasLayersMap[stuff.currentLayerUid.value] ?: graphicsEngine.state.backgroundLayer
            history = stuff.history
            historyPosition = stuff.historyPosition
            clipboard = stuff.clipboard

            state = refresh()
        }
    }

    private class UnpackerStuff(
        val palette: MutablePalette,
        val toolboxTool: BpeTool,
        val toolboxPaintShape: BpeShape,
        val toolboxEraseShape: BpeShape,
        val currentLayerUid: LayerUid,
        val history: MutableList<HistoryStep>,
        val historyPosition: Int,
        val clipboard: BpeClipboard?,
    )
}
