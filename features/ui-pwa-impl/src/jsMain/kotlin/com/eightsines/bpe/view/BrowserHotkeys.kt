package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.util.KeyCode
import com.eightsines.bpe.util.KeyModifier

val BROWSER_HOTKEYS = buildMap {
    put(BrowserKey(KeyCode.KeyD), UiAction.ToolboxPaintClick)
    put(BrowserKey(KeyCode.KeyE), UiAction.ToolboxEraseClick)
    put(BrowserKey(KeyCode.KeyW), UiAction.ToolboxSelectClick)
    put(BrowserKey(KeyCode.KeyS), UiAction.ToolboxPickColorClick)

    put(BrowserKey(KeyCode.KeyO), UiAction.PaletteInkOrColorClick)
    put(BrowserKey(KeyCode.KeyP), UiAction.PalettePaperClick)
    put(BrowserKey(KeyCode.KeyI), UiAction.PaletteBrightClick)
    put(BrowserKey(KeyCode.KeyU), UiAction.PaletteFlashClick)
    put(BrowserKey(KeyCode.KeyK), UiAction.PaletteCharClick)

    put(BrowserKey(KeyCode.KeyL), UiAction.LayersClick)
    put(BrowserKey(KeyCode.KeyJ), UiAction.SelectionMenuClick)
    put(BrowserKey(KeyCode.KeyM), UiAction.MenuClick)
    put(BrowserKey(KeyCode.KeyN), UiAction.PaintingModeClick)

    put(BrowserKey(KeyCode.KeyZ, KeyModifier.Ctrl), UiAction.ToolboxUndoClick)
    put(BrowserKey(KeyCode.KeyY, KeyModifier.Ctrl), UiAction.ToolboxRedoClick)
    put(BrowserKey(KeyCode.KeyZ, KeyModifier.Ctrl + KeyModifier.Shift), UiAction.ToolboxRedoClick)

    put(BrowserKey(KeyCode.KeyX, KeyModifier.Ctrl), UiAction.SelectionCutClick)
    put(BrowserKey(KeyCode.KeyC, KeyModifier.Ctrl), UiAction.SelectionCopyClick)
    put(BrowserKey(KeyCode.KeyV, KeyModifier.Ctrl), UiAction.ToolboxPasteClick)

    put(BrowserKey(KeyCode.Digit0), UiAction.PanelPress(0))
    put(BrowserKey(KeyCode.Digit1), UiAction.PanelPress(1))
    put(BrowserKey(KeyCode.Digit2), UiAction.PanelPress(2))
    put(BrowserKey(KeyCode.Digit3), UiAction.PanelPress(3))
    put(BrowserKey(KeyCode.Digit4), UiAction.PanelPress(4))
    put(BrowserKey(KeyCode.Digit5), UiAction.PanelPress(5))
    put(BrowserKey(KeyCode.Digit6), UiAction.PanelPress(6))
    put(BrowserKey(KeyCode.Digit7), UiAction.PanelPress(7))
    put(BrowserKey(KeyCode.Digit8), UiAction.PanelPress(8))
    put(BrowserKey(KeyCode.Digit9), UiAction.PanelPress(9))
}

val BROWSER_HANDLED_KEYS = setOf(
    BrowserKey(KeyCode.Enter),
    BrowserKey(KeyCode.Escape),
    BrowserKey(KeyCode.Space),
)
