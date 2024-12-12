package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.Rect
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Layer
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.graphics.Shape
import com.eightsines.bpe.graphics.executePair

class PaintingController(private val graphicsEngine: GraphicsEngine, private val selectionController: SelectionController) {
    var paintingMode = BpePaintingMode.Edge
        private set

    private var currentPaintingSpec: PaintingSpec? = null
    private var lastDrawingPoint: Pair<Int, Int>? = null
    private var pendingHistoryStep: HistoryStep = HistoryStep.Empty

    fun cancel(historyActionsPerformer: (List<HistoryAction>) -> Unit): Boolean {
        val shouldRefresh = when (val spec = currentPaintingSpec) {
            null -> false

            is PaintingSpec.Single -> {
                spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
                true
            }

            is PaintingSpec.Multiple -> {
                spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
                true
            }

            is PaintingSpec.Selection -> {
                selectionController.ongoingCancel(BpeSelectionState.None)
                true
            }

            is PaintingSpec.Floating -> {
                selectionController.ongoingCancel(BpeSelectionState.Selected(spec.initialState.selection))
                spec.cutActions?.let { graphicsEngine.execute(it.undoAction) }
                true
            }
        }

        currentPaintingSpec = null
        lastDrawingPoint = null

        if (pendingHistoryStep.undoActions.isNotEmpty()) {
            historyActionsPerformer(pendingHistoryStep.undoActions)
        }

        pendingHistoryStep = HistoryStep.Empty
        return shouldRefresh
    }

    fun updatePaintingMode(mode: BpePaintingMode): PaintingResult {
        if (paintingMode == mode) {
            return PaintingResult.Empty
        }

        paintingMode = mode

        return PaintingResult(
            shouldRefresh = true,
            informer = lastDrawingPoint?.let { update(it.first, it.second) }?.informer,
        )
    }

    fun start(
        tool: BpeTool,
        paintShape: BpeShape,
        eraseShape: BpeShape,
        currentLayer: Layer,
        palette: MutablePalette,
        drawingX: Int,
        drawingY: Int,
        historyActionsPerformer: (List<HistoryAction>) -> Unit,
    ): PaintingResult {
        val shouldRefresh = cancel(historyActionsPerformer)

        val startResult = when (tool) {
            BpeTool.None -> PaintingResult.Empty
            BpeTool.Paint -> executeStartPaint(tool, paintShape, currentLayer, palette, drawingX, drawingY)
            BpeTool.Erase -> executeStartPaint(tool, eraseShape, currentLayer, palette, drawingX, drawingY)
            BpeTool.Select -> executeStartSelect(currentLayer, drawingX, drawingY)
            BpeTool.PickColor -> executePickColor(currentLayer, palette, drawingX, drawingY)
        }

        return PaintingResult(
            shouldRefresh = shouldRefresh || startResult.shouldRefresh,
            informer = startResult.informer,
        )
    }

    fun update(drawingX: Int, drawingY: Int): PaintingResult {
        val spec = currentPaintingSpec ?: return PaintingResult.Empty
        lastDrawingPoint = drawingX to drawingY

        return when (spec) {
            is PaintingSpec.Single -> executeUpdateSingle(spec, drawingX, drawingY)
            is PaintingSpec.Multiple -> executeUpdateMultiple(spec, drawingX, drawingY)
            is PaintingSpec.Selection -> executeUpdateSelection(spec, drawingX, drawingY)
            is PaintingSpec.Floating -> executeUpdateFloating(spec, drawingX, drawingY)
        }
    }

    fun finish(drawingX: Int, drawingY: Int): PaintingFinishResult {
        val paintingResult = update(drawingX, drawingY)

        val historyStep = when (val spec = currentPaintingSpec) {
            is PaintingSpec.Single -> spec.paintActions.toHistoryStep()
            is PaintingSpec.Multiple -> spec.paintActions.toHistoryStep()

            is PaintingSpec.Selection -> HistoryStep(
                listOf(HistoryAction.SelectionState(selectionController.selectionState)),
                listOf(HistoryAction.SelectionState(spec.initialState ?: BpeSelectionState.None)),
            )

            is PaintingSpec.Floating -> when {
                spec.cutActions != null ->
                    HistoryStep(
                        listOfNotNull(
                            HistoryAction.Graphics(spec.cutActions.action),
                            HistoryAction.Graphics(spec.lastState.overlayActions.action),
                            HistoryAction.SelectionState(spec.lastState),
                        ),
                        listOfNotNull(
                            HistoryAction.Graphics(spec.lastState.overlayActions.undoAction),
                            HistoryAction.Graphics(spec.cutActions.undoAction),
                            HistoryAction.SelectionState(BpeSelectionState.Selected(spec.initialState.selection)),
                        ),
                    )

                spec.initialState.selection != spec.lastState.selection ->
                    HistoryStep(
                        listOfNotNull(
                            HistoryAction.Graphics(spec.initialState.overlayActions.undoAction),
                            HistoryAction.Graphics(spec.lastState.overlayActions.action),
                            HistoryAction.SelectionState(spec.lastState),
                        ),
                        listOfNotNull(
                            HistoryAction.Graphics(spec.lastState.overlayActions.undoAction),
                            HistoryAction.Graphics(spec.initialState.overlayActions.action),
                            HistoryAction.SelectionState(spec.initialState),
                        ),
                    )

                else -> HistoryStep.Empty
            }

            null -> HistoryStep.Empty
        }

        val result = PaintingFinishResult(
            shouldRefresh = paintingResult.shouldRefresh,
            historyStep = pendingHistoryStep.merge(historyStep),
        )

        currentPaintingSpec = null
        lastDrawingPoint = null
        pendingHistoryStep = HistoryStep.Empty

        return result
    }

    private fun executeStartPaint(
        tool: BpeTool,
        shape: BpeShape,
        currentLayer: Layer,
        palette: MutablePalette,
        drawingX: Int,
        drawingY: Int,
    ): PaintingResult {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return PaintingResult.Empty
        val toolDescriptor = TOOL_DESCRIPTORS[tool] ?: return PaintingResult.Empty
        val shapeDescriptor = SHAPE_DESCRIPTORS[shape] ?: return PaintingResult.Empty

        val deselectResult = selectionController.deselect()
        pendingHistoryStep = deselectResult.historyStep

        currentPaintingSpec = shapeDescriptor.createPaintingSpec(
            canvasType = currentCanvasLayer.canvasType,
            cell = toolDescriptor.getCell(currentCanvasLayer.canvasType, palette),
            shapePainter = toolDescriptor.createShapePainter(currentCanvasLayer.uid),
            drawingX = drawingX,
            drawingY = drawingY,
        )

        val updateResult = update(drawingX, drawingY)

        return PaintingResult(
            shouldRefresh = deselectResult.shouldRefresh || updateResult.shouldRefresh,
            informer = updateResult.informer,
        )
    }

    private fun executeStartSelect(currentLayer: Layer, drawingX: Int, drawingY: Int): PaintingResult {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return PaintingResult.Empty

        val (paintingSpec, shouldRefresh) = executeStartSelectFloatMove(drawingX, drawingY)
            ?: executeStartSelectFloatUp(currentCanvasLayer, drawingX, drawingY)
            ?: executeStartSelectSelection(currentCanvasLayer, drawingX, drawingY)

        currentPaintingSpec = paintingSpec
        val updateResult = update(drawingX, drawingY)

        return PaintingResult(
            shouldRefresh = shouldRefresh || updateResult.shouldRefresh,
            informer = updateResult.informer,
        )
    }

    private fun executeStartSelectFloatMove(drawingX: Int, drawingY: Int) =
        selectionController.ongoingFloatMove(drawingX, drawingY)?.let {
            PaintingSpec.Floating(
                initialState = it,
                initialX = drawingX,
                initialY = drawingY,
                lastState = it,
            ) to false
        }

    private fun executeStartSelectFloatUp(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int) =
        selectionController.ongoingFloatUp(currentCanvasLayer, drawingX, drawingY)?.let {
            PaintingSpec.Floating(
                initialState = it.floatingState,
                cutActions = it.cutActions,
                initialX = drawingX,
                initialY = drawingY,
                lastState = it.floatingState,
            ) to true
        }

    private fun executeStartSelectSelection(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int): Pair<PaintingSpec, Boolean> {
        val initialState = selectionController.selectionState
        val deselectResult = selectionController.deselect()

        pendingHistoryStep = deselectResult.historyStep
        selectionController.ongoingSelectedUpdate(null)

        return PaintingSpec.Selection(
            initialState = initialState as? BpeSelectionState.Selected,
            canvasType = currentCanvasLayer.canvasType,
            drawingEX = currentCanvasLayer.canvas.drawingWidth,
            drawingEY = currentCanvasLayer.canvas.drawingHeight,
            initialY = drawingY,
            initialX = drawingX,
        ) to deselectResult.shouldRefresh
    }

    private fun executePickColor(currentLayer: Layer, palette: MutablePalette, drawingX: Int, drawingY: Int) = when (currentLayer) {
        is BackgroundLayer -> {
            palette.ink = currentLayer.color
            PaintingResult.Updated
        }

        is CanvasLayer<*> -> when (val cell = currentLayer.canvas.getDrawingCell(drawingX, drawingY)) {
            is SciiCell -> {
                palette.ink = cell.ink
                palette.paper = cell.paper
                palette.bright = cell.bright
                palette.flash = cell.flash
                palette.character = cell.character

                PaintingResult.Updated
            }

            is BlockCell -> {
                palette.ink = cell.color
                palette.bright = cell.bright
                PaintingResult.Updated
            }
        }

        else -> PaintingResult.Empty
    }

    private fun executeUpdateSingle(spec: PaintingSpec.Single, drawingX: Int, drawingY: Int): PaintingResult {
        val rect = getPaintingRect(spec.initialX, spec.initialY, drawingX, drawingY)

        if (rect == spec.lastRect) {
            return PaintingResult.Empty
        }

        spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
        spec.lastRect = rect
        spec.paintActions = graphicsEngine.executePair(spec.painter(rect.sx, rect.sy, rect.ex, rect.ey))

        return PaintingResult(
            shouldRefresh = true,
            informer = BpeInformer(
                canvasType = spec.canvasType,
                initialX = spec.initialX,
                initialY = spec.initialY,
                rect = rect,
            ),
        )
    }

    private fun executeUpdateMultiple(spec: PaintingSpec.Multiple, drawingX: Int, drawingY: Int): PaintingResult {
        val point = drawingX to drawingY

        if (spec.points.contains(point)) {
            return PaintingResult.Empty
        }

        spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
        spec.points.add(point)
        spec.paintActions = graphicsEngine.executePair(spec.painter(spec.points.toList()))

        return PaintingResult.Updated
    }

    private fun executeUpdateSelection(spec: PaintingSpec.Selection, drawingX: Int, drawingY: Int): PaintingResult {
        val rect = getPaintingRect(spec.initialX, spec.initialY, drawingX, drawingY)

        if (rect == spec.lastRect) {
            return PaintingResult.Empty
        }

        val box = Box.ofCoords(
            maxOf(0, rect.sx),
            maxOf(0, rect.sy),
            minOf(rect.ex, spec.drawingEX),
            minOf(rect.ey, spec.drawingEY),
        )

        spec.lastRect = rect

        if (box.width > 1 || box.height > 1) {
            spec.isEmpty = false
        }

        if (!spec.isEmpty) {
            selectionController.ongoingSelectedUpdate(Selection(spec.canvasType, box))
        }

        return PaintingResult(
            shouldRefresh = true,
            informer = BpeInformer(
                canvasType = spec.canvasType,
                initialX = spec.initialX,
                initialY = spec.initialY,
                rect = rect,
            ),
        )
    }

    private fun executeUpdateFloating(spec: PaintingSpec.Floating, drawingX: Int, drawingY: Int): PaintingResult {
        val offsetX = drawingX - spec.initialX
        val offsetY = drawingY - spec.initialY

        if (offsetX == spec.lastOffsetX && offsetY == spec.lastOffsetY) {
            return PaintingResult.Empty
        }

        val newState = selectionController.ongoingFloatingUpdate(spec.initialState, offsetX, offsetY)
            ?: return PaintingResult.Empty

        spec.lastOffsetX = offsetX
        spec.lastOffsetY = offsetY
        spec.lastState = newState

        return PaintingResult.Updated
    }

    private fun getPaintingRect(initialX: Int, initialY: Int, drawingX: Int, drawingY: Int) = when (paintingMode) {
        BpePaintingMode.Center -> Rect(initialX * 2 - drawingX, initialY * 2 - drawingY, drawingX, drawingY)
        BpePaintingMode.Edge -> Rect(initialX, initialY, drawingX, drawingY)
    }

    private companion object {
        private val TOOL_DESCRIPTORS = buildMap {
            put(
                BpeTool.Paint,
                object : PaintingToolDescriptor {
                    override fun getCell(canvasType: CanvasType, palette: MutablePalette) = when (canvasType) {
                        is CanvasType.Scii -> SciiCell(
                            character = palette.character,
                            ink = palette.ink,
                            paper = palette.paper,
                            bright = palette.bright,
                            flash = palette.flash,
                        )

                        is CanvasType.HBlock, is CanvasType.VBlock, is CanvasType.QBlock -> BlockCell(palette.ink, palette.bright)
                    }

                    override fun createShapePainter(layerUid: LayerUid): (Shape<Cell>) -> GraphicsAction =
                        { GraphicsAction.MergeShape(layerUid, it) }
                },
            )

            put(
                BpeTool.Erase,
                object : PaintingToolDescriptor {
                    override fun getCell(canvasType: CanvasType, palette: MutablePalette) = canvasType.transparentCell

                    override fun createShapePainter(layerUid: LayerUid): (Shape<Cell>) -> GraphicsAction =
                        { GraphicsAction.ReplaceShape(layerUid, it) }
                }
            )
        }

        private val SHAPE_DESCRIPTORS = buildMap {
            put(
                BpeShape.Point,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Multiple(
                        painter = { shapePainter(Shape.Points(it, cell)) },
                        points = LinkedHashSet(),
                    )
                }
            )

            put(
                BpeShape.Line,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        canvasType = canvasType,
                        painter = { sx, sy, ex, ey -> shapePainter(Shape.Line(sx, sy, ex, ey, cell)) },
                        initialX = drawingX,
                        initialY = drawingY,
                    )
                }
            )

            put(
                BpeShape.FillBox,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        canvasType = canvasType,
                        painter = { sx, sy, ex, ey -> shapePainter(Shape.FillBox(sx, sy, ex, ey, cell)) },
                        initialX = drawingX,
                        initialY = drawingY,
                    )
                }
            )

            put(
                BpeShape.StrokeBox,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        canvasType = canvasType,
                        painter = { sx, sy, ex, ey -> shapePainter(Shape.StrokeBox(sx, sy, ex, ey, cell)) },
                        initialX = drawingX,
                        initialY = drawingY,
                    )
                }
            )

            put(
                BpeShape.FillEllipse,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        canvasType = canvasType,
                        painter = { sx, sy, ex, ey -> shapePainter(Shape.FillEllipse(sx, sy, ex, ey, cell)) },
                        initialX = drawingX,
                        initialY = drawingY,
                    )
                }
            )

            put(
                BpeShape.StrokeEllipse,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        canvasType: CanvasType,
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        canvasType = canvasType,
                        painter = { sx, sy, ex, ey -> shapePainter(Shape.StrokeEllipse(sx, sy, ex, ey, cell)) },
                        initialX = drawingX,
                        initialY = drawingY,
                    )
                }
            )
        }
    }
}

data class PaintingResult(
    val shouldRefresh: Boolean,
    val informer: BpeInformer?,
) {
    companion object {
        val Empty = PaintingResult(shouldRefresh = false, informer = null)
        val Updated = PaintingResult(shouldRefresh = true, informer = null)
    }
}

data class PaintingFinishResult(
    val shouldRefresh: Boolean,
    val historyStep: HistoryStep,
)

private sealed interface PaintingSpec {
    class Single(
        val canvasType: CanvasType,
        val painter: (Int, Int, Int, Int) -> GraphicsAction,
        val initialX: Int,
        val initialY: Int,
        var paintActions: GraphicsActionPair? = null,
        var lastRect: Rect? = null,
    ) : PaintingSpec

    class Multiple(
        val painter: (List<Pair<Int, Int>>) -> GraphicsAction,
        var points: LinkedHashSet<Pair<Int, Int>>,
        var paintActions: GraphicsActionPair? = null,
    ) : PaintingSpec

    class Selection(
        val initialState: BpeSelectionState.Selected?,
        val canvasType: CanvasType,
        val drawingEX: Int,
        val drawingEY: Int,
        val initialX: Int,
        val initialY: Int,
        var isEmpty: Boolean = true,
        var lastRect: Rect? = null,
    ) : PaintingSpec

    class Floating(
        val initialState: BpeSelectionState.Floating,
        val cutActions: GraphicsActionPair? = null,
        val initialX: Int,
        val initialY: Int,
        var lastOffsetX: Int = 0,
        var lastOffsetY: Int = 0,
        var lastState: BpeSelectionState.Floating,
    ) : PaintingSpec
}

private interface PaintingToolDescriptor {
    fun getCell(canvasType: CanvasType, palette: MutablePalette): Cell
    fun createShapePainter(layerUid: LayerUid): (Shape<Cell>) -> GraphicsAction
}

private interface PaintingShapeDescriptor {
    fun createPaintingSpec(
        canvasType: CanvasType,
        cell: Cell,
        shapePainter: (Shape<Cell>) -> GraphicsAction,
        drawingX: Int,
        drawingY: Int,
    ): PaintingSpec
}
