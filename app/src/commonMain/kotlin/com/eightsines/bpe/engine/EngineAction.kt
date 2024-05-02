package com.eightsines.bpe.engine

import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

sealed interface EngineAction {
    data class SetCurrentInk(val ink: SciiColor) : EngineAction
    data class SetCurrentPaper(val paper: SciiColor) : EngineAction
    data class SetCurrentBright(val bright: SciiLight) : EngineAction
    data class SetCurrentFlash(val flash: SciiLight) : EngineAction
    data class SetCurrentCharacter(val character: SciiChar) : EngineAction

    data class PickCurrentInk(val drawingX: Int, val drawingY: Int) : EngineAction
    data class PickCurrentPaper(val drawingX: Int, val drawingY: Int) : EngineAction
    data class PickCurrentBright(val drawingX: Int, val drawingY: Int) : EngineAction
    data class PickCurrentFlash(val drawingX: Int, val drawingY: Int) : EngineAction
    data class PickCurrentCharacter(val drawingX: Int, val drawingY: Int) : EngineAction

    data class SetCurrentLayer(val layerUuid: String) : EngineAction
    data class DrawCurrent(val drawingX: Int, val drawingY: Int)

    data class Historical(val action: HistoricalAction) : EngineAction
    data object Undo : EngineAction
    data object Redo : EngineAction
}
