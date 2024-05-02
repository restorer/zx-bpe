package com.eightsines.bpe.engine

import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight
import com.eightsines.bpe.engine.layer.BackgroundLayer
import com.eightsines.bpe.engine.layer.Layer

data class EngineState(
    val currentInk: SciiColor,
    val currentPaper: SciiColor,
    val currentBright: SciiLight,
    val currentFlash: SciiLight,
    val currentCharacter: SciiChar,

    val border: SciiColor,
    val isBorderVisible: Boolean = true,
    val background: BackgroundLayer,

    val layers: List<Layer<*>> = emptyList(),
    val currentLayer: Layer<*>? = null,

    val history: List<HistoryItem> = emptyList(),
    val historyPosition: Int = 0,

    val preview: Canvas.Scii,
)
