package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction
    data class Load(val inputElement: HTMLInputElement) : BrowserAction
    data object Save : BrowserAction
}
