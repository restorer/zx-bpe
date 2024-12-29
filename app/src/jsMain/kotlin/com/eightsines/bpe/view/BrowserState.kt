package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiState
import com.eightsines.bpe.resources.TextDescriptor
import com.eightsines.bpe.resources.TextRes

data class BrowserState(val uiState: UiState, val dialog: BrowserDialog? = null)

sealed interface BrowserDialog {
    data class Alert(val message: TextRes) : BrowserDialog
    data class Confirm(val tag: Any, val message: TextRes) : BrowserDialog
    data class Prompt(val tag: Any, val message: TextRes, val hint: TextDescriptor? = null, val value: String) : BrowserDialog
}
