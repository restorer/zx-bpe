package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.Selection
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.state.BackgroundLayerView
import com.eightsines.bpe.state.CanvasLayerView
import com.eightsines.bpe.state.CanvasView

class BpeEngine(
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

    private var selection: Selection? = null
    private var clipboard: Crate<*>? = null

    private var history: MutableList<HistoryStep> = mutableListOf()
    private var historyPosition: Int = 0

    // private val revertFloatingAction: GraphicsAction? = null

    var state: BpeState = refreshState(null)
        private set

    fun execute(action: BpeAction) {
        when (action) {
            is BpeAction.PaletteSetInk -> executePaletteSetInk(action)
            is BpeAction.PaletteSetPaper -> executePaletteSetPaper(action)
            is BpeAction.PaletteSetBright -> executePaletteSetBright(action)
            else -> Unit
        }
    }

    private fun executePaletteSetInk(action: BpeAction.PaletteSetInk) {
        if (getCurrentCanvasLayer(state) == null) {
            tryExecuteGraphicsAction(GraphicsAction.SetBackgroundColor(action.color))
            return
        }

        paletteInk = action.color
        state = refreshState(state)
    }

    private fun executePaletteSetPaper(action: BpeAction.PaletteSetPaper) {
        if (getCurrentCanvasLayer(state) == null) {
            tryExecuteGraphicsAction(GraphicsAction.SetBackgroundBorder(action.color))
            return
        }

        palettePaper = action.color
        state = refreshState(state)
    }

    private fun executePaletteSetBright(action: BpeAction.PaletteSetBright) {
        if (getCurrentCanvasLayer(state) == null) {
            tryExecuteGraphicsAction(GraphicsAction.SetBackgroundBright(action.light))
            return
        }

        paletteBright = action.light
        state = refreshState(state)
    }

    private fun tryExecuteGraphicsAction(graphicsAction: GraphicsAction) {
        val undoAction = graphicsEngine.execute(graphicsAction)

        if (undoAction != null) {
            historyAppend(HistoryAction.Graphics(graphicsAction), HistoryAction.Graphics(undoAction))
            state = refreshState(state)
        }
    }

    private fun historyAppend(performAction: HistoryAction, undoAction: HistoryAction) {
        val step = HistoryStep(performAction, undoAction)

        if (historyPosition == historyMaxSteps) {
            history = history.subList(1, historyPosition).also { it.add(step) }
        } else if (historyPosition == history.size) {
            history.add(step)
        } else {
            history = history.subList(0, historyPosition).also { it.add(step) }
        }

        historyPosition = history.size
    }

    private fun getCurrentCanvasLayer(state: BpeState?): CanvasLayer<*>? =
        if (state?.layersCurrentUid == null || state.layersCurrentUid == LayerUid.Background) {
            null
        } else {
            graphicsEngine.state.canvasLayersMap[state.layersCurrentUid.value]
        }

    private fun refreshState(state: BpeState?): BpeState {
        val graphicsState = graphicsEngine.state
        val currentCanvasLayer = getCurrentCanvasLayer(state)
        val backgroundLayerView = BackgroundLayerView(graphicsState.backgroundLayer)

        return BpeState(
            background = backgroundLayerView,
            canvas = CanvasView(graphicsState.preview),
            drawingType = currentCanvasLayer?.canvas?.type,

            paletteInk = if (currentCanvasLayer == null) {
                graphicsState.backgroundLayer.color
            } else {
                paletteInk
            },

            palettePaper = when {
                currentCanvasLayer == null -> graphicsState.backgroundLayer.border
                currentCanvasLayer.canvas.type == CanvasType.Scii -> palettePaper
                else -> null
            },

            paletteBright = if (currentCanvasLayer == null) graphicsState.backgroundLayer.bright else paletteBright,
            paletteFlash = if (currentCanvasLayer?.canvas?.type == CanvasType.Scii) paletteFlash else null,
            paletteChar = if (currentCanvasLayer?.canvas?.type == CanvasType.Scii) paletteChar else null,

            layers = graphicsState.canvasLayers.reversed().map(::CanvasLayerView) + listOf(backgroundLayerView),
            layersCurrentUid = currentCanvasLayer?.uid ?: LayerUid.Background,

            toolboxTool = if (currentCanvasLayer == null) null else toolboxTool,

            toolboxShape = when {
                currentCanvasLayer == null -> null
                toolboxTool == BpeTool.Paint -> toolboxPaintShape
                toolboxTool == BpeTool.Erase -> toolboxEraseShape
                else -> null
            },

            toolboxCanSelect = currentCanvasLayer != null,
            toolboxCanPaste = clipboard != null,
            toolboxCanUndo = historyPosition > 0,
            toolboxCanRedo = historyPosition < history.size,

            selection = selection,
            selectionCanCut = false,
            selectionCanCopy = false,
            selectionCanFloat = false,
            selectionCanAnchor = false,
            selectionIsFloating = false,
        )
    }
}
