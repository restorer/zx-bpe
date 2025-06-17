package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Cell
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.Rect
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.graphics.GraphicsActionPair

data class BpeState(
    val background: LayerView<BackgroundLayer>,
    val canvas: CanvasView<SciiCanvas>,
    val drawingType: CanvasType?,

    val paletteBackgroundBorder: SciiColor?,
    val paletteBackgroundPaper: SciiColor?,
    val paletteBackgroundBright: SciiLight?,
    val palettePaintSciiInk: SciiColor?,
    val palettePaintSciiPaper: SciiColor?,
    val palettePaintSciiBright: SciiLight?,
    val palettePaintSciiFlash: SciiLight?,
    val palettePaintSciiChar: SciiChar?,
    val palettePaintBlockColor: SciiColor?,
    val palettePaintBlockBright: SciiLight?,

    val paletteEraseSciiInk: Boolean?,
    val paletteEraseSciiPaper: Boolean?,
    val paletteEraseSciiBright: Boolean?,
    val paletteEraseSciiFlash: Boolean?,
    val paletteEraseSciiChar: Boolean?,
    val paletteEraseBlockColor: Boolean?,
    val paletteEraseBlockBright: Boolean?,

    val layers: List<LayerView<*>>,
    val layersCurrentUid: LayerUid,
    val layersCanMoveUp: Boolean,
    val layersCanMoveDown: Boolean,
    val layersCanDelete: Boolean,
    val layersCanMerge: Boolean,
    val layersCanConvert: Boolean,

    val toolboxTool: BpeTool,
    val toolboxShape: BpeShape?,
    val toolboxAvailTools: Set<BpeTool>,
    val toolboxCanSelect: Boolean,
    val toolboxCanPaste: Boolean,
    val toolboxCanUndo: Boolean,
    val toolboxCanRedo: Boolean,

    val selection: Selection?,
    val selectionIsActionable: Boolean,
    val selectionIsFloating: Boolean,

    val paintingMode: BpePaintingMode,
    val informer: BpeInformer?,
    val historySteps: Int,
)

enum class BpeTool(val value: Int) {
    None(1),
    Paint(2),
    Erase(3),
    Select(4),
    PickColor(5);

    companion object {
        fun of(value: Int) = when (value) {
            None.value -> None
            Paint.value -> Paint
            Erase.value -> Erase
            Select.value -> Select
            PickColor.value -> PickColor
            else -> throw IllegalArgumentException("Unknown enum value=$value for BpeTool")
        }
    }
}

@BagStuff(isPolymorphic = true)
sealed interface BpeSelectionState {
    @BagStuff(packer = "_", unpacker = "_", polymorphicOf = BpeSelectionState::class, polymorphicId = 1)
    data object None : BpeSelectionState

    @BagStuff(polymorphicOf = BpeSelectionState::class, polymorphicId = 2)
    data class Selected(@BagStuffWare(1) val selection: Selection) : BpeSelectionState

    @BagStuff(polymorphicOf = BpeSelectionState::class, polymorphicId = 3)
    data class Floating(
        @BagStuffWare(1) val selection: Selection,
        @BagStuffWare(2) val layerUid: LayerUid,
        @BagStuffWare(3) val crate: Crate<Cell>,
        @BagStuffWare(4) val overlayActions: GraphicsActionPair,
    ) : BpeSelectionState
}

@BagStuff
data class BpeClipboard(
    @BagStuffWare(1) val drawingX: Int,
    @BagStuffWare(2) val drawingY: Int,
    @BagStuffWare(3) val crate: Crate<*>,
)

data class BpeInformer(
    val canvasType: CanvasType,
    val rect: Rect,
)
