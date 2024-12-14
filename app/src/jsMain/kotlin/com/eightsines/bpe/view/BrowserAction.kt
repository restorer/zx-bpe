package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction
    data object AlertHide : BrowserAction

    data object PaintingNew : BrowserAction
    data class PaintingLoad(val inputElement: HTMLInputElement) : BrowserAction
    data object PaintingSave : BrowserAction
    data object PaintingExport : BrowserAction
}
