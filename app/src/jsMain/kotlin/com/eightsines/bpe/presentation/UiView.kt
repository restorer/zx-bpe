package com.eightsines.bpe.presentation

import com.eightsines.bpe.engine.BpeState
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode

class UiView(document: Document) {
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

    private val selectionCut = document.find<HTMLElement>(".js-selection-cut")
    private val selectionCopy = document.find<HTMLElement>(".js-selection-copy")
    private val layersButton = document.find<HTMLElement>(".js-layers")

    private val toolboxPaint = document.find<HTMLElement>(".js-toolbox-paint")
    private val toolboxErase = document.find<HTMLElement>(".js-toolbox-erase")
    private val toolboxShape = document.find<HTMLElement>(".js-toolbox-shape")
    private val toolboxSelect = document.find<HTMLElement>(".js-toolbox-select")
    private val toolboxPickColor = document.find<HTMLElement>(".js-toolbox-pick-color")
    private val toolboxPaste = document.find<HTMLElement>(".js-toolbox-paste")
    private val toolboxUndo = document.find<HTMLElement>(".js-toolbox-undo")
    private val toolboxRedo = document.find<HTMLElement>(".js-toolbox-redo")
    private val toolboxMenu = document.find<HTMLElement>(".js-toolbox-menu")

    fun render(state: BpeState) {
        container?.removeClass(HIDDEN)

        if (state.palettePaper == null) {
            paletteColor?.setVisibility(true)
            paletteColorIndicator?.className = "tool__color tool__color--${getColorClassSuffix(state.paletteInk)}"

            paletteInk?.setVisibility(false)
            palettePaper?.setVisibility(false)
        } else {
            paletteColor?.setVisibility(false)

            paletteInk?.setVisibility(true)
            paletteInkIndicator?.className = "tool__color_ink tool__color_ink--${getColorClassSuffix(state.paletteInk)}"

            palettePaper?.setVisibility(true)
            palettePaperIndicator?.className = "tool__color_paper tool__color_paper--${getColorClassSuffix(state.palettePaper)}"
        }

        paletteBright?.setVisibility(state.paletteBright != null)
        state.paletteBright?.let { paletteBrightIndicator?.className = "tool__light tool__light--${getLightClassSuffix(it)}" }

        paletteFlash?.setVisibility(state.paletteFlash != null)
        state.paletteFlash?.let { paletteFlashIndicator?.className = "tool__light tool__light--${getLightClassSuffix(it)}" }

        paletteChar?.setVisibility(state.paletteChar != null)
        state.paletteChar?.let { paletteCharIndicator?.className = "tool__character tool__character--${getCharClassSuffix(it)}" }

        selectionCut?.setVisibility(state.selectionCanCut)
        selectionCopy?.setVisibility(state.selectionCanCopy)

        // toolboxPaint
        // toolboxErase
        // toolboxShape

        toolboxSelect?.setToolEnabled(state.toolboxCanSelect)
        toolboxPaste?.setVisibility(state.toolboxCanPaste)
        toolboxUndo?.setToolEnabled(state.toolboxCanUndo)
        toolboxRedo?.setToolEnabled(state.toolboxCanRedo)
    }

    private inline fun <reified T> ParentNode.find(selectors: String) = this.querySelector(selectors) as? T

    private fun HTMLElement.setVisibility(isVisible: Boolean) = if (isVisible) {
        this.removeClass(HIDDEN)
    } else {
        this.addClass(HIDDEN)
    }

    private fun HTMLElement.setToolEnabled(isEnabled: Boolean) = if (isEnabled) {
        this.removeClass(TOOL_DISABLED)
    } else {
        this.addClass(TOOL_DISABLED)
    }

    private fun HTMLElement.setToolActive(isActive: Boolean) = if (isActive) {
        this.removeClass(TOOL_ACTIVE)
    } else {
        this.addClass(TOOL_ACTIVE)
    }

    private fun getColorClassSuffix(color: SciiColor) = if (color == SciiColor.Transparent) {
        TRANSPARENT
    } else {
        color.value.toString()
    }

    private fun getLightClassSuffix(light: SciiLight) = when (light) {
        SciiLight.On -> "on"
        SciiLight.Off -> "off"
        else -> TRANSPARENT
    }

    private fun getCharClassSuffix(character: SciiChar) = if (character == SciiChar.Transparent) {
        TRANSPARENT
    } else {
        character.value.toString()
    }

    private companion object {
        private const val HIDDEN = "hidden"
        private const val TRANSPARENT = "transparent"
        private const val TOOL_DISABLED = "tool--disabled"
        private const val TOOL_ACTIVE = "tool--active"
    }
}
