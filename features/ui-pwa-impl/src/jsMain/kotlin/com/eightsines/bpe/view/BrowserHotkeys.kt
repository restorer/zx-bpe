package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.util.KeyCode
import com.eightsines.bpe.util.KeyModifier

data class BrowserKey(val keyCode: Int, val keyModifiers: Int)

val BROWSER_HOTKEYS = buildMap {
    put(BrowserKey(KeyCode.KeyB, 0), UiAction.ToolboxPaintClick)
    put(BrowserKey(KeyCode.KeyE, 0), UiAction.ToolboxEraseClick)
    put(BrowserKey(KeyCode.KeyW, 0), UiAction.ToolboxSelectClick)
    put(BrowserKey(KeyCode.KeyV, 0), UiAction.ToolboxPickColorClick)

    put(BrowserKey(KeyCode.KeyI, 0), UiAction.PaletteInkClick)
    put(BrowserKey(KeyCode.KeyO, 0), UiAction.PaletteColorClick)
    put(BrowserKey(KeyCode.KeyP, 0), UiAction.PalettePaperClick)
    put(BrowserKey(KeyCode.Digit8, 0), UiAction.PaletteBrightClick)
    put(BrowserKey(KeyCode.Digit9, 0), UiAction.PaletteFlashClick)
    put(BrowserKey(KeyCode.Digit0, 0), UiAction.PaletteCharClick)

    put(BrowserKey(KeyCode.KeyL, 0), UiAction.LayersClick)
    put(BrowserKey(KeyCode.KeyK, 0), UiAction.SelectionMenuClick)
    put(BrowserKey(KeyCode.KeyW, KeyModifier.Shift), UiAction.SelectionMenuClick)
    put(BrowserKey(KeyCode.KeyM, 0), UiAction.MenuClick)

    put(BrowserKey(KeyCode.KeyZ, KeyModifier.Ctrl), UiAction.ToolboxUndoClick)
    put(BrowserKey(KeyCode.KeyY, KeyModifier.Ctrl), UiAction.ToolboxRedoClick)
    put(BrowserKey(KeyCode.KeyZ, KeyModifier.Ctrl + KeyModifier.Shift), UiAction.ToolboxRedoClick)

    put(BrowserKey(KeyCode.KeyX, KeyModifier.Ctrl), UiAction.SelectionCutClick)
    put(BrowserKey(KeyCode.KeyC, KeyModifier.Ctrl), UiAction.SelectionCopyClick)
    put(BrowserKey(KeyCode.KeyV, KeyModifier.Ctrl), UiAction.ToolboxPasteClick)
}
