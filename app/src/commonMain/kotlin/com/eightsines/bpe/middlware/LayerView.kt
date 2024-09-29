package com.eightsines.bpe.middlware

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.Layer

sealed interface LayerView<T : Layer> {
    val layer: T
}

class BackgroundLayerView(override val layer: BackgroundLayer) : LayerView<BackgroundLayer> {
    private val isLocked = layer.isLocked
    private val isVisible = layer.isVisible
    private val border = layer.border
    private val color = layer.color
    private val bright = layer.bright

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as BackgroundLayerView

        return isLocked == other.isLocked &&
                isVisible == other.isVisible &&
                border == other.border &&
                color == other.color &&
                bright == other.bright
    }

    override fun hashCode(): Int {
        var result = isLocked.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + border.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + bright.hashCode()
        return result
    }

    override fun toString() = "BackgroundLayerView(isLocked=$isLocked, isVisible=$isVisible, border=$border, color=$color, bright=$bright)"
}

class CanvasLayerView(override val layer: CanvasLayer<*>) : LayerView<CanvasLayer<*>> {
    private val uid = layer.uid
    private val isLocked = layer.isLocked
    private val isVisible = layer.isVisible
    private val canvasMutations = layer.canvas.mutations

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as CanvasLayerView

        return uid == other.uid &&
                isLocked == other.isLocked &&
                isVisible == other.isVisible &&
                canvasMutations == other.canvasMutations
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + canvasMutations
        return result
    }

    override fun toString() = "CanvasLayerView(uid=$uid, isLocked=$isLocked, isVisible=$isVisible, canvasMutations=$canvasMutations)"
}
