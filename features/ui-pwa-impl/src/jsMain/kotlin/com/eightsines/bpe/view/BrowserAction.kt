package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.util.KeyCode
import org.w3c.dom.HTMLInputElement

sealed interface BrowserAction {
    data class Ui(val action: UiAction) : BrowserAction

    data class DrawingEnter(val x: Int, val y: Int, val width: Int, val height: Int) : BrowserAction
    data class DrawingDown(val x: Int, val y: Int, val width: Int, val height: Int) : BrowserAction
    data class DrawingMove(val x: Int, val y: Int, val width: Int, val height: Int) : BrowserAction
    data class DrawingUp(val x: Int, val y: Int, val width: Int, val height: Int) : BrowserAction
    data class DrawingWheel(val x: Int, val y: Int, val deltaY: Double, val width: Int, val height: Int) : BrowserAction
    data object DrawingLeave : BrowserAction

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

data class BrowserKey(val keyCode: Int, val keyModifiers: Int = 0) {
    companion object {
        val Space = BrowserKey(KeyCode.Space)
    }
}
