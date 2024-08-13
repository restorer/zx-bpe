package com.eightsines.bpe.presentation

import com.eightsines.bpe.engine.BpeShape
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode

class UiView(document: Document) {
    var onAction: ((UiAction) -> Unit)? = null

    private val container = document.find<HTMLElement>(".js-container")
    val canvas = document.find<HTMLCanvasElement>(".js-canvas")

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
    private val paletteChar = document.find<HTMLElement>(".js-palette-character")
    private val paletteCharIndicator = paletteChar?.find<HTMLElement>(".tool__character")
    private val paletteSelectionCut = document.find<HTMLElement>(".js-palette-selection-cut")
    private val paletteSelectionCopy = document.find<HTMLElement>(".js-palette-selection-copy")
    private val paletteLayers = document.find<HTMLElement>(".js-palette-layers")

    private val toolboxPaint = document.find<HTMLElement>(".js-toolbox-paint")
    private val toolboxShape = document.find<HTMLElement>(".js-toolbox-shape")
    private val toolboxErase = document.find<HTMLElement>(".js-toolbox-erase")
    private val toolboxSelect = document.find<HTMLElement>(".js-toolbox-select")
    private val toolboxPickColor = document.find<HTMLElement>(".js-toolbox-pick-color")
    private val toolboxPaste = document.find<HTMLElement>(".js-toolbox-paste")
    private val toolboxUndo = document.find<HTMLElement>(".js-toolbox-undo")
    private val toolboxRedo = document.find<HTMLElement>(".js-toolbox-redo")
    private val toolboxMenu = document.find<HTMLElement>(".js-toolbox-menu")

    private val colorsPanel = document.find<HTMLElement>(".js-colors")
    private val lightsPanel = document.find<HTMLElement>(".js-lights")
    private val charactersPanel = document.find<HTMLElement>(".js-characters")
    private val layersPanel = document.find<HTMLElement>(".js-layers")
    private val shapesPanel = document.find<HTMLElement>(".js-shapes")
    private val menuPanel = document.find<HTMLElement>(".js-menu")

    init {
        toolboxMenu?.addEventListener("click", { onAction?.invoke(UiAction.ToolboxMenuClick) })
    }

    fun render(state: UiState) {
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
            paletteCharIndicator?.replaceClassModifier("tool__character--", getCharClassSuffix(it))
        }

        paletteSelectionCut?.setToolState(state.paletteSelectionCut)
        paletteSelectionCopy?.setToolState(state.paletteSelectionCopy)
        paletteLayers?.setToolState(state.paletteLayers)

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
        toolboxMenu?.setToolState(state.toolboxMenu)

        colorsPanel?.setVisible(state.panel == UiPanel.Colors)
        lightsPanel?.setVisible(state.panel == UiPanel.Lights)
        charactersPanel?.setVisible(state.panel == UiPanel.Characters)
        layersPanel?.setVisible(state.panel == UiPanel.Layers)
        shapesPanel?.setVisible(state.panel == UiPanel.Shapes)
        menuPanel?.setVisible(state.panel == UiPanel.Menu)
    }

    private inline fun <reified T> ParentNode.find(selectors: String) = querySelector(selectors) as? T

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

    private fun HTMLElement.setVisible(isVisible: Boolean) = if (isVisible) {
        removeClass(CLASS_HIDDEN)
    } else {
        addClass(CLASS_HIDDEN)
    }

    private fun getColorClassSuffix(color: SciiColor) = if (color == SciiColor.Transparent) {
        CLASS_TRANSPARENT
    } else {
        color.value.toString()
    }

    private fun getLightClassSuffix(light: SciiLight) = when (light) {
        SciiLight.On -> "on"
        SciiLight.Off -> "off"
        else -> CLASS_TRANSPARENT
    }

    private fun getCharClassSuffix(character: SciiChar) = if (character == SciiChar.Transparent) {
        CLASS_TRANSPARENT
    } else {
        character.value.toString()
    }

    private fun getShapeClassSuffix(shape: BpeShape) = when (shape) {
        BpeShape.Point -> "point"
        BpeShape.Line -> "point"
        BpeShape.StrokeBox -> "stroke_box"
        BpeShape.FillBox -> "fill_box"
    }

    private companion object {
        private const val CLASS_HIDDEN = "hidden"
        private const val CLASS_TRANSPARENT = "transparent"
        private const val CLASS_TOOL_DISABLED = "tool--disabled"
        private const val CLASS_TOOL_ACTIVE = "tool--active"
    }
}
