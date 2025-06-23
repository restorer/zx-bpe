package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction

    data class SheetEnter(val pointerX: Int, val pointerY: Int) : BrowserAction
    data class SheetDown(val pointerX: Int, val pointerY: Int) : BrowserAction
    data class SheetMove(val pointerX: Int, val pointerY: Int) : BrowserAction
    data class SheetUp(val pointerX: Int, val pointerY: Int) : BrowserAction
    data object SheetLeave : BrowserAction

    data class KeyDown(val browserKey: BrowserKey) : BrowserAction
    data class KeyUp(val browserKey: BrowserKey) : BrowserAction

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

data class BrowserKey(val keyCode: Int, val keyModifiers: Int = 0)
