package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.isBlock
import com.eightsines.bpe.graphics.GraphicsEngine
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.Severity
import com.eightsines.bpe.util.TextDescriptor
import com.eightsines.bpe.util.TextRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

class UiEngineImpl(private val logger: Logger, private val bpeEngine: BpeEngine) : UiEngine {
    private var activePanel: Panel? = null
    private var layerTypePanel: LayerTypePanel? = null
    private var currentDrawingType: CanvasType? = null
    private var currentAreaSpec: AreaSpec = AreaSpec(0, 0, 0, 0, 0.0, 0.0)
    private var isPaintActive: Boolean = false
    private var isEraseActive: Boolean = false
    private var cursorSpec: CursorSpec? = null
    private var isSheetDown: Boolean = false
    private val bpeStateFlow = MutableSharedFlow<BpeState>(extraBufferCapacity = 1)

    @Suppress("RedundantUnitExpression")
    private val _visuallyChangedFlow = bpeStateFlow
        .distinctUntilChanged { old, new -> old === new }
        .drop(1)
        .map { Unit }

    override var state: UiState = refresh()
        private set

    override val visuallyChangedFlow: Flow<Unit>
        get() = _visuallyChangedFlow

    override fun execute(action: UiAction) {
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

            is UiAction.PaintingModeClick -> executePaintingModeClick()

            is UiAction.SelectionMenuClick -> executeSelectionMenuClick()
            is UiAction.SelectionCutClick -> executeSelectionCutClick()
            is UiAction.SelectionCopyClick -> executeSelectionCopyClick()
            is UiAction.SelectionFlipHorizontalClick -> executeSelectionFlipHorizontalClick()
            is UiAction.SelectionFlipVerticalClick -> executeSelectionFlipVerticalClick()
            is UiAction.SelectionRotateCwClick -> executeSelectionRotateCwClick()
            is UiAction.SelectionRotateCcwClick -> executeSelectionRotateCcwClick()
            is UiAction.SelectionFillClick -> executeSelectionFillClick()
            is UiAction.SelectionClearClick -> executeSelectionClearClick()
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

            is UiAction.PanelColorClick -> executePanelColorClick(action)
            is UiAction.PanelLightClick -> executePanelLightClick(action)
            is UiAction.PanelCharClick -> executePanelCharClick(action)
            is UiAction.PanelEraseClick -> executePanelEraseClick(action)
            is UiAction.PanelShapeClick -> executePanelShapeClick(action)

            is UiAction.LayerItemClick -> executeLayerItemClick(action)
            is UiAction.LayerItemVisibleClick -> executeLayerItemVisibleClick(action)
            is UiAction.LayerItemLockedClick -> executeLayerItemLockedClick(action)
            is UiAction.LayerItemMaskedClick -> executeLayerItemMaskedClick(action)
            is UiAction.LayerCreateClick -> executeLayerCreateClick()
            is UiAction.LayerMergeClick -> executeLayerMergeClick()
            is UiAction.LayerConvertClick -> executeLayerConvertClick()
            is UiAction.LayerDeleteClick -> executeLayerDeleteClick()
            is UiAction.LayerMoveUpClick -> executeLayerMoveUpClick()
            is UiAction.LayerMoveDownClick -> executeLayerMoveDownClick()
            is UiAction.LayerTypeClick -> executeLayerTypeClick(action)
        }

        state = refresh()

        logger.trace("UiEngine.execute:end") {
            put("state", state.toString())
        }
    }

    override fun exportToTap(): List<Byte> = bpeEngine.exportToTap()
    override fun exportToScr(): List<Byte> = bpeEngine.exportToScr()

    @Suppress("UNCHECKED_CAST")
    override fun selfUnpacker(): BagStuffUnpacker<UiEngine> = Unpacker() as BagStuffUnpacker<UiEngine>

    @Suppress("UNCHECKED_CAST")
    override fun selfPacker(historyStepsLimit: Int): BagStuffPacker<UiEngine> = Packer(historyStepsLimit) as BagStuffPacker<UiEngine>

    override fun clear() {
        bpeEngine.clear()
        state = refresh()
    }

    private fun executeSheetEnter(action: UiAction.SheetEnter) {
        val (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        cursorSpec = if (isDrawingInside(drawingX, drawingY)) {
            drawingToCursorSpec(currentAreaSpec, drawingX, drawingY)
        } else {
            null
        }
    }

    private fun executeSheetDown(action: UiAction.SheetDown) {
        activePanel = null
        val (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        if (isDrawingInside(drawingX, drawingY)) {
            cursorSpec = drawingToCursorSpec(currentAreaSpec, drawingX, drawingY)
            isSheetDown = true
            bpeEngine.execute(BpeAction.CanvasDown(drawingX, drawingY))
        } else {
            cursorSpec = null
            bpeEngine.execute(BpeAction.SelectionDeselect)
        }
    }

    private fun executeSheetMove(action: UiAction.SheetMove) {
        var (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        when {
            isSheetDown -> {
                drawingX = drawingX.coerceIn(0, currentAreaSpec.drawingEX)
                drawingY = drawingY.coerceIn(0, currentAreaSpec.drawingEY)
                val newCursorSpec = drawingToCursorSpec(currentAreaSpec, drawingX, drawingY)

                if (cursorSpec?.primaryArea != newCursorSpec.primaryArea) {
                    cursorSpec = newCursorSpec
                    bpeEngine.execute(BpeAction.CanvasMove(drawingX, drawingY))
                }
            }

            isDrawingInside(drawingX, drawingY) -> cursorSpec = drawingToCursorSpec(currentAreaSpec, drawingX, drawingY)
            else -> cursorSpec = null
        }
    }

    private fun executeSheetUp(action: UiAction.SheetUp) {
        if (!isSheetDown) {
            return
        }

        var (drawingX, drawingY) = pointerToDrawing(action.pointerX, action.pointerY)

        drawingX = drawingX.coerceIn(0, currentAreaSpec.drawingEX)
        drawingY = drawingY.coerceIn(0, currentAreaSpec.drawingEY)

        cursorSpec = drawingToCursorSpec(currentAreaSpec, drawingX, drawingY)
        bpeEngine.execute(BpeAction.CanvasUp(drawingX, drawingY))

        isSheetDown = false
    }

    private fun executeSheetLeave() {
        if (isSheetDown) {
            bpeEngine.execute(BpeAction.CanvasCancel)
            isSheetDown = false
        }

        cursorSpec = null
    }

    private fun executePaletteColorClick() {
        if (state.paletteColor.isInteractable) {
            val panel = when {
                currentDrawingType.isBlock && isPaintActive -> Panel.PaintBlockColor
                currentDrawingType.isBlock && isEraseActive -> Panel.EraseBlockColor
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePalettePaperClick() {
        if (state.palettePaper.isInteractable) {
            val panel = when {
                currentDrawingType == CanvasType.Scii && isPaintActive -> Panel.PaintSciiPaper
                currentDrawingType == CanvasType.Scii && isEraseActive -> Panel.EraseSciiPaper
                currentDrawingType == null -> Panel.BackgroundBorder
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePaletteInkClick() {
        if (state.paletteInk.isInteractable) {
            val panel = when {
                currentDrawingType == CanvasType.Scii && isPaintActive -> Panel.PaintSciiInk
                currentDrawingType == CanvasType.Scii && isEraseActive -> Panel.EraseSciiInk
                currentDrawingType == null -> Panel.BackgroundPaper
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePaletteBrightClick() {
        if (state.paletteBright.isInteractable) {
            val panel = when {
                currentDrawingType == CanvasType.Scii && isPaintActive -> Panel.PaintSciiBright
                currentDrawingType == CanvasType.Scii && isEraseActive -> Panel.EraseSciiBright
                currentDrawingType.isBlock && isPaintActive -> Panel.PaintBlockBright
                currentDrawingType.isBlock && isEraseActive -> Panel.EraseBlockBright
                currentDrawingType == null -> Panel.BackgroundBright
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePaletteFlashClick() {
        if (state.paletteFlash.isInteractable) {
            val panel = when {
                currentDrawingType == CanvasType.Scii && isPaintActive -> Panel.PaintSciiFlash
                currentDrawingType == CanvasType.Scii && isEraseActive -> Panel.EraseSciiFlash
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePaletteCharClick() {
        if (state.paletteChar.isInteractable) {
            val panel = when {
                currentDrawingType == CanvasType.Scii && isPaintActive -> Panel.PaintSciiChar
                currentDrawingType == CanvasType.Scii && isEraseActive -> Panel.EraseSciiChar
                else -> null
            }

            activePanel = if (activePanel == panel) null else panel
        }
    }

    private fun executePaintingModeClick() {
        activePanel = null

        bpeEngine.execute(
            BpeAction.SetPaintingMode(
                when (bpeEngine.state.paintingMode) {
                    BpePaintingMode.Edge -> BpePaintingMode.Center
                    BpePaintingMode.Center -> BpePaintingMode.Edge
                },
            ),
        )
    }

    private fun executeSelectionMenuClick() {
        if (state.selectionMenu.isInteractable) {
            activePanel = if (activePanel == Panel.SelectionMenu) null else Panel.SelectionMenu
        }
    }

    private fun executeSelectionCutClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionCut)
            activePanel = null
        }
    }

    private fun executeSelectionCopyClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionCopy)
            activePanel = null
        }
    }

    private fun executeSelectionFlipHorizontalClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionFlipHorizontal)
            activePanel = null
        }
    }

    private fun executeSelectionFlipVerticalClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionFlipVertical)
            activePanel = null
        }
    }

    private fun executeSelectionRotateCwClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionRotateCw)
            activePanel = null
        }
    }

    private fun executeSelectionRotateCcwClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionRotateCcw)
            activePanel = null
        }
    }

    private fun executeSelectionFillClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionFill)
            activePanel = null
        }
    }

    private fun executeSelectionClearClick() {
        if (state.selectionMenu.isInteractable) {
            bpeEngine.execute(BpeAction.SelectionClear)
            activePanel = null
        }
    }

    private fun executeLayersClick() {
        activePanel = if (activePanel == Panel.Layers) null else Panel.Layers
        layerTypePanel = null
    }

    private fun executeToolboxPaintClick() {
        if (state.toolboxPaint.isInteractable) {
            activePanel = if (bpeEngine.state.toolboxTool != BpeTool.Paint || activePanel == Panel.Shapes) {
                null
            } else {
                Panel.Shapes
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
            activePanel = if (bpeEngine.state.toolboxTool != BpeTool.Erase || activePanel == Panel.Shapes) {
                null
            } else {
                Panel.Shapes
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
        if (state.selectionPaste.isInteractable) {
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

    private fun executePanelColorClick(action: UiAction.PanelColorClick) {
        when (activePanel) {
            Panel.BackgroundBorder -> bpeEngine.execute(BpeAction.PaletteSetBackgroundBorder(action.color))
            Panel.BackgroundPaper -> bpeEngine.execute(BpeAction.PaletteSetBackgroundPaper(action.color))
            Panel.PaintSciiPaper -> bpeEngine.execute(BpeAction.PaletteSetPaintSciiPaper(action.color))
            Panel.PaintSciiInk -> bpeEngine.execute(BpeAction.PaletteSetPaintSciiInk(action.color))
            Panel.PaintBlockColor -> bpeEngine.execute(BpeAction.PaletteSetPaintBlockColor(action.color))
            else -> Unit
        }

        activePanel = null
    }

    private fun executePanelLightClick(action: UiAction.PanelLightClick) {
        when (activePanel) {
            Panel.BackgroundBright -> bpeEngine.execute(BpeAction.PaletteSetBackgroundBright(action.light))
            Panel.PaintSciiBright -> bpeEngine.execute(BpeAction.PaletteSetPaintSciiBright(action.light))
            Panel.PaintSciiFlash -> bpeEngine.execute(BpeAction.PaletteSetPaintSciiFlash(action.light))
            Panel.PaintBlockBright -> bpeEngine.execute(BpeAction.PaletteSetPaintBlockBright(action.light))
            else -> Unit
        }

        activePanel = null
    }

    private fun executePanelCharClick(action: UiAction.PanelCharClick) {
        bpeEngine.execute(BpeAction.PaletteSetPaintSciiChar(action.character))
        activePanel = null
    }

    private fun executePanelEraseClick(action: UiAction.PanelEraseClick) {
        when (activePanel) {
            Panel.EraseSciiPaper -> bpeEngine.execute(BpeAction.PaletteSetEraseSciiPaper(action.shouldErase))
            Panel.EraseSciiInk -> bpeEngine.execute(BpeAction.PaletteSetEraseSciiInk(action.shouldErase))
            Panel.EraseSciiBright -> bpeEngine.execute(BpeAction.PaletteSetEraseSciiBright(action.shouldErase))
            Panel.EraseSciiFlash -> bpeEngine.execute(BpeAction.PaletteSetEraseSciiFlash(action.shouldErase))
            Panel.EraseSciiChar -> bpeEngine.execute(BpeAction.PaletteSetEraseSciiChar(action.shouldErase))
            Panel.EraseBlockColor -> bpeEngine.execute(BpeAction.PaletteSetEraseBlockColor(action.shouldErase))
            Panel.EraseBlockBright -> bpeEngine.execute(BpeAction.PaletteSetEraseBlockBright(action.shouldErase))
            else -> Unit
        }

        activePanel = null
    }

    private fun executePanelShapeClick(action: UiAction.PanelShapeClick) {
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

    private fun executeLayerItemMaskedClick(action: UiAction.LayerItemMaskedClick) {
        bpeEngine.execute(BpeAction.LayersSetMasked(action.layerUid, !action.isLocked))
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

        return (x / currentAreaSpec.cellWidth - if (x < 0) 1 else 0) to (y / currentAreaSpec.cellHeight - if (y < 0) 1 else 0)
    }

    private fun drawingToCursorSpec(areaSpec: AreaSpec, drawingX: Int, drawingY: Int) = CursorSpec(
        primaryArea = drawingToArea(areaSpec, UiAreaType.PrimaryCursor, drawingX, drawingY),
        secondaryArea = if (areaSpec.sciiXMultiplier != 1.0 || areaSpec.sciiYMultiplier != 1.0) {
            UiArea(
                UiSpec.BORDER_SIZE + (drawingX * areaSpec.sciiXMultiplier).toInt() * UiSpec.SCII_CELL_SIZE,
                UiSpec.BORDER_SIZE + (drawingY * areaSpec.sciiYMultiplier).toInt() * UiSpec.SCII_CELL_SIZE,
                UiSpec.SCII_CELL_SIZE,
                UiSpec.SCII_CELL_SIZE,
                UiAreaType.SecondaryCursor,
            )
        } else {
            null
        },
        informerSciiX = drawingX * areaSpec.sciiXMultiplier,
        informerSciiY = drawingY * areaSpec.sciiYMultiplier,
    )

    private fun drawingToArea(
        areaSpec: AreaSpec,
        areaType: UiAreaType,
        drawingX: Int,
        drawingY: Int,
        drawingWidth: Int = 1,
        drawingHeight: Int = 1,
    ) = UiArea(
        UiSpec.BORDER_SIZE + drawingX * areaSpec.cellWidth,
        UiSpec.BORDER_SIZE + drawingY * areaSpec.cellHeight,
        drawingWidth * areaSpec.cellWidth,
        drawingHeight * areaSpec.cellHeight,
        areaType,
    )

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isDrawingInside(drawingX: Int, drawingY: Int) =
        drawingX in 0..currentAreaSpec.drawingEX && drawingY in 0..currentAreaSpec.drawingEY

    private fun computeAreaSpec(canvasType: CanvasType?) = when (canvasType) {
        null -> AreaSpec(
            cellWidth = UiSpec.PICTURE_WIDTH,
            cellHeight = UiSpec.PICTURE_HEIGHT,
            drawingEX = 0,
            drawingEY = 0,
            sciiXMultiplier = 0.0,
            sciiYMultiplier = 0.0,
        )

        is CanvasType.Scii -> AreaSpec(
            cellWidth = UiSpec.SCII_CELL_SIZE,
            cellHeight = UiSpec.SCII_CELL_SIZE,
            drawingEX = GraphicsEngine.SCREEN_SCII_WIDTH - 1,
            drawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT - 1,
            sciiXMultiplier = 1.0,
            sciiYMultiplier = 1.0,
        )

        is CanvasType.HBlock -> AreaSpec(
            cellWidth = UiSpec.SCII_CELL_SIZE,
            cellHeight = UiSpec.BLOCK_CELL_SIZE,
            drawingEX = GraphicsEngine.SCREEN_SCII_WIDTH - 1,
            drawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT * 2 - 1,
            sciiXMultiplier = 1.0,
            sciiYMultiplier = 0.5,
        )

        is CanvasType.VBlock -> AreaSpec(
            cellWidth = UiSpec.BLOCK_CELL_SIZE,
            cellHeight = UiSpec.SCII_CELL_SIZE,
            drawingEX = GraphicsEngine.SCREEN_SCII_WIDTH * 2 - 1,
            drawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT - 1,
            sciiXMultiplier = 0.5,
            sciiYMultiplier = 1.0,
        )

        is CanvasType.QBlock -> AreaSpec(
            cellWidth = UiSpec.BLOCK_CELL_SIZE,
            cellHeight = UiSpec.BLOCK_CELL_SIZE,
            drawingEX = GraphicsEngine.SCREEN_SCII_WIDTH * 2 - 1,
            drawingEY = GraphicsEngine.SCREEN_SCII_HEIGHT * 2 - 1,
            sciiXMultiplier = 0.5,
            sciiYMultiplier = 0.5,
        )
    }

    private fun refresh(): UiState {
        val bpeState = bpeEngine.state.also(bpeStateFlow::tryEmit)
        val cursorSpec = this.cursorSpec

        currentDrawingType = bpeState.drawingType
        currentAreaSpec = computeAreaSpec(bpeState.drawingType)

        val isPaintAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Paint)
        val isEraseAvailable = bpeState.toolboxAvailTools.contains(BpeTool.Erase)

        isPaintActive = bpeState.toolboxTool == BpeTool.Paint && isPaintAvailable
        isEraseActive = bpeState.toolboxTool == BpeTool.Erase && isEraseAvailable

        return UiState(
            sheet = UiSheetView(bpeState.background, bpeState.canvas),

            areas = listOfNotNull(
                bpeState.selection?.let {
                    drawingToArea(
                        computeAreaSpec(it.canvasType),
                        UiAreaType.Selection,
                        it.drawingBox.lx,
                        it.drawingBox.ly,
                        it.drawingBox.width,
                        it.drawingBox.height,
                    )
                },
                cursorSpec?.secondaryArea,
                cursorSpec?.primaryArea,
            ),

            paletteColor = makeToolState(
                when {
                    currentDrawingType.isBlock && isPaintActive -> bpeState.palettePaintBlockColor to Panel.PaintBlockColor
                    currentDrawingType.isBlock && isEraseActive -> makeColor(bpeState.paletteEraseBlockColor) to Panel.EraseBlockColor
                    else -> null
                }
            ),
            palettePaper = makeToolState(
                when {
                    currentDrawingType == CanvasType.Scii && isPaintActive -> bpeState.palettePaintSciiPaper to Panel.PaintSciiPaper
                    currentDrawingType == CanvasType.Scii && isEraseActive -> makeColor(bpeState.paletteEraseSciiPaper) to Panel.EraseSciiPaper
                    currentDrawingType == null -> bpeState.paletteBackgroundBorder to Panel.BackgroundBorder
                    else -> null
                }
            ),
            paletteInk = makeToolState(
                when {
                    currentDrawingType == CanvasType.Scii && isPaintActive -> bpeState.palettePaintSciiInk to Panel.PaintSciiInk
                    currentDrawingType == CanvasType.Scii && isEraseActive -> makeColor(bpeState.paletteEraseSciiInk) to Panel.EraseSciiInk
                    currentDrawingType == null -> bpeState.paletteBackgroundPaper to Panel.BackgroundPaper
                    else -> null
                }
            ),
            paletteBright = makeToolState(
                when {
                    currentDrawingType == CanvasType.Scii && isPaintActive -> bpeState.palettePaintSciiBright to Panel.PaintSciiBright
                    currentDrawingType == CanvasType.Scii && isEraseActive -> makeLight(bpeState.paletteEraseSciiBright) to Panel.EraseSciiBright
                    currentDrawingType.isBlock && isPaintActive -> bpeState.palettePaintBlockBright to Panel.PaintBlockBright
                    currentDrawingType.isBlock && isEraseActive -> makeLight(bpeState.paletteEraseBlockBright) to Panel.EraseBlockBright
                    currentDrawingType == null -> bpeState.paletteBackgroundBright to Panel.BackgroundBright
                    else -> null
                }
            ),
            paletteFlash = makeToolState(
                when {
                    currentDrawingType == CanvasType.Scii && isPaintActive -> bpeState.palettePaintSciiFlash to Panel.PaintSciiFlash
                    currentDrawingType == CanvasType.Scii && isEraseActive -> makeLight(bpeState.paletteEraseSciiFlash) to Panel.EraseSciiFlash
                    else -> null
                }
            ),
            paletteChar = makeToolState(
                when {
                    currentDrawingType == CanvasType.Scii && isPaintActive -> bpeState.palettePaintSciiChar to Panel.PaintSciiChar
                    currentDrawingType == CanvasType.Scii && isEraseActive -> makeChar(bpeState.paletteEraseSciiChar) to Panel.EraseSciiChar
                    else -> null
                }
            ),

            selectionPaste = if (bpeState.toolboxCanPaste) UiToolState.Visible(Unit) else UiToolState.Hidden,
            selectionMenu = when {
                activePanel == Panel.SelectionMenu -> UiToolState.Active(Unit)
                bpeState.selectionIsActionable -> UiToolState.Visible(Unit)
                else -> UiToolState.Hidden
            },
            layers = if (activePanel == Panel.Layers) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            toolboxPaint = when {
                isPaintActive -> UiToolState.Hidden
                isPaintAvailable -> UiToolState.Visible(Unit)
                else -> UiToolState.Disabled(Unit)
            },
            toolboxShape = when {
                !(isPaintActive || isEraseActive) || bpeState.toolboxShape == null -> UiToolState.Hidden

                activePanel != Panel.Shapes && activePanel?.placement == PanelPlacement.Toolbox -> UiToolState.Visible(
                    bpeState.toolboxShape,
                    if (isPaintActive) TextRes.ToolPaint else TextRes.ToolErase,
                )

                else -> UiToolState.Active(
                    bpeState.toolboxShape,
                    if (isPaintActive) TextRes.ToolPaint else TextRes.ToolErase,
                )
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

            toolboxUndo = if (bpeState.toolboxCanUndo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            toolboxRedo = if (bpeState.toolboxCanRedo) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            toolboxMode = bpeState.paintingMode,
            menu = if (activePanel == Panel.Menu) UiToolState.Active(Unit) else UiToolState.Visible(Unit),

            activePanel = when (activePanel) {
                null -> null

                Panel.BackgroundBorder -> UiPanel.Colors(bpeState.paletteBackgroundBorder ?: SciiColor.Transparent)
                Panel.BackgroundPaper -> UiPanel.Colors(bpeState.paletteBackgroundPaper ?: SciiColor.Transparent)
                Panel.BackgroundBright -> UiPanel.Lights(bpeState.paletteBackgroundBright ?: SciiLight.Transparent)

                Panel.PaintSciiInk -> UiPanel.Colors(bpeState.palettePaintSciiInk ?: SciiColor.Transparent)
                Panel.PaintSciiPaper -> UiPanel.Colors(bpeState.palettePaintSciiPaper ?: SciiColor.Transparent)
                Panel.PaintSciiBright -> UiPanel.Lights(bpeState.palettePaintSciiBright ?: SciiLight.Transparent)
                Panel.PaintSciiFlash -> UiPanel.Lights(bpeState.palettePaintSciiFlash ?: SciiLight.Transparent)
                Panel.PaintSciiChar -> UiPanel.Chars(bpeState.palettePaintSciiChar ?: SciiChar.Transparent)
                Panel.PaintBlockColor -> UiPanel.Colors(bpeState.palettePaintBlockColor ?: SciiColor.Transparent)
                Panel.PaintBlockBright -> UiPanel.Lights(bpeState.palettePaintBlockBright ?: SciiLight.Transparent)

                Panel.EraseSciiInk -> UiPanel.Erase(bpeState.paletteEraseSciiInk ?: true)
                Panel.EraseSciiPaper -> UiPanel.Erase(bpeState.paletteEraseSciiPaper ?: true)
                Panel.EraseSciiBright -> UiPanel.Erase(bpeState.paletteEraseSciiBright ?: true)
                Panel.EraseSciiFlash -> UiPanel.Erase(bpeState.paletteEraseSciiFlash ?: true)
                Panel.EraseSciiChar -> UiPanel.Erase(bpeState.paletteEraseSciiChar ?: true)
                Panel.EraseBlockColor -> UiPanel.Erase(bpeState.paletteEraseBlockColor ?: true)
                Panel.EraseBlockBright -> UiPanel.Erase(bpeState.paletteEraseBlockBright ?: true)

                Panel.SelectionMenu -> UiPanel.SelectionMenu
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
            layersCreateCancel = if (layerTypePanel == LayerTypePanel.Create) {
                UiToolState.Active(Unit)
            } else {
                UiToolState.Hidden
            },
            layersMerge = if (bpeState.layersCanMerge) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersConvert = when {
                !bpeState.layersCanConvert -> UiToolState.Disabled(Unit)
                layerTypePanel == LayerTypePanel.Convert -> UiToolState.Active(Unit)
                else -> UiToolState.Visible(Unit)
            },
            layersConvertCancel = if (layerTypePanel == LayerTypePanel.Convert) {
                UiToolState.Active(Unit)
            } else {
                UiToolState.Hidden
            },
            layersDelete = if (bpeState.layersCanDelete) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersMoveUp = if (bpeState.layersCanMoveUp) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersMoveDown = if (bpeState.layersCanMoveDown) UiToolState.Visible(Unit) else UiToolState.Disabled(Unit),
            layersTypesIsVisible = layerTypePanel != null,

            informerPrimary = bpeState.informer?.let {
                TextDescriptor(
                    TextRes.InformerPrimary,
                    buildMap {
                        put("w", (it.rect.width * currentAreaSpec.sciiXMultiplier).toString())
                        put("h", (it.rect.height * currentAreaSpec.sciiYMultiplier).toString())
                    },
                )
            },
            informerSecondary = if (bpeState.drawingType != null && cursorSpec != null) {
                TextDescriptor(
                    TextRes.InformerSecondary,
                    buildMap {
                        put("x", cursorSpec.informerSciiX.toString())
                        put("y", cursorSpec.informerSciiY.toString())
                    },
                )
            } else {
                null
            },

            historySteps = bpeState.historySteps,
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun makeColor(shouldErase: Boolean?) = when (shouldErase) {
        null -> null
        true -> SciiColor.ForceTransparent
        false -> SciiColor.Transparent
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun makeLight(shouldErase: Boolean?) = when (shouldErase) {
        null -> null
        true -> SciiLight.ForceTransparent
        false -> SciiLight.Transparent
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun makeChar(shouldErase: Boolean?) = when (shouldErase) {
        null -> null
        true -> SciiChar.ForceTransparent
        false -> SciiChar.Transparent
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> makeToolState(pair: Pair<T?, Panel?>?) = when {
        pair == null || pair.first == null -> UiToolState.Hidden
        activePanel == pair.second -> UiToolState.Active(pair.first)
        else -> UiToolState.Visible(pair.first)
    } as UiToolState<T>

    private class Packer(private val historyStepsLimit: Int = -1) : BagStuffPacker<UiEngineImpl> {
        override val putInTheBagVersion = 3

        override fun putInTheBag(bag: PackableBag, value: UiEngineImpl) {
            bag.put(BpeEngine.Packer(historyStepsLimit), value.bpeEngine)
        }
    }

    private inner class Unpacker : BagStuffUnpacker<UiEngineImpl> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): UiEngineImpl {
            requireSupportedStuffVersion("BpeEngine", 3, version)

            if (version < 3) {
                bpeEngine.selfUnpacker().getOutOfTheBag(version, bag)
            } else {
                bag.getStuff(bpeEngine.selfUnpacker())
            }

            state = refresh()
            return this@UiEngineImpl
        }
    }
}

private enum class PanelPlacement {
    Palette,
    Toolbox,
}

private enum class Panel(val placement: PanelPlacement) {
    BackgroundBorder(PanelPlacement.Palette),
    BackgroundPaper(PanelPlacement.Palette),
    BackgroundBright(PanelPlacement.Palette),

    PaintSciiInk(PanelPlacement.Palette),
    PaintSciiPaper(PanelPlacement.Palette),
    PaintSciiBright(PanelPlacement.Palette),
    PaintSciiFlash(PanelPlacement.Palette),
    PaintSciiChar(PanelPlacement.Palette),
    PaintBlockColor(PanelPlacement.Palette),
    PaintBlockBright(PanelPlacement.Palette),

    EraseSciiInk(PanelPlacement.Palette),
    EraseSciiPaper(PanelPlacement.Palette),
    EraseSciiBright(PanelPlacement.Palette),
    EraseSciiFlash(PanelPlacement.Palette),
    EraseSciiChar(PanelPlacement.Palette),
    EraseBlockColor(PanelPlacement.Palette),
    EraseBlockBright(PanelPlacement.Palette),

    SelectionMenu(PanelPlacement.Palette),
    Layers(PanelPlacement.Palette),
    Shapes(PanelPlacement.Toolbox),
    Menu(PanelPlacement.Toolbox),
}

private enum class LayerTypePanel {
    Create,
    Convert,
}

private data class AreaSpec(
    val cellWidth: Int,
    val cellHeight: Int,
    val drawingEX: Int,
    val drawingEY: Int,
    val sciiXMultiplier: Double,
    val sciiYMultiplier: Double,
)

private data class CursorSpec(val primaryArea: UiArea, val secondaryArea: UiArea?, val informerSciiX: Double, val informerSciiY: Double)
