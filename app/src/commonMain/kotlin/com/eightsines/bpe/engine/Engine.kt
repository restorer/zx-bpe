package com.eightsines.bpe.engine

/*
import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.canvas.MutableCanvas
import com.eightsines.bpe.engine.layer.MutableLayer

class Engine(private val uidFactory: UidFactory, toolState: EngineState.Tool, graphicsState: EngineState.Graphics) {
    private val mutableLayers = mutableMapOf<String, MutableLayer<*>>()
    private val mutablePreview = MutableCanvas.Scii(Canvas.SCREEN_SCII_WIDTH, Canvas.SCREEN_SCII_HEIGHT)

    var state: EngineState = initialize(toolState, graphicsState)
        private set

    fun process(action: EngineAction) {
        state = processAction(state, action)
    }

    private fun processAction(state: EngineState, action: EngineAction): EngineState = when (action) {
        is EngineAction.Tool -> state.copy(tool = processToolAction(state.tool, action))

        is EngineAction.Graphics -> processGraphicsAction(state.graphics, action)
            ?.let { processGraphicsResult(state, action, it) }
            ?: state

        is EngineAction.Composite -> processCompositeAction(state, action)

        is EngineAction.Undo -> if (state.historyPosition >= 0) {
            val result = processGraphicsAction(state.graphics, state.history[state.historyPosition].revertAction)
            result?.updatePreview?.let { updatePreview(result.state, it) }

            state.copy(
                graphics = result?.state ?: state.graphics,
                historyPosition = state.historyPosition - 1,
            )
        } else {
            state
        }

        is EngineAction.Redo -> if (state.historyPosition < state.history.size) {
            val result = processGraphicsAction(state.graphics, state.history[state.historyPosition].applyAction)
            result?.updatePreview?.let { updatePreview(result.state, it) }

            state.copy(
                graphics = result?.state ?: state.graphics,
                historyPosition = state.historyPosition + 1,
            )
        } else {
            state
        }
    }

    private fun processGraphicsResult(
        state: EngineState,
        action: EngineAction.Graphics,
        result: GraphicsResult,
    ): EngineState {
        result.updatePreview?.let { updatePreview(result.state, it) }
        val trimmedHistory = state.history.subList(0, state.historyPosition + 1)

        return state.copy(
            graphics = result.state,
            history = trimmedHistory + listOf(EngineState.Step(action, result.undoAction)),
            historyPosition = trimmedHistory.size,
            preview = mutablePreview.toImmutable(),
        )
    }

    private fun processToolAction(state: EngineState.Tool, action: EngineAction.Tool): EngineState.Tool =
        when (action) {
            is EngineAction.Tool.SetInk -> state.copy(ink = action.color)
            is EngineAction.Tool.SetPaper -> state.copy(paper = action.color)
            is EngineAction.Tool.SetBright -> state.copy(bright = action.light)
            is EngineAction.Tool.SetFlash -> state.copy(flash = action.light)
            is EngineAction.Tool.SetCharacter -> state.copy(character = action.character)

            is EngineAction.Tool.PickInk -> when (val canvas = mutableLayers[state.layerUid]?.canvas) {
                is Canvas.CanvasScii -> state.copy(
                    ink = canvas.getDrawingCell(action.drawingX, action.drawingY).ink,
                )

                is Canvas.Block -> state.copy(
                    ink = canvas.getDrawingCell(action.drawingX, action.drawingY).color,
                )

                else -> state
            }

            is EngineAction.Tool.PickPaper -> when (val canvas = mutableLayers[state.layerUid]?.canvas) {
                is Canvas.CanvasScii -> state.copy(
                    paper = canvas.getDrawingCell(action.drawingX, action.drawingY).paper,
                )

                else -> state
            }

            is EngineAction.Tool.PickBright -> when (val canvas = mutableLayers[state.layerUid]?.canvas) {
                is Canvas.CanvasScii -> state.copy(
                    bright = canvas.getDrawingCell(action.drawingX, action.drawingY).bright,
                )

                is Canvas.Block -> state.copy(
                    bright = canvas.getDrawingCell(action.drawingX, action.drawingY).bright,
                )

                else -> state
            }

            is EngineAction.Tool.PickFlash -> when (val canvas = mutableLayers[state.layerUid]?.canvas) {
                is Canvas.CanvasScii -> state.copy(
                    flash = canvas.getDrawingCell(action.drawingX, action.drawingY).flash,
                )

                else -> state
            }

            is EngineAction.Tool.PickCharacter -> when (val canvas = mutableLayers[state.layerUid]?.canvas) {
                is Canvas.CanvasScii -> state.copy(
                    character = canvas.getDrawingCell(action.drawingX, action.drawingY).character,
                )

                else -> state
            }

            is EngineAction.Tool.SelectLayer -> when (val layer = mutableLayers[action.layerUid]) {
                null -> state
                else -> state.copy(layerUid = layer.uid)
            }
        }

    private fun processGraphicsAction(
        state: EngineState.Graphics,
        action: EngineAction.Graphics
    ): GraphicsResult? = when (action) {
        is EngineAction.Graphics.SetBorder -> if (state.border != action.color) {
            GraphicsResult(
                state = state.copy(border = action.color),
                undoAction = EngineAction.Graphics.SetBorder(state.border),
                updatePreview = null,
            )
        } else {
            null
        }

        is EngineAction.Graphics.SetBorderVisible -> if (state.isBorderVisible != action.isVisible) {
            GraphicsResult(
                state = state.copy(isBorderVisible = action.isVisible),
                undoAction = EngineAction.Graphics.SetBorderVisible(state.isBorderVisible),
                updatePreview = null,
            )
        } else {
            null
        }

        is EngineAction.Graphics.SetBackgroundColor -> if (state.background.color != action.color) {
            GraphicsResult(
                state = state.copy(background = state.background.copy(color = action.color)),
                undoAction = EngineAction.Graphics.SetBackgroundColor(state.background.color),
                updatePreview = UpdatePreview.Full,
            )
        } else {
            null
        }

        is EngineAction.Graphics.SetBackgroundBright -> if (state.background.bright != action.light) {
            GraphicsResult(
                state = state.copy(background = state.background.copy(bright = action.light)),
                undoAction = EngineAction.Graphics.SetBackgroundBright(state.background.bright),
                updatePreview = UpdatePreview.Full,
            )
        } else {
            null
        }

        is EngineAction.Graphics.SetBackgroundVisible -> if (state.background.isVisible != action.isVisible) {
            GraphicsResult(
                state = state.copy(background = state.background.copy(isVisible = action.isVisible)),
                undoAction = EngineAction.Graphics.SetBackgroundVisible(state.background.isVisible),
                updatePreview = UpdatePreview.Full,
            )
        } else {
            null
        }

        is EngineAction.Graphics.CreateNewLayer -> {
            val layer = MutableLayer(
                uid = uidFactory.createUid(),
                canvas = when (action.layerType) {
                    LayerType.Scii -> MutableCanvas.Scii(Canvas.SCREEN_SCII_WIDTH, Canvas.SCREEN_SCII_HEIGHT)
                    LayerType.HBlock -> MutableCanvas.HBlock(Canvas.SCREEN_SCII_WIDTH, Canvas.SCREEN_SCII_HEIGHT)
                    LayerType.VBlock -> MutableCanvas.VBlock(Canvas.SCREEN_SCII_WIDTH, Canvas.SCREEN_SCII_HEIGHT)
                    LayerType.QBlock -> MutableCanvas.QBlock(Canvas.SCREEN_SCII_WIDTH, Canvas.SCREEN_SCII_HEIGHT)
                }
            )

            mutableLayers[layer.uid] = layer

            val splitIndex = if (action.onTopOfLayerUid == null) {
                0
            } else {
                state.layers.indexOfFirst { it.uid == action.onTopOfLayerUid } + 1
            }

            GraphicsResult(
                state = state.copy(
                    layers = state.layers.subList(0, splitIndex) +
                            listOf(layer.toImmutable()) +
                            state.layers.subList(splitIndex, state.layers.size)
                ),
                undoAction = EngineAction.Graphics.DeleteLayerInternal(layerUid = layer.uid),
                updatePreview = null,
            )
        }

        is EngineAction.Graphics.DeleteLayerInternal -> mutableLayers[action.layerUid]?.let { layer ->
            mutableLayers.remove(layer.uid)
            val prevIndex = state.layers.indexOfFirst { it.uid == layer.uid } - 1

            GraphicsResult(
                state = state.copy(layers = state.layers.filterNot { it.uid == layer.uid }),
                undoAction = EngineAction.Graphics.UndoLayerInternal(
                    layer = layer.toImmutable(),
                    onTopOfLayerUid = if (prevIndex >= 0) state.layers[prevIndex].uid else null,
                ),
                updatePreview = UpdatePreview.Full,
            )
        }

        is EngineAction.Graphics.SetLayerVisible -> mutableLayers[action.layerUid]?.let { layer ->
            val wasVisible = layer.isVisible
            layer.isVisible = action.isVisible

            GraphicsResult(
                state = state.copy(layers = state.layers.map { if (it.uid == layer.uid) layer.toImmutable() else it }),
                undoAction = EngineAction.Graphics.SetLayerVisible(layerUid = layer.uid, isVisible = wasVisible),
                updatePreview = UpdatePreview.Full,
            )
        }

        is EngineAction.Graphics.SetLayerLocked -> mutableLayers[action.layerUid]?.let { layer ->
            val wasLocked = layer.isLocked
            layer.isLocked = action.isLocked

            GraphicsResult(
                state = state.copy(layers = state.layers.map { if (it.uid == layer.uid) layer.toImmutable() else it }),
                undoAction = EngineAction.Graphics.SetLayerVisible(layerUid = layer.uid, isVisible = wasLocked),
                updatePreview = null,
            )
        }

        is EngineAction.Graphics.MoveLayer -> mutableLayers[action.layerUid]?.let { layer ->
            if (layer.uid != action.onTopOfLayerUid) {
                val prevIndex = state.layers.indexOfFirst { it.uid == action.layerUid } - 1
                val wasOnTopOfLayerUid = if (prevIndex >= 0) state.layers[prevIndex].uid else null
                val filteredLayers = state.layers.filterNot { it.uid == action.layerUid }

                val splitIndex = if (action.onTopOfLayerUid == null) {
                    0
                } else {
                    filteredLayers.indexOfFirst { it.uid == action.onTopOfLayerUid } + 1
                }

                GraphicsResult(
                    state = state.copy(
                        layers = filteredLayers.subList(0, splitIndex) +
                                listOf(layer.toImmutable()) +
                                filteredLayers.subList(splitIndex, filteredLayers.size)
                    ),
                    undoAction = EngineAction.Graphics.MoveLayer(action.layerUid, wasOnTopOfLayerUid),
                    updatePreview = UpdatePreview.Full,
                )
            } else {
                null
            }
        }

        is EngineAction.Graphics.DrawSciiInternal -> mutableLayers[action.layerUid]?.let { layer ->
            if (layer.canvas is MutableCanvas.Scii) {
                val (sciiX, sciiY) = layer.canvas.toSciiPosition(action.drawingX, action.drawingY)
                val wasSkiiCell = layer.canvas.getSciiCell(sciiX, sciiY)

                layer.canvas.putDrawingCell(action.drawingX, action.drawingY, action.cell)

                GraphicsResult(
                    state = state.copy(
                        layers = state.layers.map { if (it.uid == layer.uid) layer.toImmutable() else it },
                    ),
                    undoAction = EngineAction.Graphics.UndoSciiInternal(action.layerUid, sciiX, sciiY, wasSkiiCell),
                    updatePreview = UpdatePreview.Cell(sciiX, sciiY),
                )
            } else {
                null
            }
        }

        is EngineAction.Graphics.DrawBlockInternal -> mutableLayers[action.layerUid]?.let { layer ->
            if (layer.canvas is MutableCanvas.Block) {
                val (sciiX, sciiY) = layer.canvas.toSciiPosition(action.drawingX, action.drawingY)
                val wasSkiiCell = layer.canvas.getSciiCell(sciiX, sciiY)

                layer.canvas.putDrawingCell(action.drawingX, action.drawingY, action.cell)

                GraphicsResult(
                    state = state.copy(
                        layers = state.layers.map { if (it.uid == layer.uid) layer.toImmutable() else it },
                    ),
                    undoAction = EngineAction.Graphics.UndoSciiInternal(action.layerUid, sciiX, sciiY, wasSkiiCell),
                    updatePreview = UpdatePreview.Cell(sciiX, sciiY),
                )
            } else {
                null
            }
        }

        is EngineAction.Graphics.UndoSciiInternal -> mutableLayers[action.layerUid]?.let { layer ->
            val wasSkiiCell = layer.canvas.getSciiCell(action.sciiX, action.sciiY)
            layer.canvas.replaceSciiCell(action.sciiX, action.sciiY, action.cell)

            GraphicsResult(
                state = state.copy(
                    layers = state.layers.map { if (it.uid == layer.uid) layer.toImmutable() else it },
                ),
                undoAction = EngineAction.Graphics.UndoSciiInternal(action.layerUid, action.sciiX, action.sciiY, wasSkiiCell),
                updatePreview = UpdatePreview.Cell(action.sciiX, action.sciiY)
            )
        }

        is EngineAction.Graphics.UndoLayerInternal -> {
            mutableLayers[action.layer.uid] = action.layer.toMutable()

            val splitIndex = if (action.onTopOfLayerUid == null) {
                0
            } else {
                state.layers.indexOfFirst { it.uid == action.onTopOfLayerUid } + 1
            }

            GraphicsResult(
                state = state.copy(
                    layers = state.layers.subList(0, splitIndex) +
                            listOf(action.layer) +
                            state.layers.subList(splitIndex, state.layers.size)
                ),
                undoAction = EngineAction.Graphics.DeleteLayerInternal(layerUid = action.layer.uid),
                updatePreview = UpdatePreview.Full,
            )
        }
    }

    private fun processCompositeAction(state: EngineState, action: EngineAction.Composite): EngineState =
        when (action) {
            is EngineAction.Composite.DeleteLayer -> {
                val graphicsAction = EngineAction.Graphics.DeleteLayerInternal(action.layerUid)
                val graphicsResult = processGraphicsAction(state.graphics, graphicsAction)

                if (graphicsResult != null) {
                    processGraphicsResult(state, graphicsAction, graphicsResult)
                } else {
                    state
                }
            }

            is EngineAction.Composite.DeleteSelectedLayer -> TODO()
            is EngineAction.Composite.SetSelectedLayerVisible -> TODO()
            is EngineAction.Composite.SetSelectedLayerLocked -> TODO()
            is EngineAction.Composite.MoveSelectedLayer -> TODO()
            is EngineAction.Composite.DrawCurrent -> TODO()
        }

    private fun initialize(toolState: EngineState.Tool, graphicsState: EngineState.Graphics): EngineState {
        for (layer in graphicsState.layers) {
            mutableLayers[layer.uid] = layer.toMutable()
        }

        updatePreview(graphicsState, UpdatePreview.Full)

        return EngineState(
            tool = toolState,
            graphics = graphicsState,
            preview = mutablePreview.toImmutable(),
        )
    }

    private fun updatePreview(state: EngineState.Graphics, update: UpdatePreview) {
        TODO()
    }

    sealed interface UpdatePreview {
        data object Full : UpdatePreview
        data class Cell(val sciiX: Int, val sciiY: Int) : UpdatePreview
    }

    data class GraphicsResult(
        val state: EngineState.Graphics,
        val undoAction: EngineAction.Graphics,
        val updatePreview: UpdatePreview?,
    )
}
*/
