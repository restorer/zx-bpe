package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.TransformType
import com.eightsines.bpe.graphics.GraphicsAction
import com.eightsines.bpe.graphics.GraphicsActionPair

@BagStuff
data class HistoryStep(
    @BagStuffWare(1) val actions: List<HistoryAction>,
    @BagStuffWare(2) val undoActions: List<HistoryAction>,
) {
    companion object {
        val Empty = HistoryStep(emptyList(), emptyList())
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
