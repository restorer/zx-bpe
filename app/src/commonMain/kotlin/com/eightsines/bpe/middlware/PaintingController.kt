package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
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
    private var currentPaintingSpec: PaintingSpec? = null
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

        if (pendingHistoryStep.undoActions.isNotEmpty()) {
            historyActionsPerformer(pendingHistoryStep.undoActions)
        }

        pendingHistoryStep = HistoryStep.Empty
        return shouldRefresh
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
    ): Boolean {
        val shouldRefresh = cancel(historyActionsPerformer)

        return when (tool) {
            BpeTool.None -> false
            BpeTool.Paint -> executeStartPaint(tool, paintShape, currentLayer, palette, drawingX, drawingY)
            BpeTool.Erase -> executeStartPaint(tool, eraseShape, currentLayer, palette, drawingX, drawingY)
            BpeTool.Select -> executeStartSelect(currentLayer, drawingX, drawingY)
            BpeTool.PickColor -> executePickColor(currentLayer, palette, drawingX, drawingY)
        } || shouldRefresh
    }

    fun update(drawingX: Int, drawingY: Int): Boolean = when (val spec = currentPaintingSpec) {
        null -> false
        is PaintingSpec.Single -> executeUpdateSingle(spec, drawingX, drawingY)
        is PaintingSpec.Multiple -> executeUpdateMultiple(spec, drawingX, drawingY)
        is PaintingSpec.Selection -> executeUpdateSelection(spec, drawingX, drawingY)
        is PaintingSpec.Floating -> executeUpdateFloating(spec, drawingX, drawingY)
    }

    fun finish(drawingX: Int, drawingY: Int): PaintingFinishResult {
        val shouldRefresh = update(drawingX, drawingY)

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
            shouldRefresh = shouldRefresh,
            historyStep = pendingHistoryStep.merge(historyStep),
        )

        currentPaintingSpec = null
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
    ): Boolean {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return false
        val toolDescriptor = TOOL_DESCRIPTORS[tool] ?: return false
        val shapeDescriptor = SHAPE_DESCRIPTORS[shape] ?: return false

        val deselectResult = selectionController.deselect()
        pendingHistoryStep = deselectResult.historyStep

        currentPaintingSpec = shapeDescriptor.createPaintingSpec(
            cell = toolDescriptor.getCell(currentCanvasLayer.canvasType, palette),
            shapePainter = toolDescriptor.createShapePainter(currentCanvasLayer.uid),
            drawingX = drawingX,
            drawingY = drawingY,
        )

        return update(drawingX, drawingY) || deselectResult.shouldRefresh
    }

    private fun executeStartSelect(currentLayer: Layer, drawingX: Int, drawingY: Int): Boolean {
        val currentCanvasLayer = currentLayer as? CanvasLayer<*> ?: return false

        val (paintingSpec, shouldRefresh) = executeStartSelectFloatMove(drawingX, drawingY)
            ?: executeStartSelectFloatUp(currentCanvasLayer, drawingX, drawingY)
            ?: executeStartSelectSelection(currentCanvasLayer, drawingX, drawingY)

        currentPaintingSpec = paintingSpec
        return update(drawingX, drawingY) || shouldRefresh
    }

    private fun executeStartSelectFloatMove(drawingX: Int, drawingY: Int) =
        selectionController.ongoingFloatMove(drawingX, drawingY)?.let {
            PaintingSpec.Floating(
                initialState = it,
                startX = drawingX,
                startY = drawingY,
                lastState = it,
            ) to false
        }

    private fun executeStartSelectFloatUp(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int) =
        selectionController.ongoingFloatUp(currentCanvasLayer, drawingX, drawingY)?.let {
            PaintingSpec.Floating(
                initialState = it.floatingState,
                cutActions = it.cutActions,
                startX = drawingX,
                startY = drawingY,
                lastState = it.floatingState,
            ) to true
        }

    private fun executeStartSelectSelection(currentCanvasLayer: CanvasLayer<*>, drawingX: Int, drawingY: Int): Pair<PaintingSpec, Boolean> {
        val initialState = selectionController.selectionState
        val deselectResult = selectionController.deselect()

        pendingHistoryStep = deselectResult.historyStep

        return PaintingSpec.Selection(
            initialState = initialState as? BpeSelectionState.Selected,
            canvasType = currentCanvasLayer.canvasType,
            startY = drawingY,
            startX = drawingX,
        ) to deselectResult.shouldRefresh
    }

    private fun executePickColor(currentLayer: Layer, palette: MutablePalette, drawingX: Int, drawingY: Int) = when (currentLayer) {
        is BackgroundLayer -> {
            palette.ink = currentLayer.color
            true
        }

        is CanvasLayer<*> -> when (val cell = currentLayer.canvas.getDrawingCell(drawingX, drawingY)) {
            is SciiCell -> {
                palette.ink = cell.ink
                palette.paper = cell.paper
                palette.bright = cell.bright
                palette.flash = cell.flash
                palette.character = cell.character

                true
            }

            is BlockCell -> {
                palette.ink = cell.color
                palette.bright = cell.bright
                true
            }
        }

        else -> false
    }

    private fun executeUpdateSingle(spec: PaintingSpec.Single, drawingX: Int, drawingY: Int) =
        if (spec.lastX != drawingX || spec.lastY != drawingY) {
            spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
            spec.lastX = drawingX
            spec.lastY = drawingY
            spec.paintActions = graphicsEngine.executePair(spec.painter(spec.startX, spec.startY, drawingX, drawingY))
            true
        } else {
            false
        }

    private fun executeUpdateMultiple(spec: PaintingSpec.Multiple, drawingX: Int, drawingY: Int): Boolean {
        val point = drawingX to drawingY

        if (spec.points.contains(point)) {
            return false
        }

        spec.paintActions?.let { graphicsEngine.execute(it.undoAction) }
        spec.points.add(point)
        spec.paintActions = graphicsEngine.executePair(spec.painter(spec.points.toList()))

        return true
    }

    private fun executeUpdateSelection(spec: PaintingSpec.Selection, drawingX: Int, drawingY: Int) =
        selectionController.ongoingSelectedUpdate(Selection(spec.canvasType, Box.of(spec.startX, spec.startY, drawingX, drawingY)))

    private fun executeUpdateFloating(spec: PaintingSpec.Floating, drawingX: Int, drawingY: Int): Boolean {
        val offsetX = drawingX - spec.startX
        val offsetY = drawingY - spec.startY

        if (offsetX == spec.lastOffsetX && offsetY == spec.lastOffsetY) {
            return false
        }

        val newState = selectionController.ongoingFloatingUpdate(spec.initialState, offsetX, offsetY) ?: return false

        spec.lastOffsetX = offsetX
        spec.lastOffsetY = offsetY
        spec.lastState = newState

        return true
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
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        painter = { startX, startY, endX, endY -> shapePainter(Shape.Line(startX, startY, endX, endY, cell)) },
                        startX = drawingX,
                        startY = drawingY,
                    )
                }
            )

            put(
                BpeShape.FillBox,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        painter = { startX, startY, endX, endY -> shapePainter(Shape.FillBox(startX, startY, endX, endY, cell)) },
                        startX = drawingX,
                        startY = drawingY,
                    )
                }
            )

            put(
                BpeShape.StrokeBox,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        painter = { startX, startY, endX, endY -> shapePainter(Shape.StrokeBox(startX, startY, endX, endY, cell)) },
                        startX = drawingX,
                        startY = drawingY,
                    )
                }
            )

            put(
                BpeShape.FillEllipse,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        painter = { startX, startY, endX, endY -> shapePainter(Shape.FillEllipse(startX, startY, endX, endY, cell)) },
                        startX = drawingX,
                        startY = drawingY,
                    )
                }
            )

            put(
                BpeShape.StrokeEllipse,
                object : PaintingShapeDescriptor {
                    override fun createPaintingSpec(
                        cell: Cell,
                        shapePainter: (Shape<Cell>) -> GraphicsAction,
                        drawingX: Int,
                        drawingY: Int,
                    ) = PaintingSpec.Single(
                        painter = { startX, startY, endX, endY -> shapePainter(Shape.StrokeEllipse(startX, startY, endX, endY, cell)) },
                        startX = drawingX,
                        startY = drawingY,
                    )
                }
            )
        }
    }
}

data class PaintingFinishResult(
    val shouldRefresh: Boolean,
    val historyStep: HistoryStep,
)

private sealed interface PaintingSpec {
    class Single(
        val painter: (Int, Int, Int, Int) -> GraphicsAction,
        val startX: Int,
        val startY: Int,
        var paintActions: GraphicsActionPair? = null,
        var lastX: Int? = null,
        var lastY: Int? = null,
    ) : PaintingSpec

    class Multiple(
        val painter: (List<Pair<Int, Int>>) -> GraphicsAction,
        var points: LinkedHashSet<Pair<Int, Int>>,
        var paintActions: GraphicsActionPair? = null,
    ) : PaintingSpec

    class Selection(
        val initialState: BpeSelectionState.Selected?,
        val canvasType: CanvasType,
        val startX: Int,
        val startY: Int,
    ) : PaintingSpec

    class Floating(
        val initialState: BpeSelectionState.Floating,
        val cutActions: GraphicsActionPair? = null,
        val startX: Int,
        val startY: Int,
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
    fun createPaintingSpec(cell: Cell, shapePainter: (Shape<Cell>) -> GraphicsAction, drawingX: Int, drawingY: Int): PaintingSpec
}
