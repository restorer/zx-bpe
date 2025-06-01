package com.eightsines.bpe.middlware

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.Rect
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.foundation.Selection
import com.eightsines.bpe.graphics.GraphicsActionPair

data class BpeState(
    val background: LayerView<BackgroundLayer>,
    val canvas: CanvasView<SciiCanvas>,
    val drawingType: CanvasType?,

    val paletteInk: SciiColor,
    val palettePaper: SciiColor?,
    val paletteBright: SciiLight?,
    val paletteFlash: SciiLight?,
    val paletteChar: SciiChar?,

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

enum class BpeShape(val value: Int) {
    Point(1),
    Line(2),
    FillBox(3),
    StrokeBox(4),
    FillEllipse(5),
    StrokeEllipse(6);

    companion object {
        fun of(value: Int) = when (value) {
            Point.value -> Point
            Line.value -> Line
            FillBox.value -> FillBox
            StrokeBox.value -> StrokeBox
            FillEllipse.value -> FillEllipse
            StrokeEllipse.value -> StrokeEllipse
            else -> throw IllegalArgumentException("Unknown enum value=$value for BpeShape")
        }
    }
}

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

enum class BpePaintingMode(val value: Int) {
    Edge(1),
    Center(2);

    companion object {
        fun of(value: Int) = when (value) {
            Edge.value -> Edge
            Center.value -> Center
            else -> throw IllegalArgumentException("Unknown enum value=$value for BpePaintingMode")
        }
    }
}

data class BpeInformer(
    val canvasType: CanvasType,
    val rect: Rect,
)
