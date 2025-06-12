package com.eightsines.bpe.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.foundation.Cell
import com.eightsines.bpe.foundation.SciiCell
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.LayerUid

@BagStuff(isPolymorphic = true)
sealed interface GraphicsAction {
    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 1)
    data class SetBackgroundBorder(@BagStuffWare(1) val color: SciiColor) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 2)
    data class SetBackgroundColor(@BagStuffWare(1) val color: SciiColor) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 3)
    data class SetBackgroundBright(@BagStuffWare(1) val light: SciiLight) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 4)
    data class SetBackgroundVisible(@BagStuffWare(1) val isVisible: Boolean) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 5)
    data class SetBackgroundLocked(@BagStuffWare(1) val isLocked: Boolean) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 6)
    data class CreateLayer(
        @BagStuffWare(1) val canvasType: CanvasType,
        @BagStuffWare(2) val layerUid: LayerUid,
        @BagStuffWare(3) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 7)
    data class ReplaceLayer(@BagStuffWare(1) val layer: CanvasLayer<*>) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 8)
    data class InsertLayer(
        @BagStuffWare(1) val layer: CanvasLayer<*>,
        @BagStuffWare(2) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 9)
    data class DeleteLayer(@BagStuffWare(1) val layerUid: LayerUid) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 10)
    data class SetLayerVisible(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isVisible: Boolean,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 11)
    data class SetLayerLocked(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isLocked: Boolean,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 19)
    data class SetLayerMasked(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val isMasked: Boolean,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 12)
    data class MoveLayer(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val onTopOfLayerUid: LayerUid,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 13)
    data class MergeShape(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val shape: Shape<Cell>,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 14)
    data class ReplaceShape(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val shape: Shape<Cell>,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 15)
    data class ReplaceCells(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val x: Int,
        @BagStuffWare(3) val y: Int,
        @BagStuffWare(4) val crate: Crate<SciiCell>,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 16)
    data class MergeLayers(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val ontoLayerUid: LayerUid,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 17)
    data class UndoMergeLayers(
        @BagStuffWare(1) val insertLayer: CanvasLayer<*>,
        @BagStuffWare(2) val insertOnTopOfLayerUid: LayerUid,
        @BagStuffWare(3) val replaceLayer: CanvasLayer<*>,
    ) : GraphicsAction

    @BagStuff(polymorphicOf = GraphicsAction::class, polymorphicId = 18)
    data class ConvertLayer(
        @BagStuffWare(1) val layerUid: LayerUid,
        @BagStuffWare(2) val canvasType: CanvasType,
    ) : GraphicsAction
}

@BagStuff
data class GraphicsActionPair(
    @BagStuffWare(1) val action: GraphicsAction,
    @BagStuffWare(2) val undoAction: GraphicsAction,
)
