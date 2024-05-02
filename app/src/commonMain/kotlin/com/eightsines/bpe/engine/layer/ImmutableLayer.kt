package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.engine.canvas.Canvas

class ImmutableLayer<T : Canvas<*>>(
    override val uuid: String,
    override val isVisible: Boolean,
    override val isLocked: Boolean,
    override val canvas: T,
) : Layer<T>
