package com.eightsines.bpe.state

import com.eightsines.bpe.graphics.Canvas

class CanvasView<T : Canvas<*>>(val canvas: T) {
    private val mutations = canvas.mutations

    val isDirty: Boolean
        get() = mutations != canvas.mutations

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
