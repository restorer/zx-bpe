package com.eightsines.bpe.presentation

import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.middlware.BpeAction
import com.eightsines.bpe.middlware.BpeEngine
import com.eightsines.bpe.middlware.BpeShape
import com.eightsines.bpe.middlware.BpeTool
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.Severity

class UiEngine(private val logger: Logger, private val bpeEngine: BpeEngine) {
    private var activePanel: Panel? = null
    private var layerTypePanel: LayerTypePanel? = null
    private var currentDrawingType: CanvasType? = null
    private var currentCellWidth: Int = 0
    private var currentCellHeight: Int = 0
    private var currentDrawingEX: Int = 0
    private var currentDrawingEY: Int = 0
    private var cursorArea: UiArea? = null
    private var isSheetDown: Boolean = false

    var state: UiState = refresh()
        private set

    fun execute(action: UiAction) {
        val severity = if (action is UiAction.SheetEnter ||
            action is UiAction.SheetDown ||
            action is UiAction.SheetMove ||
            action is UiAction.SheetUp ||
            action is UiAction.SheetLeave
        ) Severity.Trace else Severity.Note

        logger.log(severity, "UiEngine.execute:begin") {
            put("action", action.toString())
        }

        when (action) {
            is UiAction.SheetEnter -> executeSheetEnter(action)
            is UiAction.SheetDown -> executeSheetDown(action)
            is UiAction.SheetMove -> executeSheetMove(action)
            is UiAction.SheetUp -> executeSheetUp(action)
            is UiAction.SheetLeave -> executeSheetLeave()

            is UiAction.PaletteColorClick -> executePaletteColorClick()
            is UiAction.PaletteInkClick -> executePaletteInkClick()
            is UiAction.PalettePaperClick -> executePalettePaperClick()
            is UiAction.PaletteBrightClick -> executePaletteBrightClick()
            is UiAction.PaletteFlashClick -> executePaletteFlashClick()
            is UiAction.PaletteCharClick -> executePaletteCharClick()

            is UiAction.SelectionCutClick -> executeSelectionCutClick()
            is UiAction.SelectionCopyClick -> executeSelectionCopyClick()
            is UiAction.LayersClick -> executeLayersClick()

            is UiAction.ToolboxPaintClick -> executeToolboxPaintClick()
            is UiAction.ToolboxShapeClick -> executeToolboxShapeClick()
            is UiAction.ToolboxEraseClick -> executeToolboxEraseClick()
            is UiAction.ToolboxSelectClick -> executeToolboxSelectClick()
            is UiAction.ToolboxPickColorClick -> executeToolboxPickColorClick()

            is UiAction.ToolboxPasteClick -> executeToolboxPasteClick()
            is UiAction.ToolboxUndoClick -> executeToolboxUndoClick()
            is UiAction.ToolboxRedoClick -> executeToolboxRedoClick()
            is UiAction.MenuClick -> executeMenuClick()

            is UiAction.ColorsItemClick -> executeColorsItemClick(action)
            is UiAction.LightsItemClick -> executeLightsItemClick(action)
            is UiAction.CharsItemClick -> executeCharsItemClick(action)
            is UiAction.ShapesItemClick -> executeShapesItemClick(action)

            is UiAction.LayerItemClick -> executeLayerItemClick(action)
            is UiAction.LayerItemVisibleClick -> executeLayerItemVisibleClick(action)
            is UiAction.LayerItemLockedClick -> executeLayerItemLockedClick(action)
            is UiAction.LayerCreateClick -> executeLayerCreateClick()
            is UiAction.LayerMergeClick -> executeLayerMergeClick()
            is UiAction.LayerConvertClick -> executeLayerConvertClick()
            is UiAction.LayerDeleteClick -> executeLayerDeleteClick()
            is UiAction.LayerMoveUpClick -> executeLayerMoveUpClick()
            is UiAction.LayerMoveDownClick -> executeLayerMoveDownClick()
            is UiAction.LayerTypeClick -> executeLayerTypeClick(action)
        }

        state = refresh()

        logger.log(severity, "UiEngine.execute:end") {
            put("state", state.toString())
        }
    }

    private fun executeSheetEnter(action: UiAction.SheetEnter) {
        val (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        cursorArea = if (isDrawingInside(drawingX, drawingY)) {
            drawingToArea(drawingX, drawingY)
        } else {
            null
        }
    }

    private fun executeSheetDown(action: UiAction.SheetDown) {
        activePanel = null
        val (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        if (isDrawingInside(drawingX, drawingY)) {
            cursorArea = drawingToArea(drawingX, drawingY)
            isSheetDown = true
            bpeEngine.execute(BpeAction.CanvasDown(drawingX, drawingY))
        } else {
            cursorArea = null
        }
    }

    private fun executeSheetMove(action: UiAction.SheetMove) {
        var (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        when {
            isSheetDown -> {
                drawingX = drawingX.coerceIn(0, currentDrawingEX)
                drawingY = drawingY.coerceIn(0, currentDrawingEY)
                val newCursorArea = drawingToArea(drawingX, drawingY)

                if (cursorArea != newCursorArea) {
                    cursorArea = newCursorArea
                    bpeEngine.execute(BpeAction.CanvasMove(drawingX, drawingY))
                }
            }

            isDrawingInside(drawingX, drawingY) -> cursorArea = drawingToArea(drawingX, drawingY)
            else -> cursorArea = null
        }
    }

    private fun executeSheetUp(action: UiAction.SheetUp) {
        if (!isSheetDown) {
            return
        }

        var (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        drawingX = drawingX.coerceIn(0, currentDrawingEX)
        drawingY = drawingY.coerceIn(0, currentDrawingEY)

        cursorArea = drawingToArea(drawingX, drawingY)
        bpeEngine.execute(BpeAction.CanvasUp(drawingX, drawingY))

        isSheetDown = false
    }

    private fun executeSheetLeave() {
        if (isSheetDown) {
            bpeEngine.execute(BpeAction.CanvasCancel)
            isSheetDown = false
        }

        cursorArea = null
    }

    private fun executePaletteColorClick() {
        if (state.paletteColor.isInteractable) {
            activePanel = if (activePanel == Panel.Ink) null else Panel.Ink
        }
    }

    private fun executePaletteInkClick() {
        if (state.paletteInk.isInteractable) {
            activePanel = if (activePanel == Panel.Ink) null else Panel.Ink
        }
    }

    private fun executePalettePaperClick() {
        if (state.palettePaper.isInteractable) {
            activePanel = if (activePanel == Panel.Paper) null else Panel.Paper
        }
    }

    private fun executePaletteBrightClick() {
        if (state.paletteBright.isInteractable) {
            activePanel = if (activePanel == Panel.Bright) null else Panel.Bright
        }
    }

    private fun executePaletteFlashClick() {
        if (state.paletteFlash.isInteractable) {
            activePanel = if (activePanel == Panel.Flash) null else Panel.Flash
        }
    }

    private fun executePaletteCharClick() {
        if (state.paletteChar.isInteractable) {
            activePanel = if (activePanel == Panel.Chars) null else Panel.Chars
        }
    }

    private fun executeSelectionCutClick() {
        if (state.selectionCut.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionCut)
            activePanel = null
        }
    }

    private fun executeSelectionCopyClick() {
        if (state.selectionCopy.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionCopy)
            activePanel = null
        }
    }

    private fun executeLayersClick() {
        activePanel = if (activePanel == Panel.Layers) null else Panel.Layers
        layerTypePanel = null
    }

    private fun executeToolboxPaintClick() {
        if (state.toolboxPaint.isInteractable) {
            activePanel = when {
                bpeEngine.state.toolboxTool != BpeTool.Paint -> Panel.Shapes
                activePanel == Panel.Shapes -> null
                else -> Panel.Shapes
            }

            bpeEngine.execute(BpeAction.ToolboxSetTool(BpeTool.Paint))
        }
    }

    private fun executeToolboxShapeClick() {
        if (state.toolboxShape.isInteractable) {
            activePanel = if (activePanel == Panel.Shapes) null else Panel.Shapes
        }
    }

    private fun executeToolboxEraseClick() {
        if (state.toolboxErase.isInteractable) {
            activePanel = when {
                bpeEngine.state.toolboxTool != BpeTool.Erase -> Panel.Shapes
                activePanel == Panel.Shapes -> null
                else -> Panel.Shapes
            }

            bpeEngine.execute(BpeAction.ToolboxSetTool(BpeTool.Erase))
        }
    }

    private fun executeToolboxSelectClick() {
        if (state.toolboxSelect.isInteractable) {
            val bpeState = bpeEngine.state

            if (bpeState.toolboxTool == BpeTool.Select && bpeState.selection != null) {
                bpeEngine.execute(BpeAction.SelectionDeselect)
            } else {
                bpeEngine.execute(BpeAction.ToolboxSetTool(BpeTool.Select))
            }

            activePanel = null
        }
    }

    private fun executeToolboxPickColorClick() {
        if (state.toolboxPickColor.isInteractable) {
            bpeEngine.execute(BpeAction.ToolboxSetTool(BpeTool.PickColor))
            activePanel = null
        }
    }

    private fun executeToolboxPasteClick() {
        if (state.toolboxPaste.isInteractable) {
            bpeEngine.execute(BpeAction.ToolboxPaste)
            activePanel = null
        }
    }

    private fun executeToolboxUndoClick() {
        if (state.toolboxUndo.isInteractable) {
            bpeEngine.execute(BpeAction.ToolboxUndo)
        }
    }

    private fun executeToolboxRedoClick() {
        if (state.toolboxRedo.isInteractable) {
            bpeEngine.execute(BpeAction.ToolboxRedo)
        }
    }

    private fun executeColorsItemClick(action: UiAction.ColorsItemClick) {
        when (activePanel) {
            Panel.Ink -> bpeEngine.execute(BpeAction.PaletteSetInk(action.color))
            Panel.Paper -> bpeEngine.execute(BpeAction.PaletteSetPaper(action.color))
            else -> Unit
        }
    }

    private fun executeLightsItemClick(action: UiAction.LightsItemClick) {
        when (activePanel) {
            Panel.Bright -> bpeEngine.execute(BpeAction.PaletteSetBright(action.light))
            Panel.Flash -> bpeEngine.execute(BpeAction.PaletteSetFlash(action.light))
            else -> Unit
        }
    }

    private fun executeCharsItemClick(action: UiAction.CharsItemClick) {
        bpeEngine.execute(BpeAction.PaletteSetChar(action.character))
    }

    private fun executeShapesItemClick(action: UiAction.ShapesItemClick) {
        bpeEngine.execute(BpeAction.ToolboxSetShape(action.shape))
        activePanel = null
    }

    private fun executeLayerItemClick(action: UiAction.LayerItemClick) {
        bpeEngine.execute(BpeAction.LayersSetCurrent(action.layerUid))
    }

    private fun executeLayerItemVisibleClick(action: UiAction.LayerItemVisibleClick) {
        bpeEngine.execute(BpeAction.LayersSetVisible(action.layerUid, !action.isVisible))
    }

    private fun executeLayerItemLockedClick(action: UiAction.LayerItemLockedClick) {
        bpeEngine.execute(BpeAction.LayersSetLocked(action.layerUid, !action.isLocked))
    }

    private fun executeLayerCreateClick() {
        if (state.layersCreate.isInteractable) {
            layerTypePanel = if (layerTypePanel == LayerTypePanel.Create) null else LayerTypePanel.Create
        }
    }

    private fun executeLayerMergeClick() {
        if (state.layersMerge.isInteractable) {
            bpeEngine.execute(BpeAction.LayersMerge)
            layerTypePanel = null
        }
    }

    private fun executeLayerConvertClick() {
        if (state.layersConvert.isInteractable) {
            layerTypePanel = if (layerTypePanel == LayerTypePanel.Convert) null else LayerTypePanel.Convert
        }
    }

    private fun executeLayerDeleteClick() {
        if (state.layersDelete.isInteractable) {
            bpeEngine.execute(BpeAction.LayersDelete)
        }
    }

    private fun executeLayerMoveUpClick() {
        if (state.layersMoveUp.isInteractable) {
            bpeEngine.execute(BpeAction.LayersMoveUp)
        }
    }

    private fun executeLayerMoveDownClick() {
        if (state.layersMoveDown.isInteractable) {
            bpeEngine.execute(BpeAction.LayersMoveDown)
        }
    }

    private fun executeLayerTypeClick(action: UiAction.LayerTypeClick) {
        when (layerTypePanel) {
            LayerTypePanel.Create -> bpeEngine.execute(BpeAction.LayersCreate(action.type))
            LayerTypePanel.Convert -> bpeEngine.execute(BpeAction.LayersConvert(action.type))
            else -> Unit
        }

        layerTypePanel = null
    }

    private fun executeMenuClick() {
        activePanel = if (activePanel == Panel.Menu) null else Panel.Menu
    }

    private fun pointerToDrawing(pointerX: Int, pointerY: Int): Pair<Int, Int> {
        val x = pointerX - UiSpec.BORDER_SIZE
        val y = pointerY - UiSpec.BORDER_SIZE

        return (x / currentCellWidth - if (x < 0) 1 else 0) to (y / currentCellHeight - if (y < 0) 1 else 0)
    }

    private fun drawingToArea(drawingX: Int, drawingY: Int, drawingWidth: Int = 1, drawingHeight: Int = 1) =
        UiArea(
            UiSpec.BORDER_SIZE + drawingX * currentCellWidth,
            UiSpec.BORDER_SIZE + drawingY * currentCellHeight,
            drawingWidth * currentCellWidth,
            drawingHeight * currentCellHeight,
        )

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isDrawingInside(drawingX: Int, drawingY: Int) =
        drawingX in 0..currentDrawingEX && drawingY in 0..currentDrawingEY

    private fun refresh(): UiState {
        val bpeState = bpeEngine.state
        currentDrawingType = bpeState.drawingType

        when (bpeState.drawingType) {
            null -> {
                currentCellWidth = UiSpec.PICTURE_WIDTH
                currentCellHeight = UiSpec.PICTURE_HEIGHT
                currentDrawingEX = 0
                currentDrawingEY = 0
            }

            is CanvasType.Scii -> {
                currentCellWidth = UiSpec.SCII_CELL_SIZE
                currentCellHeight = UiSpec.SCII_CELL_SIZE
                currentDrawingEX = GraphicsEngine.SCREEN_SCII_WIDTH - 1
                currentDrawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT - 1
            }

            is CanvasType.HBlock -> {
                currentCellWidth = UiSpec.SCII_CELL_SIZE
                currentCellHeight = UiSpec.BLOCK_CELL_SIZE
                currentDrawingEX = GraphicsEngine.SCREEN_SCII_WIDTH - 1
                currentDrawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT * 2 - 1
            }

            is CanvasType.VBlock -> {
                currentCellWidth = UiSpec.BLOCK_CELL_SIZE
                currentCellHeight = UiSpec.SCII_CELL_SIZE
                currentDrawingEX = GraphicsEngine.SCREEN_SCII_WIDTH * 2 - 1
                currentDrawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT - 1
            }

            is CanvasType.QBlock -> {
                currentCellWidth = UiSpec.BLOCK_CELL_SIZE
                currentCellHeight = UiSpec.BLOCK_CELL_SIZE
                currentDrawingEX = GraphicsEngine.SCREEN_SCII_WIDTH * 2 - 1
                currentDrawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT * 2 - 1
            }
        }

        val isPaintAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Paint)
        val isEraseAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Erase)

        val isPaintActive = bpeState.toolboxTool == BpeTool.Paint && isPaintAvailable
        val isEraseActive = bpeState.toolboxTool == BpeTool.Erase && isEraseAvailable

        return UiState(
            sheet = UiSheetView(bpeState.background, bpeState.canvas),
            cursorArea = cursorArea,

            selectionArea = bpeState.selection?.let {
                drawingToArea(
                    it.drawingBox.x,
                    it.drawingBox.y,
                    it.drawingBox.width,
                    it.drawingBox.height,
                )
            },

            paletteColor = when {
                bpeState.palettePaper != null -> UiToolState.Hidden
                activePanel == Panel.Ink -> UiToolState.Active(bpeState.paletteInk)
                else -> UiToolState.Visible(bpeState.paletteInk)
            },
            paletteInk = when {
                bpeState.palettePaper == null -> UiToolState.Hidden
                activePanel == Panel.Ink -> UiToolState.Active(bpeState.paletteInk)
                else -> UiToolState.Visible(bpeState.paletteInk)
            },
            palettePaper = when {
                bpeState.palettePaper == null -> UiToolState.Hidden
                activePanel == Panel.Paper -> UiToolState.Active(bpeState.palettePaper)
                else -> UiToolState.Visible(bpeState.palettePaper)
            },
            paletteBright = when {
                bpeState.paletteBright == null -> UiToolState.Hidden
                activePanel == Panel.Bright -> UiToolState.Active(bpeState.paletteBright)
                else -> UiToolState.Visible(bpeState.paletteBright)
            },
            paletteFlash = when {
                bpeState.paletteFlash == null -> UiToolState.Hidden
                activePanel == Panel.Flash -> UiToolState.Active(bpeState.paletteFlash)
                else -> UiToolState.Visible(bpeState.paletteFlash)
            },
            paletteChar = when {
                bpeState.paletteChar == null -> UiToolState.Hidden
                activePanel == Panel.Chars -> UiToolState.Active(bpeState.paletteChar)
                else -> UiToolState.Visible(bpeState.paletteChar)
            },

            selectionCut = if (bpeState.selectionCanCut) UiToolState.Visible(Unit) else UiToolState.Hidden,
            selectionCopy = if (bpeState.selectionCanCopy) UiToolState.Visible(Unit) else UiToolState.Hidden,
            layers = if (activePanel == Panel.Layers) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            toolboxPaint = when {
                isPaintActive -> UiToolState.Hidden
                isPaintAvailable -> UiToolState.Visible(Unit)
                else -> UiToolState.Disabled(Unit)
            },
            toolboxShape = when {
                !(isPaintActive || isEraseActive) || bpeState.toolboxShape == null -> UiToolState.Hidden
                activePanel != Panel.Shapes && activePanel?.placement == PanelPlacement.Toolbox -> UiToolState.Visible(bpeState.toolboxShape)
                else -> UiToolState.Active(bpeState.toolboxShape)
            },
            toolboxErase = when {
                isEraseActive -> UiToolState.Hidden
                isEraseAvailable -> UiToolState.Visible(Unit)
                else -> UiToolState.Disabled(Unit)
            },
            toolboxSelect = when {
                !bpeState.toolboxCanSelect || !bpeState.toolboxAvailTools.contains(BpeTool.Select) -> UiToolState.Disabled(Unit)
                bpeState.toolboxTool != BpeTool.Select || activePanel?.placement == PanelPlacement.Toolbox -> UiToolState.Visible(Unit)
                else -> UiToolState.Active(Unit)
            },
            toolboxPickColor = when {
                !bpeState.toolboxAvailTools.contains(BpeTool.PickColor) -> UiToolState.Disabled(Unit)
                bpeState.toolboxTool != BpeTool.PickColor || activePanel?.placement == PanelPlacement.Toolbox -> UiToolState.Visible(Unit)
                else -> UiToolState.Active(Unit)
            },

            toolboxPaste = if (bpeState.toolboxCanPaste) UiToolState.Visible(Unit) else UiToolState.Hidden,
            toolboxUndo = if (bpeState.toolboxCanUndo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            toolboxRedo = if (bpeState.toolboxCanRedo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            menu = if (activePanel == Panel.Menu) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            activePanel = when (activePanel) {
                null -> null
                Panel.Ink -> UiPanel.Colors(bpeState.paletteInk)
                Panel.Paper -> UiPanel.Colors(bpeState.palettePaper ?: SciiColor.Transparent)
                Panel.Bright -> UiPanel.Lights(bpeState.paletteBright ?: SciiLight.Transparent)
                Panel.Flash -> UiPanel.Lights(bpeState.paletteBright ?: SciiLight.Transparent)
                Panel.Chars -> UiPanel.Chars(bpeState.paletteChar ?: SciiChar.Transparent)
                Panel.Layers -> UiPanel.Layers
                Panel.Shapes -> UiPanel.Shapes(bpeState.toolboxShape ?: BpeShape.Point)
                Panel.Menu -> UiPanel.Menu
            },

            layersItems = bpeState.layers,
            layersCurrentUid = bpeState.layersCurrentUid,
            layersCreate = if (layerTypePanel == LayerTypePanel.Create) {
                UiToolState.Active(Unit)
            } else {
                UiToolState.Visible(Unit)
            },
            layersMerge = if (bpeState.layersCanMerge) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersConvert = when {
                !bpeState.layersCanConvert -> UiToolState.Disabled(Unit)
                layerTypePanel == LayerTypePanel.Convert -> UiToolState.Active(Unit)
                else -> UiToolState.Visible(Unit)
            },
            layersDelete = if (bpeState.layersCanDelete) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersMoveUp = if (bpeState.layersCanMoveUp) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersMoveDown = if (bpeState.layersCanMoveDown) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersTypesIsVisible = layerTypePanel != null,
        )
    }
}

private enum class PanelPlacement {
    Palette,
    Toolbox,
}

private enum class Panel(val placement: PanelPlacement) {
    Ink(PanelPlacement.Palette),
    Paper(PanelPlacement.Palette),
    Bright(PanelPlacement.Palette),
    Flash(PanelPlacement.Palette),
    Chars(PanelPlacement.Palette),
    Layers(PanelPlacement.Palette),
    Shapes(PanelPlacement.Toolbox),
    Menu(PanelPlacement.Toolbox),
}

private enum class LayerTypePanel {
    Create,
    Convert,
}
