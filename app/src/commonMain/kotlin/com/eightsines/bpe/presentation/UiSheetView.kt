package com.eightsines.bpe.presentation

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.middlware.CanvasView
import com.eightsines.bpe.middlware.LayerView

class UiSheetView(val backgroundView: LayerView<BackgroundLayer>, val canvasView: CanvasView<SciiCanvas>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as UiSheetView
        return backgroundView == other.backgroundView && canvasView == other.canvasView
    }

    override fun hashCode(): Int {
        var result = backgroundView.hashCode()
        result = 31 * result + canvasView.hashCode()
        return result
    }

    override fun toString() = "UiSheetView(backgroundView=$backgroundView, canvasView=$canvasView)"
}