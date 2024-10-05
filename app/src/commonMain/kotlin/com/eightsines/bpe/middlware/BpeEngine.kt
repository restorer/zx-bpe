package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.Layer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Shape
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.UidFactory

class BpeEngine(
    private val logger: Logger,
    private val uidFactory: UidFactory,
    private val graphicsEngine: GraphicsEngine,
    private val historyMaxSteps: Int = 10000,
) {
    private var paletteInk: SciiColor = SciiColor.Transparent
    private var palettePaper: SciiColor = SciiColor.Transparent
    private var paletteBright: SciiLight = SciiLight.Transparent
    private var paletteFlash: SciiLight = SciiLight.Transparent
    private var paletteChar: SciiChar = SciiChar.Transparent

    private var toolboxTool: BpeTool = BpeTool.Paint
    private var toolboxPaintShape: BpeShape = BpeShape.Point
    private var toolboxEraseShape: BpeShape = BpeShape.Point

    private var selectionState: BpeSelectionState = BpeSelectionState.None
    private var clipboard: BpeClipboard? = null

    private var history: MutableList<HistoryStep> = mutableListOf()
    private var historyPosition: Int = 0

    private var currentLayer: Layer = graphicsEngine.state.backgroundLayer
    private var currentPaintingSpec: BpePaintingSpec? = null

    private var cachedMoveUpOnTopOfLayer: Layer? = null
    private var cachedMoveDownOnTopOfLayer: Layer? = null
    private var cachedLayerBelow: Layer? = null

    private var shouldRefresh: Boolean = false
    private val historyPendingActions = mutableListOf<HistoryAction>()
    private val historyPendingUndoActions = mutableListOf<HistoryAction>()

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

        historyPendingApply()

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

    private fun executePaletteSetInk(action: BpeAction.PaletteSetInk) =
        if (currentLayer is BackgroundLayer) {
            executeHistoricalGraphicsAction(GraphicsAction.SetBackgroundColor(action.color))
        } else {
            paletteInk = action.color
            shouldRefresh = true
        }

    private fun executePaletteSetPaper(action: BpeAction.PaletteSetPaper) =
        if (currentLayer is BackgroundLayer) {
            executeHistoricalGraphicsAction(GraphicsAction.SetBackgroundBorder(action.color))
        } else {
            palettePaper = action.color
            shouldRefresh = true
        }

    private fun executePaletteSetBright(action: BpeAction.PaletteSetBright) =
        if (currentLayer is BackgroundLayer) {
            executeHistoricalGraphicsAction(GraphicsAction.SetBackgroundBright(action.light))
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
            cancelPaintingAndAnchorSelection()
            currentLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
            shouldRefresh = true
        }
    }

    private fun executeLayersSetVisible(action: BpeAction.LayersSetVisible) {
        if (action.layerUid != currentLayer.uid || action.isVisible != currentLayer.isVisible) {
            cancelPaintingAndAnchorSelection()

            executeHistoricalGraphicsAction(
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
            cancelPaintingAndAnchorSelection()

            executeHistoricalGraphicsAction(
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
            cancelPaintingAndAnchorSelection()
            executeHistoricalGraphicsAction(GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid))
        }
    }

    private fun executeLayersMoveDown() {
        cachedMoveDownOnTopOfLayer?.let {
            cancelPaintingAndAnchorSelection()
            executeHistoricalGraphicsAction(GraphicsAction.MoveLayer(layerUid = currentLayer.uid, onTopOfLayerUid = it.uid))
        }
    }

    private fun executeLayersCreate(action: BpeAction.LayersCreate) {
        cancelPaintingAndAnchorSelection()
        val newLayerUid = LayerUid(uidFactory.createUid())

        executeHistoricalGraphicsAction(
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

        cancelPaintingAndAnchorSelection()
        val layerBelow = cachedLayerBelow

        executeHistoricalGraphicsAction(GraphicsAction.DeleteLayer(currentLayer.uid)) { graphicsAction, undoGraphicsAction ->
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
        cancelPaintingAndAnchorSelection()

        executeHistoricalGraphicsAction(
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
            cancelPaintingAndAnchorSelection()
            executeHistoricalGraphicsAction(GraphicsAction.ConvertLayer(layerUid = currentLayer.uid, canvasType = action.canvasType))
            currentLayer = graphicsEngine.state.canvasLayersMap[currentLayer.uid.value] ?: graphicsEngine.state.backgroundLayer
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

        cancelPaintingAndAnchorSelection()

        val overlayActions = executeGraphicsAction(
            @Suppress("UNCHECKED_CAST")
            GraphicsAction.MergeShape(
                currentCanvasLayer.uid,
                Shape.Cells(clipboard.drawingX, clipboard.drawingY, clipboard.crate as Crate<Cell>),
            ),
        ) ?: return

        selectionState = BpeSelectionState.Floating(
            selection = Selection(
                clipboard.crate.canvasType,
                Box(clipboard.drawingX, clipboard.drawingY, clipboard.crate.width, clipboard.crate.height),
            ),
            layerUid = currentCanvasLayer.uid,
            crate = clipboard.crate,
            overlayActions = overlayActions,
        )
    }

    private fun executeToolboxUndo() {
        if (currentPaintingSpec != null) {
            cancelPainting()
        }

        if (selectionState !is BpeSelectionState.None) {
            (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.second) }
            selectionState = BpeSelectionState.None
            shouldRefresh = true
        }

        if (historyPosition > 0) {
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
        cancelPaintingAndAnchorSelection()

        if (selectionState !is BpeSelectionState.None) {
            selectionState = BpeSelectionState.None
            shouldRefresh = true
        }
    }

    private fun executeSelectionCut() {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return

        val selection = when (val selectionState = this.selectionState) {
            is BpeSelectionState.Selected -> selectionState.selection
            is BpeSelectionState.Floating -> selectionState.selection
            else -> null
        } ?: return

        cancelPaintingAndAnchorSelection()
        val selectionBox = selection.drawingBox

        clipboard = BpeClipboard(
            drawingX = selectionBox.x,
            drawingY = selectionBox.y,
            crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox),
        )

        executeHistoricalGraphicsAction(
            GraphicsAction.ReplaceShape(
                currentCanvasLayer.uid,
                Shape.FillBox(selectionBox, currentCanvasLayer.canvasType.transparentCell),
            )
        )

        selectionState = BpeSelectionState.Selected(selection)
        shouldRefresh = true
    }

    private fun executeSelectionCopy() {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return

        val selection = when (val selectionState = this.selectionState) {
            is BpeSelectionState.Selected -> selectionState.selection
            is BpeSelectionState.Floating -> selectionState.selection
            else -> null
        } ?: return

        cancelPaintingAndAnchorSelection()
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
        cancelPainting()

        when (toolboxTool) {
            BpeTool.None -> Unit
            BpeTool.Paint -> startPainting(toolboxTool, toolboxPaintShape, action.drawingX, action.drawingY)
            BpeTool.Erase -> startPainting(toolboxTool, toolboxEraseShape, action.drawingX, action.drawingY)
            BpeTool.Select -> startPainting(toolboxTool, null, action.drawingX, action.drawingY)
            BpeTool.PickColor -> executePickColor(action.drawingX, action.drawingY)
        }
    }

    private fun executeCanvasMove(action: BpeAction.CanvasMove) {
        if (currentPaintingSpec != null && currentLayer is CanvasLayer<*>) {
            updatePainting(action.drawingX, action.drawingY)
        }
    }

    private fun executeCanvasUp(action: BpeAction.CanvasUp) {
        val currentCanvasLayer = this.currentLayer

        if (currentCanvasLayer is CanvasLayer<*>) {
            when (val spec = currentPaintingSpec) {
                is BpePaintingSpec.Single -> {
                    updatePainting(action.drawingX, action.drawingY)
                    spec.paintActions?.let { historyPendingAppendGraphics(it) }
                }

                is BpePaintingSpec.Multiple -> {
                    updatePainting(action.drawingX, action.drawingY)
                    spec.paintActions?.let { historyPendingAppendGraphics(it) }
                }

                is BpePaintingSpec.Select -> {
                    updatePainting(action.drawingX, action.drawingY)

                    historyPendingAppend(
                        HistoryAction.SelectionState(selectionState),
                        HistoryAction.SelectionState(spec.initialState ?: BpeSelectionState.None),
                    )
                }

                is BpePaintingSpec.MoveSelection -> {
                    updatePainting(action.drawingX, action.drawingY)

                    if (spec.cutActions != null) {
                        historyPendingAppend(
                            HistoryAction.Composite(
                                listOf(
                                    HistoryAction.Graphics(spec.cutActions.first),
                                    HistoryAction.SelectionState(selectionState),
                                ),
                            ),
                            HistoryAction.Composite(
                                listOf(
                                    HistoryAction.Graphics(spec.cutActions.second),
                                    HistoryAction.SelectionState(BpeSelectionState.Selected(spec.initialState.selection)),
                                ),
                            ),
                        )
                    }
                }

                null -> Unit
            }
        }

        currentPaintingSpec = null
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun executeCanvasCancel() = cancelPainting()

    //
    // Utils
    //

    private fun executeHistoryAction(action: HistoryAction): Unit = when (action) {
        is HistoryAction.CurrentLayer -> {
            currentLayer = graphicsEngine.state.canvasLayersMap[action.layerUid.value] ?: graphicsEngine.state.backgroundLayer
            shouldRefresh = true
        }

        is HistoryAction.SelectionState -> {
            (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.second) }
            selectionState = action.selectionState
            (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.first) }
            shouldRefresh = true
        }

        is HistoryAction.Graphics -> shouldRefresh = graphicsEngine.execute(action.graphicsAction) != null || shouldRefresh
        is HistoryAction.Composite -> action.actions.forEach(::executeHistoryAction)
    }

    private fun executePickColor(drawingX: Int, drawingY: Int) {
        when (val currentLayer = this.currentLayer) {
            is BackgroundLayer -> paletteInk = currentLayer.color

            is CanvasLayer<*> -> when (val cell = currentLayer.canvas.getDrawingCell(drawingX, drawingY)) {
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

    private fun startPainting(bpeTool: BpeTool, bpeShape: BpeShape?, drawingX: Int, drawingY: Int) {
        val currentCanvasLayer = this.currentLayer as? CanvasLayer<*> ?: return
        val selectionState = this.selectionState

        currentPaintingSpec = when {
            bpeTool == BpeTool.Select -> when {
                selectionState is BpeSelectionState.Floating &&
                        selectionState.selection.drawingBox.contains(drawingX, drawingY) -> {

                    BpePaintingSpec.MoveSelection(
                        initialState = selectionState,
                        startX = drawingX,
                        startY = drawingY,
                    )
                }

                selectionState is BpeSelectionState.Selected &&
                        selectionState.selection.canvasType == currentCanvasLayer.canvasType &&
                        selectionState.selection.drawingBox.contains(drawingX, drawingY) -> run {

                    val selectionBox = selectionState.selection.drawingBox
                    val crate = Crate.fromCanvasDrawing(currentCanvasLayer.canvas, selectionBox)

                    val cutActions = executeGraphicsAction(
                        GraphicsAction.ReplaceShape(
                            currentCanvasLayer.uid,
                            Shape.FillBox(selectionBox, currentCanvasLayer.canvasType.transparentCell),
                        ),
                    ) ?: return

                    val overlayActions = executeGraphicsAction(
                        @Suppress("UNCHECKED_CAST")
                        GraphicsAction.MergeShape(
                            currentCanvasLayer.uid,
                            Shape.Cells(selectionBox.x, selectionBox.y, crate as Crate<Cell>),
                        ),
                    ) ?: return

                    val floatingState = BpeSelectionState.Floating(
                        selection = selectionState.selection,
                        layerUid = currentCanvasLayer.uid,
                        crate = crate,
                        overlayActions = overlayActions,
                    )

                    this.selectionState = floatingState
                    shouldRefresh = true

                    BpePaintingSpec.MoveSelection(
                        initialState = floatingState,
                        cutActions = cutActions,
                        startX = drawingX,
                        startY = drawingY,
                    )
                }

                else -> {
                    if (selectionState !is BpeSelectionState.Selected) {
                        anchorSelection()
                    }

                    BpePaintingSpec.Select(
                        initialState = selectionState as? BpeSelectionState.Selected,
                        canvasType = currentCanvasLayer.canvasType,
                        startY = drawingY,
                        startX = drawingX,
                    )
                }
            }

            bpeShape == BpeShape.Point -> {
                anchorSelection()
                BpePaintingSpec.Multiple(bpeTool, LinkedHashSet())
            }

            bpeShape != null -> {
                anchorSelection()
                BpePaintingSpec.Single(bpeTool, bpeShape, drawingX, drawingY)
            }

            else -> null
        }

        updatePainting(drawingX, drawingY)
    }

    private fun updatePainting(drawingX: Int, drawingY: Int) {
        val descriptor = getPaintingDescriptor()

        when (val spec = currentPaintingSpec) {
            is BpePaintingSpec.Single -> if (descriptor != null && (spec.lastX != drawingX || spec.lastY != drawingY)) {
                spec.paintActions?.let { graphicsEngine.execute(it.second) }
                spec.lastX = drawingX
                spec.lastY = drawingY

                val shape = when (spec.shape) {
                    BpeShape.Point -> null
                    BpeShape.Line -> Shape.Line(spec.startX, spec.startY, drawingX, drawingY, descriptor.first)
                    BpeShape.FillBox -> Shape.FillBox(spec.startX, spec.startY, drawingX, drawingY, descriptor.first)
                    BpeShape.StrokeBox -> Shape.StrokeBox(spec.startX, spec.startY, drawingX, drawingY, descriptor.first)
                }

                if (shape != null) {
                    spec.paintActions = executeGraphicsAction(descriptor.second(shape))
                }
            }

            is BpePaintingSpec.Multiple -> if (descriptor != null) {
                val point = drawingX to drawingY

                if (!spec.points.contains(point)) {
                    spec.paintActions?.let { graphicsEngine.execute(it.second) }
                    spec.points.add(point)
                    spec.paintActions = executeGraphicsAction(descriptor.second(Shape.Points(spec.points.toList(), descriptor.first)))
                }
            }

            is BpePaintingSpec.Select -> {
                val newSelectionState = BpeSelectionState.Selected(
                    Selection(spec.canvasType, Box.of(spec.startX, spec.startY, drawingX, drawingY))
                )

                if (selectionState != newSelectionState) {
                    selectionState = newSelectionState
                    shouldRefresh = true
                }
            }

            is BpePaintingSpec.MoveSelection -> run {
                val offsetX = drawingX - spec.startX
                val offsetY = drawingY - spec.startY

                if (offsetX == spec.lastOffsetX && offsetY == spec.lastOffsetY) {
                    return@run
                }

                (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.second) }
                val newSelectionBox = spec.initialState.selection.drawingBox.copyWithOffset(offsetX, offsetY)

                val overlayActions = executeGraphicsAction(
                    GraphicsAction.MergeShape(
                        spec.initialState.layerUid,
                        Shape.Cells(newSelectionBox.x, newSelectionBox.y, spec.initialState.crate),
                    ),
                ) ?: return@run

                selectionState = spec.initialState.copy(
                    selection = spec.initialState.selection.copy(drawingBox = newSelectionBox),
                    overlayActions = overlayActions,
                )

                spec.lastOffsetX = offsetX
                spec.lastOffsetY = offsetY
            }

            null -> Unit
        }
    }

    private fun getPaintingDescriptor(): Pair<Cell, (Shape<Cell>) -> GraphicsAction>? {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return null

        return when (toolboxTool) {
            BpeTool.Paint -> when (currentCanvasLayer.canvasType) {
                is CanvasType.Scii -> SciiCell(
                    character = paletteChar,
                    ink = paletteInk,
                    paper = palettePaper,
                    bright = paletteBright,
                    flash = paletteFlash,
                )

                is CanvasType.HBlock, is CanvasType.VBlock, is CanvasType.QBlock -> BlockCell(paletteInk, paletteBright)
            } to { GraphicsAction.MergeShape(currentCanvasLayer.uid, it) }

            BpeTool.Erase -> currentCanvasLayer.canvasType.transparentCell to
                    { GraphicsAction.ReplaceShape(currentCanvasLayer.uid, it) }

            else -> null
        }
    }

    private fun cancelPainting() {
        when (val spec = currentPaintingSpec) {
            null -> Unit

            is BpePaintingSpec.Single -> {
                spec.paintActions?.let { graphicsEngine.execute(it.second) }
                shouldRefresh = true
            }

            is BpePaintingSpec.Multiple -> {
                spec.paintActions?.let { graphicsEngine.execute(it.second) }
                shouldRefresh = true
            }

            is BpePaintingSpec.Select -> {
                selectionState = BpeSelectionState.None
                shouldRefresh = true
            }

            is BpePaintingSpec.MoveSelection -> {
                (selectionState as? BpeSelectionState.Floating)?.overlayActions?.let { graphicsEngine.execute(it.second) }

                if (spec.cutActions != null) {
                    graphicsEngine.execute(spec.cutActions.second)
                    selectionState = BpeSelectionState.Selected(spec.initialState.selection)
                } else {
                    graphicsEngine.execute(spec.initialState.overlayActions.first)
                    selectionState = spec.initialState
                }

                shouldRefresh = true
            }
        }

        currentPaintingSpec = null
    }

    private fun anchorSelection() {
        val floatingState = selectionState as? BpeSelectionState.Floating

        if (floatingState != null) {
            historyPendingAppend(
                HistoryAction.Composite(
                    listOf(
                        HistoryAction.Graphics(floatingState.overlayActions.first),
                        HistoryAction.SelectionState(BpeSelectionState.None),
                    ),
                ),
                HistoryAction.Composite(
                    listOf(
                        HistoryAction.Graphics(floatingState.overlayActions.second),
                        HistoryAction.SelectionState(floatingState),
                    ),
                ),
            )
        }

        shouldRefresh = selectionState !is BpeSelectionState.None
        selectionState = BpeSelectionState.None
    }

    private fun cancelPaintingAndAnchorSelection() {
        cancelPainting()
        anchorSelection()
    }

    private fun executeGraphicsAction(graphicsAction: GraphicsAction): Pair<GraphicsAction, GraphicsAction>? {
        val undoAction = graphicsEngine.execute(graphicsAction)

        return if (undoAction != null) {
            shouldRefresh = true
            graphicsAction to undoAction
        } else {
            null
        }
    }

    private fun executeHistoricalGraphicsAction(
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

    private fun historyPendingAppendGraphics(actions: Pair<GraphicsAction, GraphicsAction>) {
        historyPendingActions.add(HistoryAction.Graphics(actions.first))
        historyPendingUndoActions.add(HistoryAction.Graphics(actions.second))
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

        logger.note("BpeEngine.historyPendingApply") {
            put("step", step.toString())
        }

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

private sealed interface BpePaintingSpec {
    data class Single(
        val tool: BpeTool,
        val shape: BpeShape,
        val startX: Int,
        val startY: Int,
        var paintActions: Pair<GraphicsAction, GraphicsAction>? = null,
        var lastX: Int? = null,
        var lastY: Int? = null,
    ) : BpePaintingSpec

    data class Multiple(
        val tool: BpeTool,
        var points: LinkedHashSet<Pair<Int, Int>>,
        var paintActions: Pair<GraphicsAction, GraphicsAction>? = null,
    ) : BpePaintingSpec

    data class Select(
        val initialState: BpeSelectionState.Selected?,
        val canvasType: CanvasType,
        val startX: Int,
        val startY: Int,
    ) : BpePaintingSpec

    data class MoveSelection(
        val initialState: BpeSelectionState.Floating,
        val cutActions: Pair<GraphicsAction, GraphicsAction>? = null,
        val startX: Int,
        val startY: Int,
        var lastOffsetX: Int = 0,
        var lastOffsetY: Int = 0,
    ) : BpePaintingSpec
}
