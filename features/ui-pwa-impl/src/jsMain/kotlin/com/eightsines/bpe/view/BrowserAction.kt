package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction

    data class KeyDown(val keyCode: Int, val keyModifiers: Int) : BrowserAction
    data class KeyUp(val keyCode: Int, val keyModifiers: Int) : BrowserAction

    data object DialogHide : BrowserAction
    data object DialogOk : BrowserAction
    data class DialogPromptInput(val value: String) : BrowserAction

    data object PaintingNew : BrowserAction
    data class PaintingLoad(val inputElement: HTMLInputElement) : BrowserAction
    data object PaintingSave : BrowserAction
    data object PaintingExportTap : BrowserAction
    data object PaintingExportScr : BrowserAction
    data object PaintingExportPng : BrowserAction
}
