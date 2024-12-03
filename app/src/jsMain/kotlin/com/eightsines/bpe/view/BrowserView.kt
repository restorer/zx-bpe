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
import com.eightsines.bpe.presentation.UiState
import com.eightsines.bpe.presentation.UiToolState
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

class BrowserView(private val document: Document, private val renderer: BrowserRenderer) {
    var onAction: ((BrowserAction) -> Unit)? = null

    private val loading = document.find<HTMLElement>(".js-loading")
    private val container = document.find<HTMLElement>(".js-container")
    private val sheet = document.find<HTMLCanvasElement>(".js-sheet")
    private val areas = document.find<HTMLCanvasElement>(".js-areas")

    private val paletteColor = document.find<HTMLElement>(".js-palette-color")
    private val paletteColorIndicator = paletteColor?.find<HTMLElement>(".tool__color")
    private val paletteInk = document.find<HTMLElement>(".js-palette-ink")
    private val paletteInkIndicator = paletteInk?.find<HTMLElement>(".tool__color_ink")
    private val palettePaper = document.find<HTMLElement>(".js-palette-paper")
    private val palettePaperIndicator = palettePaper?.find<HTMLElement>(".tool__color_paper")
    private val paletteBright = document.find<HTMLElement>(".js-palette-bright")
    private val paletteBrightIndicator = paletteBright?.find<HTMLElement>(".tool__light")
    private val paletteFlash = document.find<HTMLElement>(".js-palette-flash")
    private val paletteFlashIndicator = paletteFlash?.find<HTMLElement>(".tool__light")
    private val paletteChar = document.find<HTMLElement>(".js-palette-char")
    private val paletteCharIndicator = paletteChar?.find<HTMLElement>(".tool__char")

    private val selectionCut = document.find<HTMLElement>(".js-selection-cut")
    private val selectionCopy = document.find<HTMLElement>(".js-selection-copy")
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

    private val colorsPanel = document.find<HTMLElement>(".js-colors-panel")
    private val lightsPanel = document.find<HTMLElement>(".js-lights-panel")
    private val charsPanel = document.find<HTMLElement>(".js-chars-panel")

    private val colorItems = mutableMapOf<SciiColor, Element>()
    private val lightItems = mutableMapOf<SciiLight, Element>()
    private val charItems = mutableMapOf<SciiChar, Element>()

    private val layersPanel = document.find<HTMLElement>(".js-layers-panel")
    private val layersItems = layersPanel?.find<HTMLElement>(".layers__items")
    private val layersTypes = layersPanel?.find<HTMLElement>(".layers__toolbar--types")
    private val layersCreate = document.find<HTMLElement>(".js-layers-create")
    private val layersMerge = document.find<HTMLElement>(".js-layers-merge")
    private val layersConvert = document.find<HTMLElement>(".js-layers-convert")
    private val layersDelete = document.find<HTMLElement>(".js-layers-delete")
    private val layersMoveUp = document.find<HTMLElement>(".js-layers-move-up")
    private val layersMoveDown = document.find<HTMLElement>(".js-layers-move-down")

    private val shapesPanel = document.find<HTMLElement>(".js-shapes-panel")
    private val shapesPoint = document.find<HTMLElement>(".js-shape-point")
    private val shapesLine = document.find<HTMLElement>(".js-shape-line")
    private val shapesStrokeBox = document.find<HTMLElement>(".js-shape-stroke-box")
    private val shapesFillBox = document.find<HTMLElement>(".js-shape-fill-box")

    private val menuPanel = document.find<HTMLElement>(".js-menu-panel")

    private var layersItemsCache = mutableMapOf<LayerView<*>, Element>()
    private var sheetViewCache: UiSheetView? = null
    private var selectionAreaCache: UiArea? = null
    private var cursorAreaCache: UiArea? = null

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
                    onAction?.invoke(BrowserAction.Ui(UiAction.SheetEnter(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_DOWN,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    onAction?.invoke(BrowserAction.Ui(UiAction.SheetDown(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_MOVE,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    onAction?.invoke(BrowserAction.Ui(UiAction.SheetMove(point.first, point.second)))
                }
            )

            areas.addEventListener(
                EVENT_MOUSE_UP,
                {
                    val point = translateMouseToCanvas(areas, it as MouseEvent)
                    onAction?.invoke(BrowserAction.Ui(UiAction.SheetUp(point.first, point.second)))
                }
            )

            areas.addEventListener(EVENT_MOUSE_LEAVE, { onAction?.invoke(BrowserAction.Ui(UiAction.SheetLeave)) })
        }

        paletteColor?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PaletteColorClick)) }
        paletteInk?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PaletteInkClick)) }
        palettePaper?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PalettePaperClick)) }
        paletteBright?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PaletteBrightClick)) }
        paletteFlash?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PaletteFlashClick)) }
        paletteChar?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.PaletteCharClick)) }

        selectionCut?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.SelectionCutClick)) }
        selectionCopy?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.SelectionCopyClick)) }
        layers?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayersClick)) }

        toolboxPaint?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxPaintClick)) }
        toolboxShape?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxShapeClick)) }
        toolboxErase?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxEraseClick)) }
        toolboxSelect?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxSelectClick)) }
        toolboxPickColor?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxPickColorClick)) }

        toolboxPaste?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxPasteClick)) }
        toolboxUndo?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxUndoClick)) }
        toolboxRedo?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ToolboxRedoClick)) }

        shapesPoint?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.Point))) }
        shapesLine?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.Line))) }
        shapesStrokeBox?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.StrokeBox))) }
        shapesFillBox?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ShapesItemClick(BpeShape.FillBox))) }

        menu?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.MenuClick)) }
        menuLoad?.also { menuLoad -> menuLoad.addEventListener(EVENT_CHANGE, { onAction?.invoke(BrowserAction.Load(menuLoad)) }) }
        menuSave?.addClickListener { onAction?.invoke(BrowserAction.Save) }

        layersCreate?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerCreateClick)) }
        layersMerge?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerMergeClick)) }
        layersConvert?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerConvertClick)) }
        layersDelete?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerDeleteClick)) }
        layersMoveUp?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerMoveUpClick)) }
        layersMoveDown?.addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerMoveDownClick)) }
    }

    fun render(state: UiState) {
        loading?.addClass(CLASS_HIDDEN)
        container?.removeClass(CLASS_HIDDEN)

        paletteColor?.setToolState(state.paletteColor) {
            paletteColorIndicator?.replaceClassModifier("tool__color--", getColorClassSuffix(it))
        }

        paletteInk?.setToolState(state.paletteInk) {
            paletteInkIndicator?.replaceClassModifier("tool__color_ink--", getColorClassSuffix(it))
        }

        palettePaper?.setToolState(state.palettePaper) {
            palettePaperIndicator?.replaceClassModifier("tool__color_paper--", getColorClassSuffix(it))
        }

        paletteBright?.setToolState(state.paletteBright) {
            paletteBrightIndicator?.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteFlash?.setToolState(state.paletteFlash) {
            paletteFlashIndicator?.replaceClassModifier("tool__light--", getLightClassSuffix(it))
        }

        paletteChar?.setToolState(state.paletteChar) {
            paletteCharIndicator?.replaceClassModifier("tool__char--", getCharClassSuffix(it))
        }

        selectionCut?.setToolState(state.selectionCut)
        selectionCopy?.setToolState(state.selectionCopy)
        layers?.setToolState(state.layers)

        toolboxPaint?.setToolState(state.toolboxPaint)

        toolboxShape?.setToolState(state.toolboxShape) {
            toolboxShape.replaceClassModifier("tool__shape--", getShapeClassSuffix(it))
        }

        toolboxErase?.setToolState(state.toolboxErase)
        toolboxSelect?.setToolState(state.toolboxSelect)
        toolboxPickColor?.setToolState(state.toolboxPickColor)
        toolboxPaste?.setToolState(state.toolboxPaste)
        toolboxUndo?.setToolState(state.toolboxUndo)
        toolboxRedo?.setToolState(state.toolboxRedo)
        menu?.setToolState(state.menu)

        colorsPanel?.setVisible(state.activePanel is UiPanel.Colors)
        lightsPanel?.setVisible(state.activePanel is UiPanel.Lights)
        charsPanel?.setVisible(state.activePanel is UiPanel.Chars)
        layersPanel?.setVisible(state.activePanel is UiPanel.Layers)
        shapesPanel?.setVisible(state.activePanel is UiPanel.Shapes)
        menuPanel?.setVisible(state.activePanel is UiPanel.Menu)

        when (val panel = state.activePanel) {
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
            }

            null, is UiPanel.Layers, is UiPanel.Menu -> Unit
        }

        renderLayersItems(state.layersItems, state.layersCurrentUid)

        layersCreate?.setToolState(state.layersCreate)
        layersMerge?.setToolState(state.layersMerge)
        layersConvert?.setToolState(state.layersConvert)
        layersDelete?.setToolState(state.layersDelete)
        layersMoveUp?.setToolState(state.layersMoveUp)
        layersMoveDown?.setToolState(state.layersMoveDown)
        layersTypes?.setVisible(state.layersTypesIsVisible)

        sheet?.let {
            val sheetView = state.sheet

            if (sheetViewCache != sheetView) {
                sheetViewCache = sheetView
                renderer.renderSheet(it, sheetView.backgroundView.layer, sheetView.canvasView.canvas)
            }
        }

        areas?.let {
            val selectionArea = state.selectionArea
            val cursorArea = state.cursorArea

            if (selectionAreaCache != selectionArea || cursorAreaCache != cursorArea) {
                cursorAreaCache = cursorArea
                selectionAreaCache = selectionArea

                renderer.renderAreas(it, selectionArea, cursorArea)
            }
        }
    }

    private fun renderLayersItems(layersViews: List<LayerView<*>>, layersCurrentUid: LayerUid) {
        val layersItems = this.layersItems ?: return

        for (element in layersItemsCache.values) {
            layersItems.removeChild(element)
        }

        val newLayersItemsCache = mutableMapOf<LayerView<*>, Element>()

        for (layerView in layersViews) {
            val layer = layerView.layer

            newLayersItemsCache[layerView] = layersItemsCache.getOrPut(layerView) {
                val startPane = document
                    .createElement(NAME_DIV) { className = "panel__pane" }
                    .appendChildren(
                        document
                            .createElement(NAME_DIV) {
                                className = "tool tool--sm"

                                addClickListener {
                                    onAction?.invoke(BrowserAction.Ui(UiAction.LayerItemVisibleClick(layer.uid, layer.isVisible)))
                                }
                            }
                            .appendChildren(
                                document.createElement(NAME_IMG) {
                                    this as HTMLImageElement

                                    className = "tool__icon"
                                    src = if (layer.isVisible) SRC_LAYER_VISIBLE else SRC_LAYER_INVISIBLE
                                    alt = if (layer.isVisible) ALT_LAYER_VISIBLE else ALT_LAYER_INVISIBLE
                                }
                            ),
                        document
                            .createElement(NAME_DIV) {
                                className = "tool tool--sm"

                                addClickListener {
                                    onAction?.invoke(BrowserAction.Ui(UiAction.LayerItemLockedClick(layer.uid, layer.isLocked)))
                                }
                            }
                            .appendChildren(
                                document.createElement(NAME_IMG) {
                                    this as HTMLImageElement

                                    className = "tool__icon"
                                    src = if (layer.isLocked) SRC_LAYER_LOCKED else SRC_LAYER_UNLOCKED
                                    alt = if (layer.isLocked) ALT_LAYER_LOCKED else ALT_LAYER_UNLOCKED
                                }
                            )
                    )

                val previewCanvas = document.createElement(NAME_CANVAS) {
                    this as HTMLCanvasElement

                    className = "layers__preview"
                    width = PREVIEW_WIDTH
                    height = PREVIEW_HEIGHT

                    renderer.renderPreview(this, layer)
                }

                val endPane = document
                    .createElement(NAME_DIV) { className = "panel__pane" }
                    .appendChildren(
                        document.createElement(NAME_DIV) {
                            className = "tool tool--sm tool--marker"

                            if (layer is CanvasLayer<*>) {
                                appendChild(createCanvasTypeIcon(layer.canvasType))
                            }
                        }
                    )

                document
                    .createElement(NAME_DIV) {
                        className = "layers__item"
                        addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerItemClick(layer.uid))) }
                    }
                    .appendChildren(startPane, previewCanvas, endPane)
            }
        }

        for (entry in newLayersItemsCache) {
            if (entry.key.layer.uid == layersCurrentUid) {
                entry.value.addClass("layers__item--active")
            } else {
                entry.value.removeClass("layers__item--active")
            }

            layersItems.appendChild(entry.value)
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
                    addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ColorsItemClick(sciiColor))) }

                }
                .appendChildren(document.createElement(NAME_DIV) { className = "tool__color tool__color--${color}" })
                .appendTo(paneElement)
                .also { colorItems[sciiColor] = it }
        }

        document
            .createElement(NAME_DIV) {
                className = "tool tool--md"
                addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.ColorsItemClick(SciiColor.Transparent))) }
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
                    className = "tool"
                    addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LightsItemClick(sciiLight))) }
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
                        addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.CharsItemClick(sciiChar))) }
                    }
                    .appendChildren(document.createElement(NAME_DIV) { className = "tool__char tool__char--${characterValue}" })
                    .appendTo(paneElement)
                    .also { charItems[sciiChar] = it }
            }
        }

        document
            .createElement(NAME_DIV) {
                className = "tool tool--xs"
                addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.CharsItemClick(SciiChar.Transparent))) }

            }
            .appendChildren(document.createElement(NAME_DIV) { className = "tool__char tool__char--transparent" })
            .appendTo(charsPanel)
            .also { charItems[SciiChar.Transparent] = it }
    }

    private fun createLayerTypeItems() {
        val paneElement = document
            .createElement(NAME_DIV) { className = "panel__pane" }
            .appendTo(layersTypes)

        for (type in listOf(CanvasType.Scii, CanvasType.HBlock, CanvasType.VBlock, CanvasType.QBlock)) {
            document
                .createElement(NAME_DIV) {
                    className = "tool tool--md"
                    addClickListener { onAction?.invoke(BrowserAction.Ui(UiAction.LayerTypeClick(type))) }
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

        alt = when (type) {
            CanvasType.Scii -> ALT_TYPE_SCII
            CanvasType.HBlock -> ALT_TYPE_HBLOCK
            CanvasType.VBlock -> ALT_TYPE_VBLOCK
            CanvasType.QBlock -> ALT_TYPE_QBLOCK
        }
    }

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

    private inline fun <reified T : Node> T.appendChildren(vararg nodes: Node) = apply {
        for (node in nodes) {
            appendChild(node)
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

    private fun <T> Element.setToolState(state: UiToolState<T>, block: (T) -> Unit = {}) {
        when (state) {
            is UiToolState.Hidden -> addClass(CLASS_HIDDEN)

            is UiToolState.Disabled -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_ACTIVE)
                addClass(CLASS_TOOL_DISABLED)
                block(state.value)
            }

            is UiToolState.Visible -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_ACTIVE, CLASS_TOOL_DISABLED)
                block(state.value)
            }

            is UiToolState.Active -> apply {
                removeClass(CLASS_HIDDEN, CLASS_TOOL_DISABLED)
                addClass(CLASS_TOOL_ACTIVE)
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

        private const val SRC_TYPE_SCII = "drawable/type__scii.svg"
        private const val SRC_TYPE_HBLOCK = "drawable/type__hblock.svg"
        private const val SRC_TYPE_VBLOCK = "drawable/type__vblock.svg"
        private const val SRC_TYPE_QBLOCK = "drawable/type__qblock.svg"

        private const val ALT_LAYER_VISIBLE = "Visible"
        private const val ALT_LAYER_INVISIBLE = "Invisible"
        private const val ALT_LAYER_LOCKED = "Locked"
        private const val ALT_LAYER_UNLOCKED = "Unlocked"

        private const val ALT_TYPE_SCII = "SpecSCII"
        private const val ALT_TYPE_HBLOCK = "HBlock"
        private const val ALT_TYPE_VBLOCK = "VBlock"
        private const val ALT_TYPE_QBLOCK = "QBlock"

        private const val PREVIEW_WIDTH = 256
        private const val PREVIEW_HEIGHT = 192
    }
}
