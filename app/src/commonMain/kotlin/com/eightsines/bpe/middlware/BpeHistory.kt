package com.eightsines.bpe.middlware

import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.TransformType
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.getList
import com.eightsines.bpe.util.putList
import com.eightsines.bpe.util.requireNoIllegalArgumentException
import com.eightsines.bpe.util.requireSupportedStuffVersion

data class HistoryStep(val actions: List<HistoryAction>, val undoActions: List<HistoryAction>) {
    companion object : BagStuffPacker<HistoryStep>, BagStuffUnpacker<HistoryStep> {
        val Empty = HistoryStep(emptyList(), emptyList())
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: HistoryStep) {
            bag.putList(value.actions) { bag.put(HistoryAction, it) }
            bag.putList(value.undoActions) { bag.put(HistoryAction, it) }
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): HistoryStep {
            requireSupportedStuffVersion("HistoryStep", 1, version)

            val actions = bag.getList { bag.getStuff(HistoryAction) }
            val undoActions = bag.getList { bag.getStuff(HistoryAction) }

            return HistoryStep(actions, undoActions)
        }
    }
}

enum class HistoryActionType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out HistoryAction>) {
    CurrentLayer(1, HistoryAction.CurrentLayer.Polymorphic),
    SelectionState(2, HistoryAction.SelectionState.Polymorphic),
    Graphics(3, HistoryAction.Graphics.Polymorphic),
    SelectionTransform(4, HistoryAction.SelectionTransform.Polymorphic),
}

sealed interface HistoryAction {
    val type: HistoryActionType

    data class CurrentLayer(val layerUid: LayerUid) : HistoryAction {
        override val type = HistoryActionType.CurrentLayer

        internal object Polymorphic : BagStuffPacker<CurrentLayer>, BagStuffUnpacker<CurrentLayer> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: CurrentLayer) {
                bag.put(value.layerUid.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): CurrentLayer {
                requireSupportedStuffVersion("HistoryAction.CurrentLayer", 1, version)
                val layerUid = LayerUid(bag.getString())
                return CurrentLayer(layerUid)
            }
        }
    }

    data class SelectionState(val selectionState: BpeSelectionState) : HistoryAction {
        override val type = HistoryActionType.SelectionState

        internal object Polymorphic : BagStuffPacker<SelectionState>, BagStuffUnpacker<SelectionState> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SelectionState) {
                bag.put(BpeSelectionState, value.selectionState)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SelectionState {
                requireSupportedStuffVersion("HistoryAction.SelectionState", 1, version)
                val selectionState = bag.getStuff(BpeSelectionState)
                return SelectionState(selectionState)
            }
        }
    }

    data class Graphics(val graphicsAction: GraphicsAction) : HistoryAction {
        override val type = HistoryActionType.Graphics

        internal object Polymorphic : BagStuffPacker<Graphics>, BagStuffUnpacker<Graphics> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Graphics) {
                bag.put(GraphicsAction, value.graphicsAction)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Graphics {
                requireSupportedStuffVersion("HistoryAction.Graphics", 1, version)
                val graphicsAction = bag.getStuff(GraphicsAction)
                return Graphics(graphicsAction)
            }
        }
    }

    data class SelectionTransform(val transformType: TransformType) : HistoryAction {
        override val type = HistoryActionType.SelectionTransform

        internal object Polymorphic : BagStuffPacker<SelectionTransform>, BagStuffUnpacker<SelectionTransform> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: SelectionTransform) {
                bag.put(value.transformType.value)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SelectionTransform {
                requireSupportedStuffVersion("HistoryAction.SelectionTransform", 1, version)
                val transformType = requireNoIllegalArgumentException { TransformType.of(bag.getInt()) }
                return SelectionTransform(transformType)
            }
        }
    }

    companion object : BagStuffPacker<HistoryAction>, BagStuffUnpacker<HistoryAction> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: HistoryAction) {
            bag.put(value.type.value)
            bag.put(value.type.polymorphicPacker, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): HistoryAction {
            requireSupportedStuffVersion("HistoryAction", 1, version)

            return when (val type = bag.getInt()) {
                HistoryActionType.CurrentLayer.value -> bag.getStuff(CurrentLayer.Polymorphic)
                HistoryActionType.SelectionState.value -> bag.getStuff(SelectionState.Polymorphic)
                HistoryActionType.Graphics.value -> bag.getStuff(Graphics.Polymorphic)
                HistoryActionType.SelectionTransform.value -> bag.getStuff(SelectionTransform.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("HistoryAction", type)
            }
        }
    }
}

fun HistoryStep.merge(innerStep: HistoryStep) = HistoryStep(actions + innerStep.actions, innerStep.undoActions + undoActions)

fun GraphicsActionPair?.toHistoryStep() = if (this == null) {
    HistoryStep.Empty
} else {
    HistoryStep(listOf(HistoryAction.Graphics(action)), listOf(HistoryAction.Graphics(undoAction)))
}
