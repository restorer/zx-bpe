package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.Box
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
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.getStuffTyped
import com.eightsines.bpe.util.requireSupportedStuffVersion

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

enum class BpeSelectionStateType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out BpeSelectionState>?) {
    None(1, null),
    Selected(2, BpeSelectionState.Selected.Polymorphic),
    Floating(3, BpeSelectionState.Floating.Polymorphic),
}

sealed interface BpeSelectionState {
    val type: BpeSelectionStateType

    data object None : BpeSelectionState {
        override val type = BpeSelectionStateType.None
    }

    data class Selected(val selection: Selection) : BpeSelectionState {
        override val type = BpeSelectionStateType.Selected

        internal object Polymorphic : BagStuffPacker<Selected>, BagStuffUnpacker<Selected> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Selected) {
                bag.put(Selection, value.selection)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Selected {
                requireSupportedStuffVersion("BpeSelectionState.Selected", 1, version)
                val selection = bag.getStuff(Selection)
                return Selected(selection)
            }
        }
    }

    data class Floating(
        val selection: Selection,
        val layerUid: LayerUid,
        val crate: Crate<Cell>,
        val overlayActions: GraphicsActionPair,
    ) : BpeSelectionState {
        override val type = BpeSelectionStateType.Floating

        internal object Polymorphic : BagStuffPacker<Floating>, BagStuffUnpacker<Floating> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Floating) {
                bag.put(Selection, value.selection)
                bag.put(value.layerUid.value)
                bag.put(Crate, value.crate)
                bag.put(GraphicsActionPair, value.overlayActions)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Floating {
                requireSupportedStuffVersion("BpeSelectionState.Floating", 1, version)

                val selection = bag.getStuff(Selection)
                val layerUid = LayerUid(bag.getString())
                val crate: Crate<Cell> = bag.getStuffTyped(Crate)
                val overlayActions = bag.getStuff(GraphicsActionPair)

                return Floating(selection, layerUid, crate, overlayActions)
            }
        }
    }

    companion object : BagStuffPacker<BpeSelectionState>, BagStuffUnpacker<BpeSelectionState> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BpeSelectionState) {
            bag.put(value.type.value)
            value.type.polymorphicPacker?.let { bag.put(it, value) }
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BpeSelectionState {
            requireSupportedStuffVersion("BpeSelectionState", 1, version)

            return when (val type = bag.getInt()) {
                BpeSelectionStateType.None.value -> None
                BpeSelectionStateType.Selected.value -> bag.getStuff(Selected.Polymorphic)
                BpeSelectionStateType.Floating.value -> bag.getStuff(Floating.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("BpeSelectionState", type)
            }
        }
    }
}

data class BpeClipboard(val drawingX: Int, val drawingY: Int, val crate: Crate<*>) {
    companion object : BagStuffPacker<BpeClipboard>, BagStuffUnpacker<BpeClipboard> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BpeClipboard) {
            bag.put(value.drawingX)
            bag.put(value.drawingY)
            bag.put(Crate, value.crate)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BpeClipboard {
            requireSupportedStuffVersion("BpeClipboard", 1, version)

            val drawingX = bag.getInt()
            val drawingY = bag.getInt()
            val crate: Crate<Cell> = bag.getStuffTyped(Crate)

            return BpeClipboard(drawingX, drawingY, crate)
        }
    }
}

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
    val initialX: Int,
    val initialY: Int,
    val rect: Rect,
)
