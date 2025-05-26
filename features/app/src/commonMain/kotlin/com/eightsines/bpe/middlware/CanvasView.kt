package com.eightsines.bpe.middlware

import com.eightsines.bpe.foundation.Canvas

class CanvasView<T : Canvas<*>>(val canvas: T) {
    private val mutations = canvas.mutations

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as CanvasView<*>
        return mutations == other.mutations
    }

    override fun hashCode(): Int {
        return mutations
    }
}
