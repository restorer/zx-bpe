package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.MutableCanvasLayer
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.getStuffTyped
import com.eightsines.bpe.util.requireNoIllegalArgumentException
import com.eightsines.bpe.util.requireSupportedStuffVersion

enum class GraphicsActionType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out GraphicsAction>) {
    // v1
    SetBackgroundBorder(1, GraphicsAction.SetBackgroundBorder.Polymorphic),
    SetBackgroundColor(2, GraphicsAction.SetBackgroundColor.Polymorphic),
    SetBackgroundBright(3, GraphicsAction.SetBackgroundBright.Polymorphic),
    SetBackgroundVisible(4, GraphicsAction.SetBackgroundVisible.Polymorphic),
    SetBackgroundLocked(5, GraphicsAction.SetBackgroundLocked.Polymorphic),
    CreateLayer(6, GraphicsAction.CreateLayer.Polymorphic),
    ReplaceLayer(7, GraphicsAction.ReplaceLayer.Polymorphic),
    InsertLayer(8, GraphicsAction.InsertLayer.Polymorphic),
    DeleteLayer(9, GraphicsAction.DeleteLayer.Polymorphic),
    SetLayerVisible(10, GraphicsAction.SetLayerVisible.Polymorphic),
    SetLayerLocked(11, GraphicsAction.SetLayerLocked.Polymorphic),
    MoveLayer(12, GraphicsAction.MoveLayer.Polymorphic),
    MergeShape(13, GraphicsAction.MergeShape.Polymorphic),
    ReplaceShape(14, GraphicsAction.ReplaceShape.Polymorphic),
    ReplaceCells(15, GraphicsAction.ReplaceCells.Polymorphic),
    MergeLayers(16, GraphicsAction.MergeLayers.Polymorphic),
    UndoMergeLayers(17, GraphicsAction.UndoMergeLayers.Polymorphic),
    ConvertLayer(18, GraphicsAction.ConvertLayer.Polymorphic),

    // v2
    SetLayerMasked(19, GraphicsAction.SetLayerMasked.Polymorphic),
}

sealed interface GraphicsAction {
    val type: GraphicsActionType

    data class SetBackgroundBorder(val color: SciiColor) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundBorder

        internal object Polymorphic : BagStuffPacker<SetBackgroundBorder>, BagStuffUnpacker<SetBackgroundBorder> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetBackgroundBorder) {
                bag.put(value.color.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetBackgroundBorder {
                requireSupportedStuffVersion("GraphicsAction.SetBackgroundBorder", 1, version)
                val color = SciiColor(bag.getInt())
                return SetBackgroundBorder(color)
            }
        }
    }

    data class SetBackgroundColor(val color: SciiColor) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundColor

        internal object Polymorphic : BagStuffPacker<SetBackgroundColor>, BagStuffUnpacker<SetBackgroundColor> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetBackgroundColor) {
                bag.put(value.color.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetBackgroundColor {
                requireSupportedStuffVersion("GraphicsAction.SetBackgroundColor", 1, version)
                val color = SciiColor(bag.getInt())
                return SetBackgroundColor(color)
            }
        }
    }

    data class SetBackgroundBright(val light: SciiLight) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundBright

        internal object Polymorphic : BagStuffPacker<SetBackgroundBright>, BagStuffUnpacker<SetBackgroundBright> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetBackgroundBright) {
                bag.put(value.light.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetBackgroundBright {
                requireSupportedStuffVersion("GraphicsAction.SetBackgroundBright", 1, version)
                val light = SciiLight(bag.getInt())
                return SetBackgroundBright(light)
            }
        }
    }

    data class SetBackgroundVisible(val isVisible: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundVisible

        internal object Polymorphic : BagStuffPacker<SetBackgroundVisible>, BagStuffUnpacker<SetBackgroundVisible> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetBackgroundVisible) {
                bag.put(value.isVisible)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetBackgroundVisible {
                requireSupportedStuffVersion("GraphicsAction.SetBackgroundVisible", 1, version)
                val isVisible = bag.getBoolean()
                return SetBackgroundVisible(isVisible)
            }
        }
    }

    data class SetBackgroundLocked(val isLocked: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetBackgroundLocked

        internal object Polymorphic : BagStuffPacker<SetBackgroundLocked>, BagStuffUnpacker<SetBackgroundLocked> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetBackgroundLocked) {
                bag.put(value.isLocked)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetBackgroundLocked {
                requireSupportedStuffVersion("GraphicsAction.SetBackgroundLocked", 1, version)
                val isLocked = bag.getBoolean()
                return SetBackgroundLocked(isLocked)
            }
        }
    }

    data class CreateLayer(val canvasType: CanvasType, val layerUid: LayerUid, val onTopOfLayerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.CreateLayer

        internal object Polymorphic : BagStuffPacker<CreateLayer>, BagStuffUnpacker<CreateLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: CreateLayer) {
                bag.put(value.canvasType.value)
                bag.put(value.layerUid.value)
                bag.put(value.onTopOfLayerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): CreateLayer {
                requireSupportedStuffVersion("GraphicsAction.CreateLayer", 1, version)

                val canvasType = requireNoIllegalArgumentException { CanvasType.of(bag.getInt()) }
                val layerUid = LayerUid(bag.getString())
                val onTopOfLayerUid = LayerUid(bag.getString())

                return CreateLayer(canvasType, layerUid, onTopOfLayerUid)
            }
        }
    }

    data class ReplaceLayer(val layer: CanvasLayer<*>) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceLayer

        internal object Polymorphic : BagStuffPacker<ReplaceLayer>, BagStuffUnpacker<ReplaceLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: ReplaceLayer) {
                bag.put(CanvasLayer, value.layer)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): ReplaceLayer {
                requireSupportedStuffVersion("GraphicsAction.ReplaceLayer", 1, version)
                val layer = bag.getStuff(MutableCanvasLayer)
                return ReplaceLayer(layer)
            }
        }
    }

    data class InsertLayer(val layer: CanvasLayer<*>, val onTopOfLayerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.InsertLayer

        internal object Polymorphic : BagStuffPacker<InsertLayer>, BagStuffUnpacker<InsertLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: InsertLayer) {
                bag.put(CanvasLayer, value.layer)
                bag.put(value.onTopOfLayerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): InsertLayer {
                requireSupportedStuffVersion("GraphicsAction.InsertLayer", 1, version)

                val layer = bag.getStuff(MutableCanvasLayer)
                val onTopOfLayerUid = LayerUid(bag.getString())

                return InsertLayer(layer, onTopOfLayerUid)
            }
        }
    }

    data class DeleteLayer(val layerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.DeleteLayer

        internal object Polymorphic : BagStuffPacker<DeleteLayer>, BagStuffUnpacker<DeleteLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: DeleteLayer) {
                bag.put(value.layerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): DeleteLayer {
                requireSupportedStuffVersion("GraphicsAction.DeleteLayer", 1, version)
                val layerUid = LayerUid(bag.getString())
                return DeleteLayer(layerUid)
            }
        }
    }

    data class SetLayerVisible(val layerUid: LayerUid, val isVisible: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerVisible

        internal object Polymorphic : BagStuffPacker<SetLayerVisible>, BagStuffUnpacker<SetLayerVisible> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetLayerVisible) {
                bag.put(value.layerUid.value)
                bag.put(value.isVisible)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetLayerVisible {
                requireSupportedStuffVersion("GraphicsAction.SetLayerVisible", 1, version)

                val layerUid = LayerUid(bag.getString())
                val isVisible = bag.getBoolean()

                return SetLayerVisible(layerUid, isVisible)
            }
        }
    }

    data class SetLayerLocked(val layerUid: LayerUid, val isLocked: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerLocked

        internal object Polymorphic : BagStuffPacker<SetLayerLocked>, BagStuffUnpacker<SetLayerLocked> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetLayerLocked) {
                bag.put(value.layerUid.value)
                bag.put(value.isLocked)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetLayerLocked {
                requireSupportedStuffVersion("GraphicsAction.SetLayerLocked", 1, version)

                val layerUid = LayerUid(bag.getString())
                val isLocked = bag.getBoolean()

                return SetLayerLocked(layerUid, isLocked)
            }
        }
    }

    data class SetLayerMasked(val layerUid: LayerUid, val isMasked: Boolean) : GraphicsAction {
        override val type = GraphicsActionType.SetLayerMasked

        internal object Polymorphic : BagStuffPacker<SetLayerMasked>, BagStuffUnpacker<SetLayerMasked> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SetLayerMasked) {
                bag.put(value.layerUid.value)
                bag.put(value.isMasked)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SetLayerMasked {
                requireSupportedStuffVersion("GraphicsAction.SetLayerMasked", 1, version)

                val layerUid = LayerUid(bag.getString())
                val isMasked = bag.getBoolean()

                return SetLayerMasked(layerUid, isMasked)
            }
        }
    }

    data class MoveLayer(val layerUid: LayerUid, val onTopOfLayerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.MoveLayer

        internal object Polymorphic : BagStuffPacker<MoveLayer>, BagStuffUnpacker<MoveLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: MoveLayer) {
                bag.put(value.layerUid.value)
                bag.put(value.onTopOfLayerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MoveLayer {
                requireSupportedStuffVersion("GraphicsAction.MoveLayer", 1, version)

                val layerUid = LayerUid(bag.getString())
                val onTopOfLayerUid = LayerUid(bag.getString())

                return MoveLayer(layerUid, onTopOfLayerUid)
            }
        }
    }

    data class MergeShape(val layerUid: LayerUid, val shape: Shape<Cell>) : GraphicsAction {
        override val type = GraphicsActionType.MergeShape

        internal object Polymorphic : BagStuffPacker<MergeShape>, BagStuffUnpacker<MergeShape> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: MergeShape) {
                bag.put(value.layerUid.value)
                bag.put(Shape, value.shape)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MergeShape {
                requireSupportedStuffVersion("GraphicsAction.MergeShape", 1, version)

                val layerUid = LayerUid(bag.getString())
                val shape: Shape<Cell> = bag.getStuffTyped(Shape)

                return MergeShape(layerUid, shape)
            }
        }
    }

    data class ReplaceShape(val layerUid: LayerUid, val shape: Shape<Cell>) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceShape

        internal object Polymorphic : BagStuffPacker<ReplaceShape>, BagStuffUnpacker<ReplaceShape> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: ReplaceShape) {
                bag.put(value.layerUid.value)
                bag.put(Shape, value.shape)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): ReplaceShape {
                requireSupportedStuffVersion("GraphicsAction.ReplaceShape", 1, version)

                val layerUid = LayerUid(bag.getString())
                val shape: Shape<Cell> = bag.getStuffTyped(Shape)

                return ReplaceShape(layerUid, shape)
            }
        }
    }

    data class ReplaceCells(
        val layerUid: LayerUid,
        val x: Int,
        val y: Int,
        val crate: Crate<SciiCell>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.ReplaceCells

        internal object Polymorphic : BagStuffPacker<ReplaceCells>, BagStuffUnpacker<ReplaceCells> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: ReplaceCells) {
                bag.put(value.layerUid.value)
                bag.put(value.x)
                bag.put(value.y)
                bag.put(Crate, value.crate)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): ReplaceCells {
                requireSupportedStuffVersion("GraphicsAction.ReplaceCells", 1, version)

                val layerUid = LayerUid(bag.getString())
                val x = bag.getInt()
                val y = bag.getInt()
                val crate: Crate<SciiCell> = bag.getStuffTyped(Crate)

                return ReplaceCells(layerUid, x, y, crate)
            }
        }
    }

    data class MergeLayers(val layerUid: LayerUid, val ontoLayerUid: LayerUid) : GraphicsAction {
        override val type = GraphicsActionType.MergeLayers

        internal object Polymorphic : BagStuffPacker<MergeLayers>, BagStuffUnpacker<MergeLayers> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: MergeLayers) {
                bag.put(value.layerUid.value)
                bag.put(value.ontoLayerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MergeLayers {
                requireSupportedStuffVersion("GraphicsAction.MergeLayers", 1, version)

                val layerUid = LayerUid(bag.getString())
                val ontoLayerUid = LayerUid(bag.getString())

                return MergeLayers(layerUid, ontoLayerUid)
            }
        }
    }

    data class UndoMergeLayers(
        val insertLayer: CanvasLayer<*>,
        val insertOnTopOfLayerUid: LayerUid,
        val replaceLayer: CanvasLayer<*>,
    ) : GraphicsAction {
        override val type = GraphicsActionType.UndoMergeLayers

        internal object Polymorphic : BagStuffPacker<UndoMergeLayers>, BagStuffUnpacker<UndoMergeLayers> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: UndoMergeLayers) {
                bag.put(CanvasLayer, value.insertLayer)
                bag.put(value.insertOnTopOfLayerUid.value)
                bag.put(CanvasLayer, value.replaceLayer)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): UndoMergeLayers {
                requireSupportedStuffVersion("GraphicsAction.UndoMergeLayers", 1, version)

                val insertLayer = bag.getStuff(MutableCanvasLayer)
                val insertOnTopOfLayerUid = LayerUid(bag.getString())
                val replaceLayer = bag.getStuff(MutableCanvasLayer)

                return UndoMergeLayers(insertLayer, insertOnTopOfLayerUid, replaceLayer)
            }
        }
    }

    data class ConvertLayer(val layerUid: LayerUid, val canvasType: CanvasType) : GraphicsAction {
        override val type = GraphicsActionType.ConvertLayer

        internal object Polymorphic : BagStuffPacker<ConvertLayer>, BagStuffUnpacker<ConvertLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: ConvertLayer) {
                bag.put(value.layerUid.value)
                bag.put(value.canvasType.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): ConvertLayer {
                requireSupportedStuffVersion("GraphicsAction.ConvertLayer", 1, version)

                val layerUid = LayerUid(bag.getString())
                val canvasType = requireNoIllegalArgumentException { CanvasType.of(bag.getInt()) }

                return ConvertLayer(layerUid, canvasType)
            }
        }
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
                GraphicsActionType.SetBackgroundBorder.value -> bag.getStuff(SetBackgroundBorder.Polymorphic)
                GraphicsActionType.SetBackgroundColor.value -> bag.getStuff(SetBackgroundColor.Polymorphic)
                GraphicsActionType.SetBackgroundBright.value -> bag.getStuff(SetBackgroundBright.Polymorphic)
                GraphicsActionType.SetBackgroundVisible.value -> bag.getStuff(SetBackgroundVisible.Polymorphic)
                GraphicsActionType.SetBackgroundLocked.value -> bag.getStuff(SetBackgroundLocked.Polymorphic)
                GraphicsActionType.CreateLayer.value -> bag.getStuff(CreateLayer.Polymorphic)
                GraphicsActionType.ReplaceLayer.value -> bag.getStuff(ReplaceLayer.Polymorphic)
                GraphicsActionType.InsertLayer.value -> bag.getStuff(InsertLayer.Polymorphic)
                GraphicsActionType.DeleteLayer.value -> bag.getStuff(DeleteLayer.Polymorphic)
                GraphicsActionType.SetLayerVisible.value -> bag.getStuff(SetLayerVisible.Polymorphic)
                GraphicsActionType.SetLayerLocked.value -> bag.getStuff(SetLayerLocked.Polymorphic)
                GraphicsActionType.SetLayerMasked.value -> bag.getStuff(SetLayerMasked.Polymorphic)
                GraphicsActionType.MoveLayer.value -> bag.getStuff(MoveLayer.Polymorphic)
                GraphicsActionType.MergeShape.value -> bag.getStuff(MergeShape.Polymorphic)
                GraphicsActionType.ReplaceShape.value -> bag.getStuff(ReplaceShape.Polymorphic)
                GraphicsActionType.ReplaceCells.value -> bag.getStuff(ReplaceCells.Polymorphic)
                GraphicsActionType.MergeLayers.value -> bag.getStuff(MergeLayers.Polymorphic)
                GraphicsActionType.UndoMergeLayers.value -> bag.getStuff(UndoMergeLayers.Polymorphic)
                GraphicsActionType.ConvertLayer.value -> bag.getStuff(ConvertLayer.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("GraphicsAction", type)
            }
        }
    }
}

data class GraphicsActionPair(val action: GraphicsAction, val undoAction: GraphicsAction) {
    companion object : BagStuffPacker<GraphicsActionPair>, BagStuffUnpacker<GraphicsActionPair> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: GraphicsActionPair) {
            bag.put(GraphicsAction, value.action)
            bag.put(GraphicsAction, value.undoAction)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): GraphicsActionPair {
            requireSupportedStuffVersion("GraphicsActionPair", 1, version)

            val action = bag.getStuff(GraphicsAction)
            val undoAction = bag.getStuff(GraphicsAction)

            return GraphicsActionPair(action, undoAction)
        }
    }
}
