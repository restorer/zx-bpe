package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.engine.data.BlockDrawingCell
import com.eightsines.bpe.engine.data.SciiCell

sealed interface ImmutableCanvas<T> : Canvas<T> {
    class Scii(
        sciiWidth: Int,
        sciiHeight: Int,
        override val cells: List<List<SciiCell>>,
    ) : Canvas.Scii(sciiWidth, sciiHeight), ImmutableCanvas<SciiCell>

    class HBlock(
        sciiWidth: Int,
        sciiHeight: Int,
        override val cells: List<List<BlockDrawingCell>>,
    ) : Canvas.HBlock(sciiWidth, sciiHeight), ImmutableCanvas<BlockDrawingCell>

    class VBlock(
        sciiWidth: Int,
        sciiHeight: Int,
        override val cells: List<List<BlockDrawingCell>>,
    ) : Canvas.VBlock(sciiWidth, sciiHeight), ImmutableCanvas<BlockDrawingCell>

    class QBlock(
        sciiWidth: Int,
        sciiHeight: Int,
        override val pixels: List<List<Boolean>>,
        override val attrs: List<List<BlockDrawingCell>>,
    ) : Canvas.QBlock(sciiWidth, sciiHeight), ImmutableCanvas<BlockDrawingCell>
}
