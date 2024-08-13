package com.eightsines.bpe.presentation

import com.eightsines.bpe.engine.BpeShape
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

data class UiState(
    val paletteColor: UiToolState<SciiColor>,
    val paletteInk: UiToolState<SciiColor>,
    val palettePaper: UiToolState<SciiColor>,
    val paletteBright: UiToolState<SciiLight>,
    val paletteFlash: UiToolState<SciiLight>,
    val paletteChar: UiToolState<SciiChar>,
    val paletteSelectionCut: UiToolState<Unit>,
    val paletteSelectionCopy: UiToolState<Unit>,
    val paletteLayers: UiToolState<Unit>,

    val toolboxPaint: UiToolState<Unit>,
    val toolboxShape: UiToolState<BpeShape>,
    val toolboxErase: UiToolState<Unit>,
    val toolboxSelect: UiToolState<Unit>,
    val toolboxPickColor: UiToolState<Unit>,
    val toolboxPaste: UiToolState<Unit>,
    val toolboxUndo: UiToolState<Unit>,
    val toolboxRedo: UiToolState<Unit>,
    val toolboxMenu: UiToolState<Unit>,

    val panel: UiPanel?,
)

sealed interface UiToolState<out T> {
    data object Hidden : UiToolState<Nothing>
    data class Disabled<T>(val value: T) : UiToolState<T>
    data class Visible<T>(val value: T) : UiToolState<T>
    data class Active<T>(val value: T) : UiToolState<T>
}

enum class UiPanel {
    Colors,
    Lights,
    Characters,
    Layers,
    Shapes,
    Menu,
}
