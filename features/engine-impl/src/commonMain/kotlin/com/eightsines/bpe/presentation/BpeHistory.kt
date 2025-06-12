package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
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
            bag.putList(value.actions) { bag.put(HistoryAction_Stuff, it) }
            bag.putList(value.undoActions) { bag.put(HistoryAction_Stuff, it) }
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): HistoryStep {
            requireSupportedStuffVersion("HistoryStep", 1, version)

            val actions = bag.getList { bag.getStuff(HistoryAction_Stuff) }
            val undoActions = bag.getList { bag.getStuff(HistoryAction_Stuff) }

            return HistoryStep(actions, undoActions)
        }
    }
}

@BagStuff(isPolymorphic = true)
sealed interface HistoryAction {
    @BagStuff(polymorphicOf = HistoryAction::class, polymorphicId = 1)
    data class CurrentLayer(@BagStuffWare(1) val layerUid: LayerUid) : HistoryAction

    @BagStuff(polymorphicOf = HistoryAction::class, polymorphicId = 2)
    data class SelectionState(@BagStuffWare(1) val selectionState: BpeSelectionState) : HistoryAction

    @BagStuff(polymorphicOf = HistoryAction::class, polymorphicId = 3)
    data class Graphics(@BagStuffWare(1) val graphicsAction: GraphicsAction) : HistoryAction

    @BagStuff(polymorphicOf = HistoryAction::class, polymorphicId = 4)
    data class SelectionTransform(@BagStuffWare(1) val transformType: TransformType) : HistoryAction
}

fun HistoryStep.merge(innerStep: HistoryStep) = HistoryStep(actions + innerStep.actions, innerStep.undoActions + undoActions)

fun GraphicsActionPair?.toHistoryStep() = if (this == null) {
    HistoryStep.Empty
} else {
    HistoryStep(listOf(HistoryAction.Graphics(action)), listOf(HistoryAction.Graphics(undoAction)))
}
