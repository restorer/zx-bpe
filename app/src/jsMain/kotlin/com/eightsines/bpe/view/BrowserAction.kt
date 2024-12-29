package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction
    data object DialogHide : BrowserAction
    data class DialogConfirmOk(val tag: Any) : BrowserAction
    data class DialogPromptOk(val tag: Any, val value: String) : BrowserAction

    data object PaintingNew : BrowserAction
    data class PaintingLoad(val inputElement: HTMLInputElement) : BrowserAction
    data object PaintingSave : BrowserAction
    data object PaintingExportTap : BrowserAction
    data object PaintingExportScr : BrowserAction
    data object PaintingExportPng : BrowserAction
}
