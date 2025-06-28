package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import kotlinx.coroutines.flow.Flow

interface UiEngine {
    val state: UiState
    val visuallyChangedFlow: Flow<Unit>

    fun execute(action: UiAction)

    fun exportToTap(name: String): List<Byte>
    fun exportToScr(): List<Byte>

    fun selfUnpacker(): BagStuffUnpacker<UiEngine>
    fun selfPacker(historyStepsLimit: Int = -1): BagStuffPacker<UiEngine>

    fun clear()
}
