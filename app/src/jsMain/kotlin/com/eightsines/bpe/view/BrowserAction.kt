package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction
    data object Load : BrowserAction
    data object Save : BrowserAction
}
