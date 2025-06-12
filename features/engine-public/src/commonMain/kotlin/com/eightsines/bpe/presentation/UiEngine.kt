package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import kotlinx.coroutines.flow.Flow

interface UiEngine {
    val state: UiState
    val visuallyChangedFlow: Flow<Unit>

    fun execute(action: UiAction)

    fun exportToTap(): List<Byte>
    fun exportToScr(): List<Byte>

    fun selfUnpacker(): BagStuffUnpacker<out UiEngine>
    fun selfPacker(historyStepsLimit: Int = -1): BagStuffPacker<out UiEngine>

    fun clear()
}
