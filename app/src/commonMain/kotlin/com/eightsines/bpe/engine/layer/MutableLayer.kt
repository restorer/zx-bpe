package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.engine.canvas.MutableCanvas

class MutableLayer<T : MutableCanvas<*>>(
    override val uuid: String,
    override var isVisible: Boolean,
    override var isLocked: Boolean,
    override val canvas: T,
) : Layer<T> {
    private var immutableCache: ImmutableLayer<T>? = null

    fun toImmutable() = if (immutableCache?.canvas?.isImmutableCached == true) {
        immutableCache
    } else {
        @Suppress("UNCHECKED_CAST")
        ImmutableLayer(
            uuid = uuid,
            isVisible = isVisible,
            isLocked = isLocked,
            canvas = canvas.toImmutable() as T,
        ).also { immutableCache = it }
    }
}
