package com.eightsines.bpe.middlware

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.getList
import com.eightsines.bpe.bag.putList
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.TransformType
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair

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
    CurrentLayer(1, HistoryAction_CurrentLayer_PolymorphicStuff),
    SelectionState(2, HistoryAction_SelectionState_PolymorphicStuff),
    Graphics(3, HistoryAction_Graphics_PolymorphicStuff),
    SelectionTransform(4, HistoryAction_SelectionTransform_PolymorphicStuff),
}

sealed interface HistoryAction {
    val type: HistoryActionType

    @BagStuff(isPolymorphic = true)
    data class CurrentLayer(@BagStuffWare(1) val layerUid: LayerUid) : HistoryAction {
        override val type = HistoryActionType.CurrentLayer
    }

    @BagStuff(isPolymorphic = true)
    data class SelectionState(@BagStuffWare(1) val selectionState: BpeSelectionState) : HistoryAction {
        override val type = HistoryActionType.SelectionState
    }

    @BagStuff(isPolymorphic = true)
    data class Graphics(@BagStuffWare(1) val graphicsAction: GraphicsAction) : HistoryAction {
        override val type = HistoryActionType.Graphics
    }

    @BagStuff(isPolymorphic = true)
    data class SelectionTransform(@BagStuffWare(1) val transformType: TransformType) : HistoryAction {
        override val type = HistoryActionType.SelectionTransform
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
                HistoryActionType.CurrentLayer.value -> bag.getStuff(HistoryAction_CurrentLayer_PolymorphicStuff)
                HistoryActionType.SelectionState.value -> bag.getStuff(HistoryAction_SelectionState_PolymorphicStuff)
                HistoryActionType.Graphics.value -> bag.getStuff(HistoryAction_Graphics_PolymorphicStuff)
                HistoryActionType.SelectionTransform.value -> bag.getStuff(HistoryAction_SelectionTransform_PolymorphicStuff)
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
