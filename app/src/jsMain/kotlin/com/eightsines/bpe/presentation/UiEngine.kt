package com.eightsines.bpe.presentation

import com.eightsines.bpe.engine.BpeEngine
import com.eightsines.bpe.engine.BpeTool

class UiEngine(private val bpeEngine: BpeEngine) {
    private var panel: Panel? = null

    var state: UiState = refresh()
        private set

    fun execute(action: UiAction) {
        when (action) {
            is UiAction.ToolboxMenuClick -> executeMenuClick()
        }

        state = refresh()
    }

    private fun executeMenuClick() {
        panel = if (panel == Panel.Menu) null else Panel.Menu
    }

    private fun refresh(): UiState {
        val bpeState = bpeEngine.state

        val isPaintAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Paint)
        val isEraseAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Erase)

        val isPaintActive = bpeState.toolboxTool == BpeTool.Paint && isPaintAvailable
        val isEraseActive = bpeState.toolboxTool == BpeTool.Erase && isEraseAvailable

        return UiState(
            paletteColor = when {
                bpeState.palettePaper != null -> UiToolState.Hidden
                panel == Panel.Ink -> UiToolState.Active(bpeState.paletteInk)
                else -> UiToolState.Visible(bpeState.paletteInk)
            },
            paletteInk = when {
                bpeState.palettePaper == null -> UiToolState.Hidden
                panel == Panel.Ink -> UiToolState.Active(bpeState.paletteInk)
                else -> UiToolState.Visible(bpeState.paletteInk)
            },
            palettePaper = when {
                bpeState.palettePaper == null -> UiToolState.Hidden
                panel == Panel.Paper -> UiToolState.Active(bpeState.palettePaper)
                else -> UiToolState.Visible(bpeState.palettePaper)
            },
            paletteBright = when {
                bpeState.paletteBright == null -> UiToolState.Hidden
                panel == Panel.Bright -> UiToolState.Active(bpeState.paletteBright)
                else -> UiToolState.Visible(bpeState.paletteBright)
            },
            paletteFlash = when {
                bpeState.paletteFlash == null -> UiToolState.Hidden
                panel == Panel.Flash -> UiToolState.Active(bpeState.paletteFlash)
                else -> UiToolState.Visible(bpeState.paletteFlash)
            },
            paletteChar = when {
                bpeState.paletteChar == null -> UiToolState.Hidden
                panel == Panel.Characters -> UiToolState.Active(bpeState.paletteChar)
                else -> UiToolState.Visible(bpeState.paletteChar)
            },
            paletteSelectionCut = if (bpeState.selectionCanCut) UiToolState.Visible(Unit) else UiToolState.Hidden,
            paletteSelectionCopy = if (bpeState.selectionCanCopy) UiToolState.Visible(Unit) else UiToolState.Hidden,
            paletteLayers = if (panel == Panel.Layers) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            toolboxPaint = when {
                isPaintActive -> UiToolState.Hidden
                isPaintAvailable -> UiToolState.Visible(Unit)
                else -> UiToolState.Disabled(Unit)
            },
            toolboxShape = when {
                !(isPaintActive || isEraseActive) || bpeState.toolboxShape == null -> UiToolState.Hidden
                panel == Panel.Shapes -> UiToolState.Active(bpeState.toolboxShape)
                else -> UiToolState.Visible(bpeState.toolboxShape)
            },
            toolboxErase = when {
                isEraseActive -> UiToolState.Hidden
                isEraseAvailable -> UiToolState.Visible(Unit)
                else -> UiToolState.Disabled(Unit)
            },
            toolboxSelect = when {
                !bpeState.toolboxCanSelect || !bpeState.toolboxAvailTools.contains(BpeTool.Select) -> UiToolState.Disabled(Unit)
                bpeState.toolboxTool == BpeTool.Select -> UiToolState.Active(Unit)
                else -> UiToolState.Visible(Unit)
            },
            toolboxPickColor = when {
                !bpeState.toolboxAvailTools.contains(BpeTool.PickColor) -> UiToolState.Disabled(Unit)
                bpeState.toolboxTool == BpeTool.PickColor -> UiToolState.Active(Unit)
                else -> UiToolState.Visible(Unit)
            },
            toolboxPaste = if (bpeState.toolboxCanPaste) UiToolState.Visible(Unit) else UiToolState.Hidden,
            toolboxUndo = if (bpeState.toolboxCanUndo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            toolboxRedo = if (bpeState.toolboxCanRedo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            toolboxMenu = if (panel == Panel.Menu) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            panel = when (panel) {
                null -> null
                Panel.Ink, Panel.Paper -> UiPanel.Colors
                Panel.Bright, Panel.Flash -> UiPanel.Lights
                Panel.Characters -> UiPanel.Characters
                Panel.Layers -> UiPanel.Layers
                Panel.Shapes -> UiPanel.Shapes
                Panel.Menu -> UiPanel.Menu
            },
        )
    }
}

enum class Panel {
    Ink,
    Paper,
    Bright,
    Flash,
    Characters,
    Layers,
    Shapes,
    Menu,
}
