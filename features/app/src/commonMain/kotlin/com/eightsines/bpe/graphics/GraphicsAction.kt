package com.eightsines.bpe.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.LayerUid

enum class GraphicsActionType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out GraphicsAction>) {
    SetBackgroundBorder(1, GraphicsAction_SetBackgroundBorder_PolymorphicStuff),
    SetBackgroundColor(2, GraphicsAction_SetBackgroundColor_PolymorphicStuff),
    SetBackgroundBright(3, GraphicsAction_SetBackgroundBright_PolymorphicStuff),
    SetBackgroundVisible(4, GraphicsAction_SetBackgroundVisible_PolymorphicStuff),
    SetBackgroundLocked(5, GraphicsAction_SetBackgroundLocked_PolymorphicStuff),
    CreateLayer(6, GraphicsAction_CreateLayer_PolymorphicStuff),
    ReplaceLayer(7, GraphicsAction_ReplaceLayer_PolymorphicStuff),
    InsertLayer(8, GraphicsAction_InsertLayer_PolymorphicStuff),
    DeleteLayer(9, GraphicsAction_DeleteLayer_PolymorphicStuff),
    SetLayerVisible(10, GraphicsAction_SetLayerVisible_PolymorphicStuff),
    SetLayerLocked(11, GraphicsAction_SetLayerLocked_PolymorphicStuff),
    SetLayerMasked(19, GraphicsAction_SetLayerMasked_PolymorphicStuff),
    MoveLayer(12, GraphicsAction_MoveLayer_PolymorphicStuff),
    MergeShape(13, GraphicsAction_MergeShape_PolymorphicStuff),
    ReplaceShape(14, GraphicsAction_ReplaceShape_PolymorphicStuff),
    ReplaceCells(15, GraphicsAction_ReplaceCells_PolymorphicStuff),
    MergeLayers(16, GraphicsAction_MergeLayers_PolymorphicStuff),
    UndoMergeLayers(17, GraphicsAction_UndoMergeLayers_PolymorphicStuff),
    ConvertLayer(18, GraphicsAction_ConvertLayer_PolymorphicStuff),
    // Last value is 19 (SetLayerMasked)
}

@BagStuff(packer = "GraphicsAction", unpacker = "GraphicsAction")
sealed interface GraphicsAction {
    val type: GraphicsActionType

    @BagStuff(isPolymorphic = true)
    data class SetBackgroundBorder(@BagStuffWare(1) val color: SciiColor) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundBorder
    }

    @BagStuff(isPolymorphic = true)
    data class SetBackgroundColor(@BagStuffWare(1) val color: SciiColor) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundColor
    }

    @BagStuff(isPolymorphic = true)
    data class SetBackgroundBright(@BagStuffWare(1) val light: SciiLight) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundBright
    }

    @BagStuff(isPolymorphic = true)
    data class SetBackgroundVisible(@BagStuffWare(1) val isVisible: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundVisible
    }

    @BagStuff(isPolymorphic = true)
    data class SetBackgroundLocked(@BagStuffWare(1) val isLocked: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundLocked
    }

    @BagStuff(isPolymorphic = true)
    data class CreateLayer(
        @BagStuffWare(1) val canvasType: CanvasType,
        @BagStuffWare(2) val layerUid: LayerUid,
        @BagStuffWare(3) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction {
        override val type = GraphicsActionType.CreateLayer
    }

    @BagStuff(isPolymorphic = true)
    data class ReplaceLayer(
        @BagStuffWare(1) val layer: CanvasLayer<*>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceLayer
    }

    @BagStuff(isPolymorphic = true)
    data class InsertLayer(
        @BagStuffWare(1) val layer: CanvasLayer<*>,
        @BagStuffWare(2) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction {
        override val type = GraphicsActionType.InsertLayer
    }

    @BagStuff(isPolymorphic = true)
    data class DeleteLayer(@BagStuffWare(1) val layerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.DeleteLayer
    }

    @BagStuff(isPolymorphic = true)
    data class SetLayerVisible(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isVisible: Boolean,
    ) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerVisible
    }

    @BagStuff(isPolymorphic = true)
    data class SetLayerLocked(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isLocked: Boolean,
    ) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerLocked
    }

    @BagStuff(isPolymorphic = true)
    data class SetLayerMasked(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isMasked: Boolean,
    ) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerMasked
    }

    @BagStuff(isPolymorphic = true)
    data class MoveLayer(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction {
        override val type = GraphicsActionType.MoveLayer
    }

    @BagStuff(isPolymorphic = true)
    data class MergeShape(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val shape: Shape<Cell>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.MergeShape
    }

    @BagStuff(isPolymorphic = true)
    data class ReplaceShape(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val shape: Shape<Cell>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceShape
    }

    @BagStuff(isPolymorphic = true)
    data class ReplaceCells(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val x: Int,
        @BagStuffWare(3) val y: Int,
        @BagStuffWare(4) val crate: Crate<SciiCell>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceCells
    }

    @BagStuff(isPolymorphic = true)
    data class MergeLayers(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val ontoLayerUid: LayerUid,
    ) : GraphicsAction {
        override val type = GraphicsActionType.MergeLayers
    }

    @BagStuff(isPolymorphic = true)
    data class UndoMergeLayers(
        @BagStuffWare(1) val insertLayer: CanvasLayer<*>,
        @BagStuffWare(2) val insertOnTopOfLayerUid: LayerUid,
        @BagStuffWare(3) val replaceLayer: CanvasLayer<*>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.UndoMergeLayers
    }

    @BagStuff(isPolymorphic = true)
    data class ConvertLayer(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val canvasType: CanvasType,
    ) : GraphicsAction {
        override val type = GraphicsActionType.ConvertLayer
    }

    companion object : BagStuffPacker<GraphicsAction>, BagStuffUnpacker<GraphicsAction> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: GraphicsAction) {
            bag.put(value.type.value)
            bag.put(value.type.polymorphicPacker, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): GraphicsAction {
            requireSupportedStuffVersion("GraphicsAction", 1, version)

            return when (val type = bag.getInt()) {
                GraphicsActionType.SetBackgroundBorder.value -> bag.getStuff(GraphicsAction_SetBackgroundBorder_PolymorphicStuff)
                GraphicsActionType.SetBackgroundColor.value -> bag.getStuff(GraphicsAction_SetBackgroundColor_PolymorphicStuff)
                GraphicsActionType.SetBackgroundBright.value -> bag.getStuff(GraphicsAction_SetBackgroundBright_PolymorphicStuff)
                GraphicsActionType.SetBackgroundVisible.value -> bag.getStuff(GraphicsAction_SetBackgroundVisible_PolymorphicStuff)
                GraphicsActionType.SetBackgroundLocked.value -> bag.getStuff(GraphicsAction_SetBackgroundLocked_PolymorphicStuff)
                GraphicsActionType.CreateLayer.value -> bag.getStuff(GraphicsAction_CreateLayer_PolymorphicStuff)
                GraphicsActionType.ReplaceLayer.value -> bag.getStuff(GraphicsAction_ReplaceLayer_PolymorphicStuff)
                GraphicsActionType.InsertLayer.value -> bag.getStuff(GraphicsAction_InsertLayer_PolymorphicStuff)
                GraphicsActionType.DeleteLayer.value -> bag.getStuff(GraphicsAction_DeleteLayer_PolymorphicStuff)
                GraphicsActionType.SetLayerVisible.value -> bag.getStuff(GraphicsAction_SetLayerVisible_PolymorphicStuff)
                GraphicsActionType.SetLayerLocked.value -> bag.getStuff(GraphicsAction_SetLayerLocked_PolymorphicStuff)
                GraphicsActionType.SetLayerMasked.value -> bag.getStuff(GraphicsAction_SetLayerMasked_PolymorphicStuff)
                GraphicsActionType.MoveLayer.value -> bag.getStuff(GraphicsAction_MoveLayer_PolymorphicStuff)
                GraphicsActionType.MergeShape.value -> bag.getStuff(GraphicsAction_MergeShape_PolymorphicStuff)
                GraphicsActionType.ReplaceShape.value -> bag.getStuff(GraphicsAction_ReplaceShape_PolymorphicStuff)
                GraphicsActionType.ReplaceCells.value -> bag.getStuff(GraphicsAction_ReplaceCells_PolymorphicStuff)
                GraphicsActionType.MergeLayers.value -> bag.getStuff(GraphicsAction_MergeLayers_PolymorphicStuff)
                GraphicsActionType.UndoMergeLayers.value -> bag.getStuff(GraphicsAction_UndoMergeLayers_PolymorphicStuff)
                GraphicsActionType.ConvertLayer.value -> bag.getStuff(GraphicsAction_ConvertLayer_PolymorphicStuff)
                else -> throw UnknownPolymorphicTypeBagUnpackException("GraphicsAction", type)
            }
        }
    }
}

@BagStuff
data class GraphicsActionPair(
    @BagStuffWare(1) val action: GraphicsAction,
    @BagStuffWare(2) val undoAction: GraphicsAction,
)
