package com.eightsines.bpe.presentation

import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.util.TextDescriptor
import com.eightsines.bpe.util.TextRes

data class UiState(
    val sheet: UiSheetView,
    val areas: List<UiArea>,

    val paletteColor: UiToolState<SciiColor>,
    val palettePaper: UiToolState<SciiColor>,
    val paletteInk: UiToolState<SciiColor>,
    val paletteBright: UiToolState<SciiLight>,
    val paletteFlash: UiToolState<SciiLight>,
    val paletteChar: UiToolState<SciiChar>,

    val palettePaperHint: TextRes,
    val paletteInkHint: TextRes,

    val selectionPaste: UiToolState<Unit>,
    val selectionMenu: UiToolState<Unit>,
    val layers: UiToolState<Unit>,

    val toolboxPaint: UiToolState<Unit>,
    val toolboxShape: UiToolState<BpeShape>,
    val toolboxErase: UiToolState<Unit>,
    val toolboxSelect: UiToolState<Unit>,
    val toolboxPickColor: UiToolState<Unit>,

    val toolboxUndo: UiToolState<Unit>,
    val toolboxRedo: UiToolState<Unit>,
    val toolboxMode: BpePaintingMode,
    val menu: UiToolState<Unit>,

    val activePanel: UiPanel?,

    val layersItems: List<LayerView<*>>,
    val layersCurrentUid: LayerUid,
    val layersCreate: UiToolState<Unit>,
    val layersCreateCancel: UiToolState<Unit>,
    val layersMerge: UiToolState<Unit>,
    val layersConvert: UiToolState<Unit>,
    val layersConvertCancel: UiToolState<Unit>,
    val layersDelete: UiToolState<Unit>,
    val layersMoveUp: UiToolState<Unit>,
    val layersMoveDown: UiToolState<Unit>,
    val layersTypesIsVisible: Boolean,

    val informerPrimary: TextDescriptor?,
    val informerSecondary: TextDescriptor?,

    val historySteps: Int,
)

enum class UiAreaType {
    Selection,
    SecondaryCursor,
    PrimaryCursor,
}

data class UiArea(val pointerX: Int, val pointerY: Int, val pointerWidth: Int, val pointerHeight: Int, val type: UiAreaType)

sealed interface UiToolState<out T> {
    val isInteractable: Boolean

    data object Hidden : UiToolState<Nothing> {
        override val isInteractable = false
    }

    data class Disabled<T>(val value: T, val title: TextRes? = null) : UiToolState<T> {
        override val isInteractable = false
        override fun toString() = "Disabled(${if (value == Unit) "" else value.toString()})"
    }

    data class Visible<T>(val value: T, val title: TextRes? = null) : UiToolState<T> {
        override val isInteractable = true
        override fun toString() = "Visible(${if (value == Unit) "" else value.toString()})"
    }

    data class Active<T>(val value: T, val title: TextRes? = null) : UiToolState<T> {
        override val isInteractable = true
        override fun toString() = "Active(${if (value == Unit) "" else value.toString()})"
    }
}

sealed interface UiPanel {
    data class Colors(val color: SciiColor) : UiPanel
    data class Lights(val light: SciiLight) : UiPanel
    data class Chars(val character: SciiChar) : UiPanel
    data class Erase(val shouldErase: Boolean) : UiPanel
    data object SelectionMenu : UiPanel
    data object Layers : UiPanel
    data class Shapes(val shape: BpeShape) : UiPanel
    data object Menu : UiPanel
}
