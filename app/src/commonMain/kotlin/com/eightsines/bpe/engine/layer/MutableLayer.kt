package com.eightsines.bpe.engine.layer

import com.eightsines.bpe.engine.canvas.MutableCanvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface MutableLayer

data class MutableBackgroundLayer(
    override val uid: String,
    override var isVisible: Boolean,
    override val isLocked: Boolean,
    override var color: SciiColor,
    override var bright: SciiLight,
) : BackgroundLayer, MutableLayer

data class MutableCanvasLayer<T : Cell>(
    override val uid: String,
    override var isVisible: Boolean,
    override var isLocked: Boolean,
    override val canvas: MutableCanvas<T>,
) : CanvasLayer<T>, MutableLayer
