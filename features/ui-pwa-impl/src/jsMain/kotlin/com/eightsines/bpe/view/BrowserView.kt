package com.eightsines.bpe.view

import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.presentation.BpePaintingMode
import com.eightsines.bpe.presentation.BpeShape
import com.eightsines.bpe.presentation.LayerView
import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiArea
import com.eightsines.bpe.presentation.UiPanel
import com.eightsines.bpe.presentation.UiSheetView
import com.eightsines.bpe.presentation.UiToolState
import com.eightsines.bpe.util.ElapsedTimeProvider
import com.eightsines.bpe.util.KeyModifier
import com.eightsines.bpe.util.ResourceManager
import com.eightsines.bpe.util.TextRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.dom.addClass
import kotlinx.dom.createElement
import kotlinx.dom.removeClass
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.TouchList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

class BrowserView(
    private val document: Document,
    private val elapsedTimeProvider: ElapsedTimeProvider,
    private val renderer: BrowserRenderer,
    private val resourceManager: ResourceManager,
    private val sheetController: BrowserSheetController,
) {
    private val _actionFlow = MutableSharedFlow<BrowserAction>(extraBufferCapacity = 4)

    val actionFlow: Flow<BrowserAction>
        get() = _actionFlow

    private val loading = document.find<HTMLElement>(".js-loading")
    private val container = document.find<HTMLElement>(".js-container")
    private val drawing = document.find<HTMLElement>(".js-drawing")
    private val drawingSheet = document.find<HTMLCanvasElement>(BrowserSheetController.SELECTOR_DRAWING_SHEET)
    private val drawingAreas = document.find<HTMLCanvasElement>(".js-drawing-areas")

    private val paletteBlockColor = document.find<HTMLElement>(".js-palette-color")
    private val paletteBlockColorIndicator = document.find<HTMLElement>(".js-palette-color-indicator")
    private val paletteSciiPaper = document.find<HTMLElement>(".js-palette-paper")
    private val paletteSciiPaperIndicator = document.find<HTMLElement>(".js-palette-paper-indicator")
    private val paletteSciiInk = document.find<HTMLElement>(".js-palette-ink")
    private val paletteSciiInkIndicator = document.find<HTMLElement>(".js-palette-ink-indicator")
    private val paletteSciiBright = document.find<HTMLElement>(".js-palette-bright")
    private val paletteSciiFlash = document.find<HTMLElement>(".js-palette-flash")
    private val paletteSciiChar = document.find<HTMLElement>(".js-palette-char")
    private val paletteSciiCharIndicator = document.find<HTMLElement>(".js-palette-char-indicator")

    private val selectionMenu = document.find<HTMLElement>(".js-selection-menu")
    private val selectionCut = document.find<HTMLElement>(".js-selection-cut")
    private val selectionCopy = document.find<HTMLElement>(".js-selection-copy")
    private val selectionPasteOuter = document.find<HTMLElement>(".js-selection-paste-outer")
    private val selectionPasteMenu = document.find<HTMLElement>(".js-selection-paste-menu")
    private val selectionFlipHorizontal = document.find<HTMLElement>(".js-selection-flip-horizontal")
    private val selectionFlipVertical = document.find<HTMLElement>(".js-selection-flip-vertical")
    private val selectionRotateCw = document.find<HTMLElement>(".js-selection-rotate-cw")
    private val selectionRotateCcw = document.find<HTMLElement>(".js-selection-rotate-ccw")
    private val selectionFill = document.find<HTMLElement>(".js-selection-fill")
    private val selectionClear = document.find<HTMLElement>(".js-selection-clear")
    private val layers = document.find<HTMLElement>(".js-layers")

    private val toolboxPaint = document.find<HTMLElement>(".js-toolbox-paint")
    private val toolboxShape = document.find<HTMLElement>(".js-toolbox-shape")
    private val toolboxErase = document.find<HTMLElement>(".js-toolbox-erase")
    private val toolboxSelect = document.find<HTMLElement>(".js-toolbox-select")
    private val toolboxPickColor = document.find<HTMLElement>(".js-toolbox-pick-color")

    private val toolboxUndo = document.find<HTMLElement>(".js-toolbox-undo")
    private val toolboxRedo = document.find<HTMLElement>(".js-toolbox-redo")
    private val toolboxMode = document.find<HTMLElement>(".js-toolbox-mode")

    private val menu = document.find<HTMLElement>(".js-menu")
    private val menuNew = document.find<HTMLElement>(".js-menu-new")
    private val menuLoad = document.find<HTMLInputElement>(".js-menu-load")
    private val menuSave = document.find<HTMLElement>(".js-menu-save")
    private val menuExportTap = document.find<HTMLElement>(".js-menu-export-tap")
    private val menuExportScr = document.find<HTMLElement>(".js-menu-export-scr")
    private val menuExportPng = document.find<HTMLElement>(".js-menu-export-png")

    private val colorsPanel = document.find<HTMLElement>(".js-colors-panel")
    private val lightsPanel = document.find<HTMLElement>(".js-lights-panel")
    private val charsPanel = document.find<HTMLElement>(".js-chars-panel")
    private val erasePanel = document.find<HTMLElement>(".js-erase-panel")
    private val eraseOff = document.find<HTMLElement>(".js-erase-off")
    private val eraseOn = document.find<HTMLElement>(".js-erase-on")
    private val selectionPanel = document.find<HTMLElement>(".js-selection-panel")

    private val colorItems = mutableMapOf<SciiColor, Element>()
    private val lightItems = mutableMapOf<SciiLight, Element>()
    private val charItems = mutableMapOf<SciiChar, Element>()

    private val layersPanel = document.find<HTMLElement>(".js-layers-panel")
    private val layersToolbarPrimary = document.find<HTMLElement>(".js-layers-toolbar-primary")
    private val layersToolbarTypes = document.find<HTMLElement>(".js-layers-toolbar-types")
    private val layersItems = document.find<HTMLElement>(".js-layers-items")
    private val layersTypes = document.find<HTMLElement>(".js-layers-types")
    private val layersCreate = document.find<HTMLElement>(".js-layers-create")
    private val layersCreateCancel = document.find<HTMLElement>(".js-layers-create-cancel")
    private val layersMerge = document.find<HTMLElement>(".js-layers-merge")
    private val layersConvert = document.find<HTMLElement>(".js-layers-convert")
    private val layersConvertCancel = document.find<HTMLElement>(".js-layers-convert-cancel")
    private val layersDelete = document.find<HTMLElement>(".js-layers-delete")
    private val layersMoveUp = document.find<HTMLElement>(".js-layers-move-up")
    private val layersMoveDown = document.find<HTMLElement>(".js-layers-move-down")

    private val shapesPanel = document.find<HTMLElement>(".js-shapes-panel")
    private val shapesPoint = document.find<HTMLElement>(".js-shape-point")
    private val shapesLine = document.find<HTMLElement>(".js-shape-line")
    private val shapesStrokeBox = document.find<HTMLElement>(".js-shape-stroke-box")
    private val shapesFillBox = document.find<HTMLElement>(".js-shape-fill-box")
    private val shapesStrokeEllipse = document.find<HTMLElement>(".js-shape-stroke-ellipse")
    private val shapesFillEllipse = document.find<HTMLElement>(".js-shape-fill-ellipse")

    private val menuPanel = document.find<HTMLElement>(".js-menu-panel")
    private val informer = document.find<HTMLElement>(".js-informer")

    private val dialog = document.find<HTMLElement>(".js-dialog")
    private val dialogBackground = document.find<HTMLElement>(".js-dialog-background")
    private val dialogAlert = document.find<HTMLElement>(".js-dialog-alert")
    private val dialogConfirm = document.find<HTMLElement>(".js-dialog-confirm")
    private val dialogConfirmMessage = document.find<HTMLElement>(".js-dialog-confirm-message")
    private val dialogConfirmOk = document.find<HTMLElement>(".js-dialog-confirm-ok")
    private val dialogConfirmCancel = document.find<HTMLElement>(".js-dialog-confirm-cancel")
    private val dialogPrompt = document.find<HTMLElement>(".js-dialog-prompt")
    private val dialogPromptMessage = document.find<HTMLElement>(".js-dialog-prompt-message")
    private val dialogPromptInput = document.find<HTMLInputElement>(".js-dialog-prompt-input")
    private val dialogPromptOk = document.find<HTMLElement>(".js-dialog-prompt-ok")
    private val dialogPromptCancel = document.find<HTMLElement>(".js-dialog-prompt-cancel")
    private val dialogPromptHint = document.find<HTMLElement>(".js-dialog-prompt-hint")

    private var layersItemsCache = mutableMapOf<LayerView<*>, CachedLayerItem>()
    private var sheetViewCache: UiSheetView? = null
    private var areasCache: List<UiArea> = emptyList()

    private var wasRendered = false
    private var lastRefreshTimeMs = 0L
    private var activeDialog: BrowserDialog? = null
    private var drawingTransform: DrawingTransform? = null

    init {
        createColorItems()
        createLightItems()
        createCharItems()
        createLayerTypeItems()

        attachDrawingTouchEvents()
        attachDrawingMouseEvents()

        paletteBlockColor.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteInkOrColorClick)) }
        paletteSciiPaper.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PalettePaperClick)) }
        paletteSciiInk.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteInkOrColorClick)) }
        paletteSciiBright.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteBrightClick)) }
        paletteSciiFlash.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteFlashClick)) }
        paletteSciiChar.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteCharClick)) }

        selectionPasteOuter.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPasteClick)) }
        selectionMenu.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionMenuClick)) }
        selectionCut.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionCutClick)) }
        selectionCopy.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionCopyClick)) }
        selectionPasteMenu.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPasteClick)) }
        selectionFlipHorizontal.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionFlipHorizontalClick)) }
        selectionFlipVertical.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionFlipVerticalClick)) }
        selectionRotateCw.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionRotateCwClick)) }
        selectionRotateCcw.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionRotateCcwClick)) }
        selectionFill.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionFillClick)) }
        selectionClear.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionClearClick)) }
        layers.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayersClick)) }

        toolboxPaint.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPaintClick)) }
        toolboxShape.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxShapeClick)) }
        toolboxErase.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxEraseClick)) }
        toolboxSelect.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxSelectClick)) }
        toolboxPickColor.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPickColorClick)) }

        toolboxUndo.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxUndoClick)) }
        toolboxRedo.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxRedoClick)) }

        eraseOff.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelEraseClick(false))) }
        eraseOn.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelEraseClick(true))) }

        shapesPoint.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.Point))) }
        shapesLine.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.Line))) }
        shapesStrokeBox.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.StrokeBox))) }
        shapesFillBox.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.FillBox))) }
        shapesStrokeEllipse.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.StrokeEllipse))) }
        shapesFillEllipse.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelShapeClick(BpeShape.FillEllipse))) }

        toolboxMode.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaintingModeClick)) }

        menu.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.MenuClick)) }
        menuNew.addClickListener { _actionFlow.tryEmit(BrowserAction.PaintingNew) }

        menuLoad.addEventListener(
            EVENT_CHANGE,
            {
                it.stopPropagation()
                _actionFlow.tryEmit(BrowserAction.PaintingLoad(menuLoad))
            },
        )

        menuSave.addClickListener { _actionFlow.tryEmit(BrowserAction.PaintingSave) }
        menuExportTap.addClickListener { _actionFlow.tryEmit(BrowserAction.PaintingExportTap) }
        menuExportScr.addClickListener { _actionFlow.tryEmit(BrowserAction.PaintingExportScr) }
        menuExportPng.addClickListener { _actionFlow.tryEmit(BrowserAction.PaintingExportPng) }

        layersCreate.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerCreateClick)) }
        layersCreateCancel.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerCreateClick)) }
        layersMerge.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMergeClick)) }
        layersConvert.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerConvertClick)) }
        layersConvertCancel.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerConvertClick)) }
        layersDelete.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerDeleteClick)) }
        layersMoveUp.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMoveUpClick)) }
        layersMoveDown.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMoveDownClick)) }

        dialogBackground.addClickListener { _actionFlow.tryEmit(BrowserAction.DialogHide) }
        dialogAlert.addClickListener { _actionFlow.tryEmit(BrowserAction.DialogHide) }

        dialogConfirmOk.addClickListener { _actionFlow.tryEmit(BrowserAction.DialogOk) }
        dialogConfirmCancel.addClickListener { _actionFlow.tryEmit(BrowserAction.DialogHide) }

        dialogPromptOk.addClickListener {
            if (activeDialog is BrowserDialog.Prompt) {
                _actionFlow.tryEmit(BrowserAction.DialogPromptInput(dialogPromptInput.value))
                _actionFlow.tryEmit(BrowserAction.DialogOk)
            }
        }

        dialogPromptCancel.addClickListener { _actionFlow.tryEmit(BrowserAction.DialogHide) }

        for (element in document.findAll<HTMLElement>("[bpe-title]")) {
            element.title = element.getAttribute("bpe-title")?.let { resId ->
                TextRes.of(resId)?.let { resourceManager.resolveText(it) } ?: resId
            } ?: ""
        }

        for (element in document.findAll<HTMLElement>("[bpe-text]")) {
            element.textContent = element.getAttribute("bpe-text")?.let { resId ->
                TextRes.of(resId)?.let { resourceManager.resolveText(it) } ?: resId
            } ?: ""
        }

        document.addEventListener(
            EVENT_KEY_DOWN,
            {
                it as KeyboardEvent

                val browserKey = if (!it.repeat) {
                    if (activeDialog is BrowserDialog.Prompt) {
                        _actionFlow.tryEmit(BrowserAction.DialogPromptInput(dialogPromptInput.value))
                    }

                    val browserKey = BrowserKey(it.keyCode, getKeyModifiers(it))
                    _actionFlow.tryEmit(BrowserAction.KeyDown(browserKey))

                    browserKey
                } else {
                    null
                }

                if (activeDialog == null && (BROWSER_HOTKEYS.contains(browserKey) || BROWSER_HANDLED_KEYS.contains(browserKey))) {
                    it.stopPropagation()
                    it.preventDefault()
                }
            },
        )

        document.addEventListener(
            EVENT_KEY_UP,
            {
                it as KeyboardEvent

                if (activeDialog is BrowserDialog.Prompt) {
                    _actionFlow.tryEmit(BrowserAction.DialogPromptInput(dialogPromptInput.value))
                }

                val browserKey = BrowserKey(it.keyCode, getKeyModifiers(it))
                _actionFlow.tryEmit(BrowserAction.KeyUp(browserKey))

                if (activeDialog == null && (BROWSER_HOTKEYS.contains(browserKey) || BROWSER_HANDLED_KEYS.contains(browserKey))) {
                    it.stopPropagation()
                    it.preventDefault()
                }
            },
        )
    }

    fun render(state: BrowserState) {
        val uiState = state.uiState

        if (!wasRendered) {
            loading.addClass(CLASS_HIDDEN)
            container.removeClass(CLASS_HIDDEN)
        }

        drawingTransform = state.transform
        reposition()

        paletteBlockColor.setToolState(uiState.paletteColor) {
            paletteBlockColorIndicator.replaceClassModifier("tool__color--", getColorClassSuffix(it))
        }

        paletteSciiPaper.setToolState(uiState.palettePaper) {
            paletteSciiPaperIndicator.replaceClassModifier("tool__color_paper--", getColorClassSuffix(it))
        }

        paletteSciiInk.setToolState(uiState.paletteInk) {
            paletteSciiInkIndicator.replaceClassModifier("tool__color_ink--", getColorClassSuffix(it))
        }

        paletteSciiBright.setToolState(uiState.paletteBright) {
            paletteSciiBright.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteSciiFlash.setToolState(uiState.paletteFlash) {
            paletteSciiFlash.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteSciiChar.setToolState(uiState.paletteChar) {
            paletteSciiCharIndicator.replaceClassModifier("tool__char--", getCharClassSuffix(it))
        }

        selectionPasteOuter.setToolState(
            if (uiState.selectionMenu is UiToolState.Hidden) uiState.selectionPaste else UiToolState.Hidden
        )

        selectionMenu.setToolState(uiState.selectionMenu)
        selectionPasteMenu.setToolState(uiState.selectionPaste)

        layers.setToolState(uiState.layers)

        toolboxPaint.setToolState(uiState.toolboxPaint)

        toolboxShape.setToolState(uiState.toolboxShape) {
            toolboxShape.replaceClassModifier("tool__shape--", getShapeClassSuffix(it))
        }

        toolboxErase.setToolState(uiState.toolboxErase)
        toolboxSelect.setToolState(uiState.toolboxSelect)
        toolboxPickColor.setToolState(uiState.toolboxPickColor)
        toolboxUndo.setToolState(uiState.toolboxUndo)
        toolboxRedo.setToolState(uiState.toolboxRedo)

        toolboxMode.replaceClassModifier("tool__mode--", getPaintingModeClassSuffix(uiState.toolboxMode))
        toolboxMode.title = getPaintingModeTitle(uiState.toolboxMode)

        menu.setToolState(uiState.menu)

        colorsPanel.setVisible(uiState.activePanel is UiPanel.Colors)
        lightsPanel.setVisible(uiState.activePanel is UiPanel.Lights)
        charsPanel.setVisible(uiState.activePanel is UiPanel.Chars)
        erasePanel.setVisible(uiState.activePanel is UiPanel.Erase)
        selectionPanel.setVisible(uiState.activePanel is UiPanel.SelectionMenu)
        layersPanel.setVisible(uiState.activePanel is UiPanel.Layers)
        shapesPanel.setVisible(uiState.activePanel is UiPanel.Shapes)
        menuPanel.setVisible(uiState.activePanel is UiPanel.Menu)

        when (val panel = uiState.activePanel) {
            is UiPanel.Colors ->
                for ((color, element) in colorItems) {
                    element.setActive(color == panel.color)
                }

            is UiPanel.Lights ->
                for ((light, element) in lightItems) {
                    element.setActive(light == panel.light)
                }

            is UiPanel.Chars ->
                for ((character, element) in charItems) {
                    element.setActive(character == panel.character)
                }

            is UiPanel.Erase -> {
                eraseOff.setActive(!panel.shouldErase)
                eraseOn.setActive(panel.shouldErase)
            }

            is UiPanel.Shapes -> {
                shapesPoint.setActive(panel.shape == BpeShape.Point)
                shapesLine.setActive(panel.shape == BpeShape.Line)
                shapesStrokeBox.setActive(panel.shape == BpeShape.StrokeBox)
                shapesFillBox.setActive(panel.shape == BpeShape.FillBox)
                shapesStrokeEllipse.setActive(panel.shape == BpeShape.StrokeEllipse)
                shapesFillEllipse.setActive(panel.shape == BpeShape.FillEllipse)
            }

            null, is UiPanel.SelectionMenu, is UiPanel.Layers, is UiPanel.Menu -> Unit
        }

        renderLayersItems(uiState.layersItems, uiState.layersCurrentUid)

        layersCreate.setToolState(uiState.layersCreate)
        layersCreateCancel.setToolState(uiState.layersCreateCancel)
        layersMerge.setToolState(uiState.layersMerge)
        layersConvert.setToolState(uiState.layersConvert)
        layersConvertCancel.setToolState(uiState.layersConvertCancel)
        layersDelete.setToolState(uiState.layersDelete)
        layersMoveUp.setToolState(uiState.layersMoveUp)
        layersMoveDown.setToolState(uiState.layersMoveDown)
        layersToolbarPrimary.setVisible(!uiState.layersTypesIsVisible)
        layersToolbarTypes.setVisible(uiState.layersTypesIsVisible)

        val sheetView = uiState.sheet
        val areas = uiState.areas

        if (sheetViewCache != sheetView) {
            sheetViewCache = sheetView
            renderer.renderSheet(drawingSheet, sheetView.backgroundView.layer, sheetView.canvasView.canvas)
        }

        if (areasCache != areas) {
            areasCache = areas
            renderer.renderAreas(drawingAreas, areas)
        }

        informer.setVisible(uiState.informerPrimary != null || uiState.informerSecondary != null)

        informer.innerHTML = makeSpan(uiState.informerSecondary?.let(resourceManager::resolveText) ?: "") +
                makeSpan(uiState.informerPrimary?.let(resourceManager::resolveText) ?: "")

        if (state.dialog != null) {
            dialog.setVisible(true)

            if (state.dialog is BrowserDialog.Alert) {
                dialogAlert.setVisible(true)
                dialogAlert.textContent = resourceManager.resolveText(state.dialog.message)
            } else {
                dialogAlert.setVisible(false)
            }

            if (state.dialog is BrowserDialog.Confirm) {
                dialogConfirm.setVisible(true)
                dialogConfirmMessage.textContent = resourceManager.resolveText(state.dialog.message)
            } else {
                dialogConfirm.setVisible(false)
            }

            if (state.dialog is BrowserDialog.Prompt) {
                dialogPrompt.setVisible(true)
                dialogPromptMessage.textContent = resourceManager.resolveText(state.dialog.message)

                if (state.dialog !== activeDialog) {
                    dialogPromptInput.also {
                        it.value = state.dialog.value
                        it.focus()

                        it.selectionEnd = 10000
                        it.selectionStart = 10000
                    }
                }

                dialogPromptHint.setVisible(state.dialog.hint != null)
                dialogPromptHint.textContent = state.dialog.hint?.let(resourceManager::resolveText) ?: ""
            } else {
                dialogPrompt.setVisible(false)
            }
        } else {
            dialog.setVisible(false)
        }

        wasRendered = true
        activeDialog = state.dialog
    }

    fun refresh() {
        if (!wasRendered) {
            return
        }

        val elapsedTimeMs = elapsedTimeProvider.getElapsedTimeMs()

        if (elapsedTimeMs - lastRefreshTimeMs < BrowserRenderer.FLASH_MS) {
            return
        }

        for ((layerView, cachedLayerItem) in layersItemsCache) {
            renderer.renderPreview(cachedLayerItem.previewCanvas, layerView.layer)
        }

        sheetViewCache?.let { renderer.renderSheet(drawingSheet, it.backgroundView.layer, it.canvasView.canvas) }
        renderer.renderAreas(drawingAreas, areasCache)

        lastRefreshTimeMs = elapsedTimeMs
    }

    fun reposition() {
        val bbox = drawingTransform
            ?.let { sheetController.getSheetBbox(drawing.clientWidth, drawing.clientHeight, it) }
            ?: return

        val sheetLeftStyle = "${bbox.lx}px"
        val sheetTopStyle = "${bbox.ly}px"
        val sheetWidthStyle = "${bbox.width}px"
        val sheetHeightStyle = "${bbox.height}px"

        drawingSheet.style.also {
            it.left = sheetLeftStyle
            it.top = sheetTopStyle
            it.width = sheetWidthStyle
            it.height = sheetHeightStyle
        }

        drawingAreas.style.also {
            it.left = sheetLeftStyle
            it.top = sheetTopStyle
            it.width = sheetWidthStyle
            it.height = sheetHeightStyle
        }
    }

    private fun renderLayersItems(layersViews: List<LayerView<*>>, layersCurrentUid: LayerUid) {
        for (item in layersItemsCache.values) {
            layersItems.removeChild(item.element)
        }

        val newLayersItemsCache = mutableMapOf<LayerView<*>, CachedLayerItem>()

        for (layerView in layersViews) {
            val layer = layerView.layer

            newLayersItemsCache[layerView] = layersItemsCache.getOrPut(layerView) {
                val startPane = document
                    .createElement(NAME_DIV) { className = "panel__pane" }
                    .appendChildren(
                        document
                            .createElement(NAME_DIV) {
                                this as HTMLElement
                                className = "tool tool--sm"

                                title = resourceManager.resolveText(
                                    if (layer.isVisible) TextRes.LayerVisible else TextRes.LayerInvisible
                                )

                                addClickListener {
                                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerItemVisibleClick(layer.uid, layer.isVisible)))
                                }
                            }
                            .appendChildren(
                                document.createElement(NAME_IMG) {
                                    this as HTMLImageElement

                                    className = "tool__icon"
                                    src = if (layer.isVisible) SRC_LAYER_VISIBLE else SRC_LAYER_INVISIBLE
                                    alt = ""
                                }
                            ),
                        document
                            .createElement(NAME_DIV) {
                                this as HTMLElement
                                className = "tool tool--sm"

                                title = resourceManager.resolveText(
                                    if (layer.isLocked) TextRes.LayerLocked else TextRes.LayerUnlocked
                                )

                                addClickListener {
                                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerItemLockedClick(layer.uid, layer.isLocked)))
                                }
                            }
                            .appendChildren(
                                document.createElement(NAME_IMG) {
                                    this as HTMLImageElement

                                    className = "tool__icon"
                                    src = if (layer.isLocked) SRC_LAYER_LOCKED else SRC_LAYER_UNLOCKED
                                    alt = ""
                                }
                            ),
                        document
                            .createElement(NAME_DIV) {
                                this as HTMLElement
                                className = "tool tool--sm"

                                if (layer is CanvasLayer<*>) {
                                    title = resourceManager.resolveText(
                                        if (layer.isMasked) TextRes.LayerMasked else TextRes.LayerUnmasked
                                    )

                                    addClickListener {
                                        _actionFlow.tryEmit(
                                            BrowserAction.Ui(UiAction.LayerItemMaskedClick(layer.uid, layer.isMasked)),
                                        )
                                    }
                                }
                            }
                            .appendChildren(
                                (layer as? CanvasLayer<*>?)?.let { canvasLayer ->
                                    document.createElement(NAME_IMG) {
                                        this as HTMLImageElement

                                        className = "tool__icon"
                                        src = if (canvasLayer.isMasked) SRC_LAYER_MASKED else SRC_LAYER_UNMASKED
                                        alt = ""
                                    }
                                }
                            ),
                    )

                val previewCanvas = document.createElement(NAME_CANVAS) {
                    this as HTMLCanvasElement

                    className = "layers__preview"
                    width = PREVIEW_WIDTH
                    height = PREVIEW_HEIGHT

                    renderer.renderPreview(this, layer)
                } as HTMLCanvasElement

                val endPane = document
                    .createElement(NAME_DIV) { className = "panel__pane" }
                    .appendChildren(
                        document.createElement(NAME_DIV) {
                            this as HTMLElement
                            className = "tool tool--sm tool--marker"

                            if (layer is CanvasLayer<*>) {
                                title = getCanvasTypeTitle(layer.canvasType)
                                appendChild(createCanvasTypeIcon(layer.canvasType))
                            }
                        }
                    )

                val element = document
                    .createElement(NAME_DIV) {
                        className = "layers__item"
                        addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerItemClick(layer.uid))) }
                    }
                    .appendChildren(startPane, previewCanvas, endPane)

                CachedLayerItem(element, previewCanvas)
            }
        }

        for ((layerView, cachedLayerItem) in newLayersItemsCache) {
            if (layerView.layer.uid == layersCurrentUid) {
                cachedLayerItem.element.addClass("layers__item--active")
            } else {
                cachedLayerItem.element.removeClass("layers__item--active")
            }

            layersItems.appendChild(cachedLayerItem.element)
        }

        layersItemsCache = newLayersItemsCache
    }

    private fun createColorItems() {
        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(colorsPanel)

        for (color in 0..7) {
            val sciiColor = SciiColor(color)

            document
                .createElement(NAME_DIV) {
                    className = "tool tool--md"
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelColorClick(sciiColor))) }

                }
                .appendChildren(document.createElement(NAME_DIV) { className = "tool__color tool__color--${color}" })
                .appendTo(paneElement)
                .also { colorItems[sciiColor] = it }
        }

        for ((sciiColor, suffix) in listOf(SciiColor.Transparent to SUFFIX_TRANSPARENT, SciiColor.ForceTransparent to SUFFIX_FORCE_TRANSPARENT)) {
            document
                .createElement(NAME_DIV) {
                    className = "tool tool--md"
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelColorClick(sciiColor))) }
                }
                .appendChildren(
                    document.createElement(NAME_IMG) {
                        this as HTMLImageElement

                        className = "tool__indicator tool__indicator--transparent"
                        src = SRC_LIGHT_FORMAT.replace("{}", suffix)
                        alt = ""
                    }
                )
                .appendTo(paneElement)
                .also { colorItems[sciiColor] = it }
        }
    }

    private fun createLightItems() {
        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(lightsPanel)

        val availLights = listOf(
            SciiLight.Off to SUFFIX_OFF,
            SciiLight.On to SUFFIX_ON,
            SciiLight.Transparent to SUFFIX_TRANSPARENT,
            SciiLight.ForceTransparent to SUFFIX_FORCE_TRANSPARENT,
        )

        for ((sciiLight, suffix) in availLights) {
            document
                .createElement(NAME_DIV) {
                    className = "tool tool--md"
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelLightClick(sciiLight))) }
                }
                .appendChildren(
                    document.createElement(NAME_IMG) {
                        this as HTMLImageElement

                        className = "tool__icon tool__icon--light"
                        src = SRC_LIGHT_FORMAT.replace("{}", suffix)
                        alt = ""
                    }
                )
                .appendTo(paneElement)
                .also { lightItems[sciiLight] = it }
        }
    }

    private fun createCharItems() {
        for (row in 0..<7) {
            val paneElement = document
                .createElement(NAME_DIV) { className = "panel__pane" }
                .appendTo(charsPanel)

            for (col in 0..<16) {
                val characterValue = row * 16 + col + 32
                val sciiChar = SciiChar(characterValue)

                document
                    .createElement(NAME_DIV) {
                        className = "tool tool--xs"
                        addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelCharClick(sciiChar))) }
                    }
                    .appendChildren(document.createElement(NAME_DIV) { className = "tool__char tool__char--${characterValue}" })
                    .appendTo(paneElement)
                    .also { charItems[sciiChar] = it }
            }
        }

        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(charsPanel)

        for ((sciiChar, suffix) in listOf(SciiChar.Transparent to SUFFIX_TRANSPARENT, SciiChar.ForceTransparent to SUFFIX_FORCE_TRANSPARENT)) {
            document
                .createElement(NAME_DIV) {
                    className = "tool tool--xs"
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PanelCharClick(sciiChar))) }
                }
                .appendChildren(
                    document.createElement(NAME_IMG) {
                        this as HTMLImageElement

                        className = "tool__indicator tool__indicator--transparent"
                        src = SRC_LIGHT_FORMAT.replace("{}", suffix)
                        alt = ""
                    }
                )
                .appendTo(paneElement)
                .also { charItems[sciiChar] = it }
        }
    }

    private fun createLayerTypeItems() {
        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(layersTypes)

        for (type in listOf(CanvasType.HBlock, CanvasType.VBlock, CanvasType.QBlock, CanvasType.Scii)) {
            document
                .createElement(NAME_DIV) {
                    this as HTMLElement

                    className = "tool tool--md"
                    title = getCanvasTypeTitle(type)

                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerTypeClick(type))) }
                }
                .appendChildren(createCanvasTypeIcon(type))
                .appendTo(paneElement)
        }
    }

    private fun createCanvasTypeIcon(type: CanvasType) = document.createElement(NAME_IMG) {
        this as HTMLImageElement
        className = "tool__icon"

        src = when (type) {
            CanvasType.Scii -> SRC_TYPE_SCII
            CanvasType.HBlock -> SRC_TYPE_HBLOCK
            CanvasType.VBlock -> SRC_TYPE_VBLOCK
            CanvasType.QBlock -> SRC_TYPE_QBLOCK
        }

        alt = ""
    }

    private fun getCanvasTypeTitle(type: CanvasType) = resourceManager.resolveText(
        when (type) {
            CanvasType.Scii -> TextRes.CanvasScii
            CanvasType.HBlock -> TextRes.CanvasHBlock
            CanvasType.VBlock -> TextRes.CanvasVBlock
            CanvasType.QBlock -> TextRes.CanvasQBlock
        }
    )

    private fun attachDrawingTouchEvents() {
        drawing.addEventListener(
            EVENT_TOUCH_START,
            {
                it as TouchEvent
                it.preventDefault()

                if (it.changedTouches.length < 1) {
                    return@addEventListener
                }

                _actionFlow.tryEmit(
                    BrowserAction.DrawingDown(
                        points = it.changedTouches.mapWithLimit { it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop },
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_TOUCH_MOVE,
            {
                it as TouchEvent
                it.preventDefault()

                if (it.changedTouches.length < 1) {
                    return@addEventListener
                }

                _actionFlow.tryEmit(
                    BrowserAction.DrawingMove(
                        points = it.changedTouches.mapWithLimit { it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop },
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_TOUCH_END,
            {
                it as TouchEvent
                it.preventDefault()

                if (it.changedTouches.length < 1) {
                    return@addEventListener
                }

                _actionFlow.tryEmit(
                    BrowserAction.DrawingUp(
                        points = it.changedTouches.mapWithLimit { it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop },
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(EVENT_TOUCH_CANCEL, { _actionFlow.tryEmit(BrowserAction.DrawingLeave) })
    }

    private fun attachDrawingMouseEvents() {
        drawing.addEventListener(
            EVENT_MOUSE_ENTER,
            {
                it as MouseEvent
                it.preventDefault()

                _actionFlow.tryEmit(
                    BrowserAction.DrawingEnter(
                        x = it.clientX - drawing.offsetLeft,
                        y = it.clientY - drawing.offsetTop,
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_MOUSE_DOWN,
            {
                it as MouseEvent
                it.preventDefault()

                _actionFlow.tryEmit(
                    BrowserAction.DrawingDown(
                        points = listOf(it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop),
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_MOUSE_MOVE,
            {
                it as MouseEvent
                it.preventDefault()

                _actionFlow.tryEmit(
                    BrowserAction.DrawingMove(
                        points = listOf(it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop),
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_MOUSE_UP,
            {
                it as MouseEvent
                it.preventDefault()

                _actionFlow.tryEmit(
                    BrowserAction.DrawingUp(
                        points = listOf(it.clientX - drawing.offsetLeft to it.clientY - drawing.offsetTop),
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            }
        )

        drawing.addEventListener(
            EVENT_WHEEL,
            {
                it as WheelEvent
                it.preventDefault()

                _actionFlow.tryEmit(
                    BrowserAction.DrawingWheel(
                        x = it.clientX - drawing.offsetLeft,
                        y = it.clientY - drawing.offsetTop,
                        deltaY = it.deltaY,
                        width = drawing.clientWidth,
                        height = drawing.clientHeight,
                    )
                )
            },
        )

        drawing.addEventListener(EVENT_MOUSE_LEAVE, { _actionFlow.tryEmit(BrowserAction.DrawingLeave) })
    }

    private inline fun <reified T> ParentNode.find(selectors: String) = requireNotNull(querySelector(selectors) as? T) {
        "Unable to find element: \"$selectors\""
    }

    private inline fun <reified T> ParentNode.findAll(selectors: String): List<T> = querySelectorAll(selectors).let { nodeList ->
        object : AbstractList<T>() {
            override val size: Int get() = nodeList.length

            override fun get(index: Int): T = when (index) {
                in 0..lastIndex -> nodeList.item(index).unsafeCast<T>()
                else -> throw IndexOutOfBoundsException("index $index is not in range [0..$lastIndex]")
            }
        }
    }

    private inline fun <reified T : Node> T.appendChildren(vararg nodes: Node?) = apply {
        for (node in nodes) {
            if (node != null) {
                appendChild(node)
            }
        }
    }

    private inline fun <reified T : Node> T.appendTo(node: Node?) = apply { node?.appendChild(this) }

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    private inline fun Node.addClickListener(crossinline listener: (Event) -> Unit) = addEventListener(
        EVENT_CLICK,
        {
            it.preventDefault()
            it.stopPropagation()

            listener(it)
        },
    )

    private fun Element.replaceClassModifier(prefix: String, suffix: String) {
        val cssClasses = className.trim().split("\\s+".toRegex())
        val filteredCssClasses = cssClasses.filterNot { it.startsWith(prefix) } + listOf(prefix + suffix)

        if (cssClasses != filteredCssClasses) {
            className = filteredCssClasses.joinToString(" ")
        }
    }

    private fun <T> HTMLElement.setToolState(state: UiToolState<T>, block: (T) -> Unit = {}) {
        when (state) {
            is UiToolState.Hidden -> addClass(CLASS_HIDDEN)

            is UiToolState.Disabled -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_ACTIVE)
                addClass(CLASS_TOOL_DISABLED)

                state.title?.let { title = resourceManager.resolveText(it) }
                block(state.value)
            }

            is UiToolState.Visible -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_ACTIVE, CLASS_TOOL_DISABLED)

                state.title?.let { title = resourceManager.resolveText(it) }
                block(state.value)
            }

            is UiToolState.Active -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_DISABLED)
                addClass(CLASS_TOOL_ACTIVE)

                state.title?.let { title = resourceManager.resolveText(it) }
                block(state.value)
            }
        }
    }

    private fun Element.setVisible(isVisible: Boolean) = if (isVisible) {
        removeClass(CLASS_HIDDEN)
    } else {
        addClass(CLASS_HIDDEN)
    }

    private fun Element.setActive(isActive: Boolean) = if (isActive) {
        addClass(CLASS_TOOL_ACTIVE)
    } else {
        removeClass(CLASS_TOOL_ACTIVE)
    }

    private fun getColorClassSuffix(color: SciiColor) = when (color) {
        SciiColor.ForceTransparent -> SUFFIX_FORCE_TRANSPARENT
        SciiColor.Transparent -> SUFFIX_TRANSPARENT
        else -> color.value.toString()
    }

    private fun getLightClassSuffix(light: SciiLight) = when (light) {
        SciiLight.On -> "on"
        SciiLight.Off -> "off"
        SciiLight.ForceTransparent -> SUFFIX_FORCE_TRANSPARENT
        else -> SUFFIX_TRANSPARENT
    }

    private fun getCharClassSuffix(character: SciiChar) = when (character) {
        SciiChar.ForceTransparent -> SUFFIX_FORCE_TRANSPARENT
        SciiChar.Transparent -> SUFFIX_TRANSPARENT
        else -> character.value.toString()
    }

    private fun getShapeClassSuffix(shape: BpeShape) = when (shape) {
        BpeShape.Point -> "point"
        BpeShape.Line -> "line"
        BpeShape.StrokeBox -> "stroke_box"
        BpeShape.FillBox -> "fill_box"
        BpeShape.StrokeEllipse -> "stroke_ellipse"
        BpeShape.FillEllipse -> "fill_ellipse"
    }

    private fun getPaintingModeClassSuffix(mode: BpePaintingMode) = when (mode) {
        BpePaintingMode.Edge -> "edge"
        BpePaintingMode.Center -> "center"
    }

    private fun getPaintingModeTitle(mode: BpePaintingMode) = resourceManager.resolveText(
        when (mode) {
            BpePaintingMode.Edge -> TextRes.ToolboxModeEdge
            BpePaintingMode.Center -> TextRes.ToolboxModeCenter
        }
    )

    private fun getKeyModifiers(event: KeyboardEvent): Int {
        var modifiers = 0

        if (event.shiftKey) {
            modifiers += KeyModifier.Shift
        }

        if (event.ctrlKey || event.metaKey) {
            modifiers += KeyModifier.Ctrl
        }

        if (event.altKey) {
            modifiers += KeyModifier.Alt
        }

        return modifiers
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun makeSpan(innerHtml: String) = "<span>$innerHtml</span>"

    companion object {
        private const val CLASS_HIDDEN = "hidden"
        private const val CLASS_TOOL_DISABLED = "tool--disabled"
        private const val CLASS_TOOL_ACTIVE = "tool--active"
        private const val SUFFIX_OFF = "off"
        private const val SUFFIX_ON = "on"
        private const val SUFFIX_TRANSPARENT = "transparent"
        private const val SUFFIX_FORCE_TRANSPARENT = "force_transparent"

        private const val EVENT_CLICK = "click"
        private const val EVENT_CHANGE = "change"
        private const val EVENT_MOUSE_ENTER = "mouseenter"
        private const val EVENT_MOUSE_DOWN = "mousedown"
        private const val EVENT_MOUSE_MOVE = "mousemove"
        private const val EVENT_MOUSE_UP = "mouseup"
        private const val EVENT_WHEEL = "wheel"
        private const val EVENT_MOUSE_LEAVE = "mouseleave"
        private const val EVENT_TOUCH_START = "touchstart"
        private const val EVENT_TOUCH_MOVE = "touchmove"
        private const val EVENT_TOUCH_END = "touchend"
        private const val EVENT_TOUCH_CANCEL = "touchcancel"
        private const val EVENT_KEY_DOWN = "keydown"
        private const val EVENT_KEY_UP = "keyup"

        private const val NAME_DIV = "div"
        private const val NAME_IMG = "img"
        private const val NAME_CANVAS = "canvas"

        private const val SRC_LAYER_VISIBLE = "drawable/layer__visible.svg"
        private const val SRC_LAYER_INVISIBLE = "drawable/layer__invisible.svg"
        private const val SRC_LAYER_LOCKED = "drawable/layer__locked.svg"
        private const val SRC_LAYER_UNLOCKED = "drawable/layer__unlocked.svg"
        private const val SRC_LAYER_MASKED = "drawable/layer__masked.svg"
        private const val SRC_LAYER_UNMASKED = "drawable/layer__unmasked.svg"

        private const val SRC_TYPE_SCII = "drawable/type__scii.svg"
        private const val SRC_TYPE_HBLOCK = "drawable/type__hblock.svg"
        private const val SRC_TYPE_VBLOCK = "drawable/type__vblock.svg"
        private const val SRC_TYPE_QBLOCK = "drawable/type__qblock.svg"

        private const val SRC_LIGHT_FORMAT = "drawable/light__{}.svg"

        private const val PREVIEW_WIDTH = 256
        private const val PREVIEW_HEIGHT = 192
    }
}

private inline fun <R> TouchList.mapWithLimit(limit: Int = 2, transform: (Touch) -> R) = buildList {
    for (index in 0..<length) {
        item(index)?.let {
            add(transform(it))

            if (size >= limit) {
                return@buildList
            }
        }
    }
}

private class CachedLayerItem(val element: Element, val previewCanvas: HTMLCanvasElement)
