package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.engine.canvas.Canvas

interface Layer<T : Canvas<*>> {
    val uuid: String
    val isVisible: Boolean
    val isLocked: Boolean
    val canvas: T
}
