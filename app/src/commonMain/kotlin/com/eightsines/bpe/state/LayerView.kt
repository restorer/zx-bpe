package com.eightsines.bpe.state

import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.Layer

sealed interface LayerView<T : Layer> {
    val layer: T
    val isDirty: Boolean
}

class BackgroundLayerView(override val layer: BackgroundLayer) : LayerView<BackgroundLayer> {
    private val isLocked = layer.isLocked
    private val isVisible = layer.isVisible
    private val border = layer.border
    private val color = layer.color
    private val bright = layer.bright

    override val isDirty: Boolean
        get() = isLocked != layer.isLocked ||
                isVisible != layer.isVisible ||
                border != layer.border ||
                color != layer.color ||
                bright != layer.bright

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as BackgroundLayerView

        return isLocked == other.isLocked ||
                isVisible == other.isVisible ||
                border == other.border ||
                color == other.color ||
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
}

class CanvasLayerView(override val layer: CanvasLayer<*>) : LayerView<CanvasLayer<*>> {
    private val isLocked = layer.isLocked
    private val isVisible = layer.isVisible
    private val canvasMutations = layer.canvas.mutations

    override val isDirty: Boolean
        get() = isLocked != layer.isLocked ||
                isVisible != layer.isVisible ||
                canvasMutations != layer.canvas.mutations

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as CanvasLayerView

        return isLocked != other.isLocked ||
                isVisible != other.isVisible ||
                canvasMutations != other.canvasMutations
    }

    override fun hashCode(): Int {
        var result = isLocked.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + canvasMutations
        return result
    }
}
