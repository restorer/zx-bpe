package com.eightsines.bpe.presentation

import com.eightsines.bpe.engine.BpeShape
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.state.LayerView

data class UiState(
    val paletteColor: UiToolState<SciiColor>,
    val paletteInk: UiToolState<SciiColor>,
    val palettePaper: UiToolState<SciiColor>,
    val paletteBright: UiToolState<SciiLight>,
    val paletteFlash: UiToolState<SciiLight>,
    val paletteChar: UiToolState<SciiChar>,

    val selectionCut: UiToolState<Unit>,
    val selectionCopy: UiToolState<Unit>,
    val layers: UiToolState<Unit>,

    val toolboxPaint: UiToolState<Unit>,
    val toolboxShape: UiToolState<BpeShape>,
    val toolboxErase: UiToolState<Unit>,
    val toolboxSelect: UiToolState<Unit>,
    val toolboxPickColor: UiToolState<Unit>,

    val toolboxPaste: UiToolState<Unit>,
    val toolboxUndo: UiToolState<Unit>,
    val toolboxRedo: UiToolState<Unit>,
    val menu: UiToolState<Unit>,

    val activePanel: UiPanel?,

    val layersItems: List<LayerView<*>>,
    val layersCurrentUid: LayerUid,
    val layersCreate: UiToolState<Unit>,
    val layersMerge: UiToolState<Unit>,
    val layersConvert: UiToolState<Unit>,
    val layersDelete: UiToolState<Unit>,
    val layersMoveUp: UiToolState<Unit>,
    val layersMoveDown: UiToolState<Unit>,
    val layersTypesIsVisible: Boolean,
)

sealed interface UiToolState<out T> {
    val isInteractable: Boolean

    data object Hidden : UiToolState<Nothing> {
        override val isInteractable = false
    }

    data class Disabled<T>(val value: T) : UiToolState<T> {
        override val isInteractable = false
    }

    data class Visible<T>(val value: T) : UiToolState<T> {
        override val isInteractable = true
    }

    data class Active<T>(val value: T) : UiToolState<T> {
        override val isInteractable = true
    }
}

enum class UiPanel {
    Colors,
    Lights,
    Chars,
    Layers,
    Shapes,
    Menu,
}
