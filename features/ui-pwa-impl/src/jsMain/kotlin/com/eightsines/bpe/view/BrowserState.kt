package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiState
import com.eightsines.bpe.util.TextDescriptor
import com.eightsines.bpe.util.TextRes

data class BrowserState(val uiState: UiState, val dialog: BrowserDialog? = null)

sealed interface BrowserDialog {
    data class Alert(val message: TextRes) : BrowserDialog
    data class Confirm(val tag: Any, val message: TextRes) : BrowserDialog
    data class Prompt(val tag: Any, val message: TextDescriptor, val hint: TextDescriptor? = null, val value: String) : BrowserDialog
}

data class DrawingTransform(
    val translateXRatio: Double = 0.0,
    val translateYRatio: Double = 0.0,
    val scale: Double = 1.0,
)
