package com.eightsines.bpe.view

import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.middlware.BpeShape
import com.eightsines.bpe.middlware.LayerView
import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiArea
import com.eightsines.bpe.presentation.UiPanel
import com.eightsines.bpe.presentation.UiSheetView
import com.eightsines.bpe.presentation.UiToolState
import com.eightsines.bpe.resources.ResourceManager
import com.eightsines.bpe.resources.TextRes
import com.eightsines.bpe.resources.TextResId
import com.eightsines.bpe.util.ElapsedTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.dom.addClass
import kotlinx.dom.createElement
import kotlinx.dom.removeClass
import org.w3c.dom.DOMRect
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

class BrowserView(
    private val document: Document,
    private val elapsedTimeProvider: ElapsedTimeProvider,
    private val renderer: BrowserRenderer,
    private val resourceManager: ResourceManager,
) {
    private val _actionFlow = MutableSharedFlow<BrowserAction>(extraBufferCapacity = 1)

    val actionFlow: Flow<BrowserAction>
        get() = _actionFlow

    private val loading = document.find<HTMLElement>(".js-loading")
    private val container = document.find<HTMLElement>(".js-container")
    private val sheet = document.find<HTMLCanvasElement>(".js-sheet")
    private val areas = document.find<HTMLCanvasElement>(".js-areas")

    private val paletteColor = document.find<HTMLElement>(".js-palette-color")
    private val paletteColorIndicator = document.find<HTMLElement>(".js-palette-color-indicator")
    private val paletteInk = document.find<HTMLElement>(".js-palette-ink")
    private val paletteInkIndicator = document.find<HTMLElement>(".js-palette-ink-indicator")
    private val palettePaper = document.find<HTMLElement>(".js-palette-paper")
    private val palettePaperIndicator = document.find<HTMLElement>(".js-palette-paper-indicator")
    private val paletteBright = document.find<HTMLElement>(".js-palette-bright")
    private val paletteBrightIndicator = document.find<HTMLElement>(".js-palette-bright-indicator")
    private val paletteFlash = document.find<HTMLElement>(".js-palette-flash")
    private val paletteFlashIndicator = document.find<HTMLElement>(".js-palette-flash-indicator")
    private val paletteChar = document.find<HTMLElement>(".js-palette-char")
    private val paletteCharIndicator = document.find<HTMLElement>(".js-palette-char-indicator")

    private val selectionMenu = document.find<HTMLElement>(".js-selection-menu")
    private val selectionCut = document.find<HTMLElement>(".js-selection-cut")
    private val selectionCopy = document.find<HTMLElement>(".js-selection-copy")
    private val selectionFlipHorizontal = document.find<HTMLElement>(".js-selection-flip-horizontal")
    private val selectionFlipVertical = document.find<HTMLElement>(".js-selection-flip-vertical")
    private val selectionRotateCw = document.find<HTMLElement>(".js-selection-rotate-cw")
    private val selectionRotateCcw = document.find<HTMLElement>(".js-selection-rotate-ccw")
    private val layers = document.find<HTMLElement>(".js-layers")

    private val toolboxPaint = document.find<HTMLElement>(".js-toolbox-paint")
    private val toolboxShape = document.find<HTMLElement>(".js-toolbox-shape")
    private val toolboxErase = document.find<HTMLElement>(".js-toolbox-erase")
    private val toolboxSelect = document.find<HTMLElement>(".js-toolbox-select")
    private val toolboxPickColor = document.find<HTMLElement>(".js-toolbox-pick-color")

    private val toolboxPaste = document.find<HTMLElement>(".js-toolbox-paste")
    private val toolboxUndo = document.find<HTMLElement>(".js-toolbox-undo")
    private val toolboxRedo = document.find<HTMLElement>(".js-toolbox-redo")
    private val menu = document.find<HTMLElement>(".js-menu")
    private val menuLoad = document.find<HTMLInputElement>(".js-menu-load")
    private val menuSave = document.find<HTMLElement>(".js-menu-save")
    private val menuExport = document.find<HTMLElement>(".js-menu-export")

    private val colorsPanel = document.find<HTMLElement>(".js-colors-panel")
    private val lightsPanel = document.find<HTMLElement>(".js-lights-panel")
    private val charsPanel = document.find<HTMLElement>(".js-chars-panel")
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
    private val shapesStrokeOval = document.find<HTMLElement>(".js-shape-stroke-oval")
    private val shapesFillOval = document.find<HTMLElement>(".js-shape-fill-oval")

    private val menuPanel = document.find<HTMLElement>(".js-menu-panel")

    private val alert = document.find<HTMLElement>(".js-alert")
    private val alertContent = document.find<HTMLElement>(".js-alert-content")

    private var layersItemsCache = mutableMapOf<LayerView<*>, CachedLayerItem>()
    private var sheetViewCache: UiSheetView? = null
    private var selectionAreaCache: UiArea? = null
    private var cursorAreaCache: UiArea? = null

    private var wasRendered = false
    private var lastRefreshTimeMs = 0L

    init {
        createColorItems()
        createLightItems()
        createCharItems()
        createLayerTypeItems()

        areas?.let { areas ->
            areas.addEventListener(
                EVENT_MOUSE_ENTER,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SheetEnter(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_DOWN,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SheetDown(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_MOVE,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SheetMove(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_UP,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SheetUp(point.first, point.second)))
                }
            )

            areas.addEventListener(EVENT_MOUSE_LEAVE, { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SheetLeave)) })
        }

        paletteColor?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteColorClick)) }
        paletteInk?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteInkClick)) }
        palettePaper?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PalettePaperClick)) }
        paletteBright?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteBrightClick)) }
        paletteFlash?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteFlashClick)) }
        paletteChar?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.PaletteCharClick)) }

        selectionMenu?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionMenuClick)) }
        selectionCut?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionCutClick)) }
        selectionCopy?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionCopyClick)) }
        selectionFlipHorizontal?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionFlipHorizontalClick)) }
        selectionFlipVertical?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionFlipVerticalClick)) }
        selectionRotateCw?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionRotateCwClick)) }
        selectionRotateCcw?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.SelectionRotateCcwClick)) }
        layers?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayersClick)) }

        toolboxPaint?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPaintClick)) }
        toolboxShape?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxShapeClick)) }
        toolboxErase?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxEraseClick)) }
        toolboxSelect?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxSelectClick)) }
        toolboxPickColor?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPickColorClick)) }

        toolboxPaste?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxPasteClick)) }
        toolboxUndo?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxUndoClick)) }
        toolboxRedo?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ToolboxRedoClick)) }

        shapesPoint?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.Point))) }
        shapesLine?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.Line))) }
        shapesStrokeBox?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.StrokeBox))) }
        shapesFillBox?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.FillBox))) }
        shapesStrokeOval?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.StrokeOval))) }
        shapesFillOval?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.FillOval))) }

        menu?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.MenuClick)) }
        menuLoad?.also { menuLoad -> menuLoad.addEventListener(EVENT_CHANGE, { _actionFlow.tryEmit(BrowserAction.Load(menuLoad)) }) }
        menuSave?.addClickListener { _actionFlow.tryEmit(BrowserAction.Save) }
        menuExport?.addClickListener { _actionFlow.tryEmit(BrowserAction.Export) }

        layersCreate?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerCreateClick)) }
        layersCreateCancel?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerCreateClick)) }
        layersMerge?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMergeClick)) }
        layersConvert?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerConvertClick)) }
        layersConvertCancel?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerConvertClick)) }
        layersDelete?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerDeleteClick)) }
        layersMoveUp?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMoveUpClick)) }
        layersMoveDown?.addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LayerMoveDownClick)) }

        alert?.addClickListener { _actionFlow.tryEmit(BrowserAction.HideAlert) }

        for (element in document.findAll<HTMLElement>("[bpe-title]")) {
            element.title = element.getAttribute("bpe-title")?.let { resourceManager.resolveText(TextResId(it)) } ?: ""
        }

        for (element in document.findAll<HTMLElement>("[bpe-text]")) {
            element.textContent = element.getAttribute("bpe-text")?.let { resourceManager.resolveText(TextResId(it)) } ?: ""
        }
    }

    fun render(state: BrowserState) {
        val uiState = state.uiState

        loading?.addClass(CLASS_HIDDEN)
        container?.removeClass(CLASS_HIDDEN)

        paletteColor?.setToolState(uiState.paletteColor) {
            paletteColorIndicator?.replaceClassModifier("tool__color--", getColorClassSuffix(it))
        }

        paletteInk?.setToolState(uiState.paletteInk) {
            paletteInkIndicator?.replaceClassModifier("tool__color_ink--", getColorClassSuffix(it))
        }

        palettePaper?.setToolState(uiState.palettePaper) {
            palettePaperIndicator?.replaceClassModifier("tool__color_paper--", getColorClassSuffix(it))
        }

        paletteBright?.setToolState(uiState.paletteBright) {
            paletteBrightIndicator?.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteFlash?.setToolState(uiState.paletteFlash) {
            paletteFlashIndicator?.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteChar?.setToolState(uiState.paletteChar) {
            paletteCharIndicator?.replaceClassModifier("tool__char--", getCharClassSuffix(it))
        }

        selectionMenu?.setToolState(uiState.selectionMenu)
        layers?.setToolState(uiState.layers)

        toolboxPaint?.setToolState(uiState.toolboxPaint)

        toolboxShape?.setToolState(uiState.toolboxShape) {
            toolboxShape.replaceClassModifier("tool__shape--", getShapeClassSuffix(it))
        }

        toolboxErase?.setToolState(uiState.toolboxErase)
        toolboxSelect?.setToolState(uiState.toolboxSelect)
        toolboxPickColor?.setToolState(uiState.toolboxPickColor)
        toolboxPaste?.setToolState(uiState.toolboxPaste)
        toolboxUndo?.setToolState(uiState.toolboxUndo)
        toolboxRedo?.setToolState(uiState.toolboxRedo)
        menu?.setToolState(uiState.menu)

        colorsPanel?.setVisible(uiState.activePanel is UiPanel.Colors)
        lightsPanel?.setVisible(uiState.activePanel is UiPanel.Lights)
        charsPanel?.setVisible(uiState.activePanel is UiPanel.Chars)
        selectionPanel?.setVisible(uiState.activePanel is UiPanel.SelectionMenu)
        layersPanel?.setVisible(uiState.activePanel is UiPanel.Layers)
        shapesPanel?.setVisible(uiState.activePanel is UiPanel.Shapes)
        menuPanel?.setVisible(uiState.activePanel is UiPanel.Menu)

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

            is UiPanel.Shapes -> {
                shapesPoint?.setActive(panel.shape == BpeShape.Point)
                shapesLine?.setActive(panel.shape == BpeShape.Line)
                shapesStrokeBox?.setActive(panel.shape == BpeShape.StrokeBox)
                shapesFillBox?.setActive(panel.shape == BpeShape.FillBox)
                shapesStrokeOval?.setActive(panel.shape == BpeShape.StrokeOval)
                shapesFillOval?.setActive(panel.shape == BpeShape.FillOval)
            }

            null, is UiPanel.SelectionMenu, is UiPanel.Layers, is UiPanel.Menu -> Unit
        }

        renderLayersItems(uiState.layersItems, uiState.layersCurrentUid)

        layersCreate?.setToolState(uiState.layersCreate)
        layersCreateCancel?.setToolState(uiState.layersCreateCancel)
        layersMerge?.setToolState(uiState.layersMerge)
        layersConvert?.setToolState(uiState.layersConvert)
        layersConvertCancel?.setToolState(uiState.layersConvertCancel)
        layersDelete?.setToolState(uiState.layersDelete)
        layersMoveUp?.setToolState(uiState.layersMoveUp)
        layersMoveDown?.setToolState(uiState.layersMoveDown)
        layersToolbarPrimary?.setVisible(!uiState.layersTypesIsVisible)
        layersToolbarTypes?.setVisible(uiState.layersTypesIsVisible)

        sheet?.let {
            val sheetView = uiState.sheet

            if (sheetViewCache != sheetView) {
                sheetViewCache = sheetView

                renderer.renderSheet(it, sheetView.backgroundView.layer, sheetView.canvasView.canvas)
            }
        }

        areas?.let {
            val selectionArea = uiState.selectionArea
            val cursorArea = uiState.cursorArea

            if (selectionAreaCache != selectionArea || cursorAreaCache != cursorArea) {
                selectionAreaCache = selectionArea
                cursorAreaCache = cursorArea

                renderer.renderAreas(it, selectionArea, cursorArea)
            }
        }

        alert?.setVisible(state.alertText != null)
        alertContent?.textContent = state.alertText?.let(resourceManager::resolveText) ?: ""

        wasRendered = true
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

        sheet?.let { sheet ->
            sheetViewCache?.let { renderer.renderSheet(sheet, it.backgroundView.layer, it.canvasView.canvas) }
        }

        areas?.let { renderer.renderAreas(it, selectionAreaCache, cursorAreaCache) }
        lastRefreshTimeMs = elapsedTimeMs
    }

    private fun renderLayersItems(layersViews: List<LayerView<*>>, layersCurrentUid: LayerUid) {
        val layersItems = this.layersItems ?: return

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
                                        if (layer.isPixelsLocked) TextRes.LayerPixelsLocked else TextRes.LayerPixelsUnlocked
                                    )

                                    addClickListener {
                                        _actionFlow.tryEmit(
                                            BrowserAction.Ui(UiAction.LayerItemPixelsLockedClick(layer.uid, layer.isPixelsLocked)),
                                        )
                                    }
                                }
                            }
                            .appendChildren(
                                (layer as? CanvasLayer<*>?)?.let { canvasLayer ->
                                    document.createElement(NAME_IMG) {
                                        this as HTMLImageElement

                                        className = "tool__icon"
                                        src = if (canvasLayer.isPixelsLocked) SRC_LAYER_PIXELS_LOCKED else SRC_LAYER_PIXELS_UNLOCKED
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
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ColorsItemClick(sciiColor))) }

                }
                .appendChildren(document.createElement(NAME_DIV) { className = "tool__color tool__color--${color}" })
                .appendTo(paneElement)
                .also { colorItems[sciiColor] = it }
        }

        document
            .createElement(NAME_DIV) {
                className = "tool tool--md"
                addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.ColorsItemClick(SciiColor.Transparent))) }
            }
            .appendChildren(document.createElement(NAME_DIV) { className = "tool__color tool__color--transparent" })
            .appendTo(paneElement)
            .also { colorItems[SciiColor.Transparent] = it }
    }

    private fun createLightItems() {
        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(lightsPanel)

        for ((sciiLight, suffix) in listOf(SciiLight.Off to "off", SciiLight.On to "on", SciiLight.Transparent to SUFFIX_TRANSPARENT)) {
            document
                .createElement(NAME_DIV) {
                    className = "tool tool--md"
                    addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.LightsItemClick(sciiLight))) }
                }
                .appendChildren(document.createElement(NAME_DIV) { className = "tool__light tool__light--${suffix}" })
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
                        addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.CharsItemClick(sciiChar))) }
                    }
                    .appendChildren(document.createElement(NAME_DIV) { className = "tool__char tool__char--${characterValue}" })
                    .appendTo(paneElement)
                    .also { charItems[sciiChar] = it }
            }
        }

        document
            .createElement(NAME_DIV) {
                className = "tool tool--xs"
                addClickListener { _actionFlow.tryEmit(BrowserAction.Ui(UiAction.CharsItemClick(SciiChar.Transparent))) }
            }
            .appendChildren(document.createElement(NAME_DIV) { className = "tool__char tool__char--transparent" })
            .appendTo(charsPanel)
            .also { charItems[SciiChar.Transparent] = it }
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

    private fun translateMouseToCanvas(canvas: HTMLCanvasElement, event: MouseEvent): Pair<Int, Int> {
        val bbox: DOMRect = canvas.getBoundingClientRect()

        if (bbox.width < 1.0 || bbox.height < 1.0) {
            return 0 to 0
        }

        val scale: Double
        val offsetX: Double
        val offsetY: Double

        if (canvas.height / bbox.height > canvas.width / bbox.width) {
            scale = bbox.height / canvas.height
            offsetX = (bbox.width - canvas.width * scale) * 0.5
            offsetY = 0.0
        } else {
            scale = bbox.width / canvas.width
            offsetX = 0.0
            offsetY = (bbox.height - canvas.height * scale) * 0.5
        }

        val x = (event.clientX - bbox.left - offsetX) / scale
        val y = (event.clientY - bbox.top - offsetY) / scale

        return x.toInt() to y.toInt()
    }

    private inline fun <reified T> ParentNode.find(selectors: String) = querySelector(selectors) as? T

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
    private inline fun Node.addClickListener(noinline listener: (Event) -> Unit) = addEventListener(EVENT_CLICK, listener)

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

                if (state.title != null) {
                    title = resourceManager.resolveText(state.title)
                }

                block(state.value)
            }

            is UiToolState.Visible -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_ACTIVE, CLASS_TOOL_DISABLED)

                if (state.title != null) {
                    title = resourceManager.resolveText(state.title)
                }

                block(state.value)
            }

            is UiToolState.Active -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_DISABLED)
                addClass(CLASS_TOOL_ACTIVE)

                if (state.title != null) {
                    title = resourceManager.resolveText(state.title)
                }

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

    private fun getColorClassSuffix(color: SciiColor) = if (color == SciiColor.Transparent) {
        SUFFIX_TRANSPARENT
    } else {
        color.value.toString()
    }

    private fun getLightClassSuffix(light: SciiLight) = when (light) {
        SciiLight.On -> "on"
        SciiLight.Off -> "off"
        else -> SUFFIX_TRANSPARENT
    }

    private fun getCharClassSuffix(character: SciiChar) = if (character == SciiChar.Transparent) {
        SUFFIX_TRANSPARENT
    } else {
        character.value.toString()
    }

    private fun getShapeClassSuffix(shape: BpeShape) = when (shape) {
        BpeShape.Point -> "point"
        BpeShape.Line -> "line"
        BpeShape.StrokeBox -> "stroke_box"
        BpeShape.FillBox -> "fill_box"
        BpeShape.StrokeOval -> "stroke_oval"
        BpeShape.FillOval -> "fill_oval"
    }

    private companion object {
        private const val CLASS_HIDDEN = "hidden"
        private const val CLASS_TOOL_DISABLED = "tool--disabled"
        private const val CLASS_TOOL_ACTIVE = "tool--active"
        private const val SUFFIX_TRANSPARENT = "transparent"

        private const val EVENT_CLICK = "click"
        private const val EVENT_CHANGE = "change"
        private const val EVENT_MOUSE_ENTER = "mouseenter"
        private const val EVENT_MOUSE_DOWN = "mousedown"
        private const val EVENT_MOUSE_MOVE = "mousemove"
        private const val EVENT_MOUSE_UP = "mouseup"
        private const val EVENT_MOUSE_LEAVE = "mouseleave"

        private const val NAME_DIV = "div"
        private const val NAME_IMG = "img"
        private const val NAME_CANVAS = "canvas"

        private const val SRC_LAYER_VISIBLE = "drawable/layer__visible.svg"
        private const val SRC_LAYER_INVISIBLE = "drawable/layer__invisible.svg"
        private const val SRC_LAYER_LOCKED = "drawable/layer__locked.svg"
        private const val SRC_LAYER_UNLOCKED = "drawable/layer__unlocked.svg"
        private const val SRC_LAYER_PIXELS_LOCKED = "drawable/layer__pixels_locked.svg"
        private const val SRC_LAYER_PIXELS_UNLOCKED = "drawable/layer__pixels_unlocked.svg"

        private const val SRC_TYPE_SCII = "drawable/type__scii.svg"
        private const val SRC_TYPE_HBLOCK = "drawable/type__hblock.svg"
        private const val SRC_TYPE_VBLOCK = "drawable/type__vblock.svg"
        private const val SRC_TYPE_QBLOCK = "drawable/type__qblock.svg"

        private const val PREVIEW_WIDTH = 256
        private const val PREVIEW_HEIGHT = 192
    }
}

private class CachedLayerItem(val element: Element, val previewCanvas: HTMLCanvasElement)
