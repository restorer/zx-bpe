package com.eightsines.bpe.engine

import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.canvas.ImmutableCanvas
import com.eightsines.bpe.engine.canvas.MutableCanvas
import com.eightsines.bpe.engine.layer.BackgroundLayer
import com.eightsines.bpe.engine.layer.Layer

class Engine(private val defaults: Defaults) {
    var state: EngineState = createInitialState()
        private set

    fun process(action: EngineAction) {
        state = when (action) {
            is EngineAction.SetCurrentInk -> state.copy(currentInk = action.ink)
            is EngineAction.SetCurrentPaper -> state.copy(currentPaper = action.paper)
            is EngineAction.SetCurrentBright -> state.copy(currentBright = action.bright)
            is EngineAction.SetCurrentFlash -> state.copy(currentFlash = action.flash)
            is EngineAction.SetCurrentCharacter -> state.copy(currentCharacter = action.character)

            is EngineAction.PickCurrentInk -> {
                state.copy(
                    currentInk = when (val canvas = state.currentLayer?.canvas) {
                        is Canvas.Scii -> canvas.getDrawingCell(action.drawingX, action.drawingY).ink
                        is Canvas.HBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                        is Canvas.VBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                        is Canvas.QBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                        null -> state.currentInk
                    }
                )
            }

            is EngineAction.PickCurrentPaper -> state.copy(
                currentInk = when (val canvas = state.currentLayer?.canvas) {
                    is Canvas.Scii -> canvas.getDrawingCell(action.drawingX, action.drawingY).ink
                    is Canvas.HBlock, Canvas.VBlock, Canvas.QBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                    is Canvas.VBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                    is Canvas.QBlock -> canvas.getDrawingCell(action.drawingX, action.drawingY).color
                    else -> state.currentInk
                }
            )
        }
    }

    private fun createInitialState(): EngineState {
        val background = BackgroundLayer(
            isVisible = true,
            color = defaults.backgroundColor,
            bright = defaults.backgroundBright,
        )

        return EngineState(
            currentInk = defaults.currentInk,
            currentPaper = defaults.currentPaper,
            currentBright = defaults.currentBright,
            currentFlash = defaults.currentFlash,
            currentCharacter = defaults.currentCharacter,
            border = defaults.border,
            background = background,
            preview = createPreview(background, emptyList()),
        )
    }

    private fun createPreview(background: BackgroundLayer, layers: List<Layer<*>>): Canvas.Scii {
        TODO()
    }
}
