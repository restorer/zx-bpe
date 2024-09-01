package com.eightsines.bpe.state

import com.eightsines.bpe.graphics.SciiCanvas
import com.eightsines.bpe.layer.BackgroundLayer

class SheetView(val backgroundView: LayerView<BackgroundLayer>, val canvasView: CanvasView<SciiCanvas>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as SheetView
        return backgroundView == other.backgroundView && canvasView == other.canvasView
    }

    override fun hashCode(): Int {
        var result = backgroundView.hashCode()
        result = 31 * result + canvasView.hashCode()
        return result
    }
}
