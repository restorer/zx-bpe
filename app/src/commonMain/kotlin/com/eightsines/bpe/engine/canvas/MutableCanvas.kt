package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.engine.data.*

sealed interface MutableCanvas<T> : Canvas<T> {
    val isImmutableCached: Boolean

    fun putDrawingCell(drawingX: Int, drawingY: Int, cell: T)
    fun undoSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell)
    fun toImmutable(): Canvas<T>

    class Scii(sciiWidth: Int, sciiHeight: Int) : Canvas.Scii(sciiWidth, sciiHeight), MutableCanvas<SciiCell> {
        override val cells: MutableList<MutableList<SciiCell>> =
            MutableList(drawingHeight) { MutableList(drawingWidth) { SciiCell.Transparent } }

        private var immutableCache: ImmutableCanvas.Scii? = null

        override val isImmutableCached: Boolean
            get() = immutableCache != null

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: SciiCell) {
            cells[drawingY][drawingX] = if (cell.character == SciiChar.Transparent) {
                SciiCell.Transparent
            } else {
                cell
            }

            immutableCache = null
        }

        override fun undoSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            cells[sciiY][sciiX] = cell
            immutableCache = null
        }

        override fun toImmutable() = immutableCache
            ?: ImmutableCanvas.Scii(
                sciiWidth = sciiWidth,
                sciiHeight = sciiHeight,
                cells = cells.map { it.toList() },
            ).also { immutableCache = it }
    }

    class HBlock(
        sciiWidth: Int,
        sciiHeight: Int,
    ) : Canvas.HBlock(sciiWidth, sciiHeight), MutableCanvas<BlockDrawingCell> {
        override val cells: MutableList<MutableList<BlockDrawingCell>> =
            MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

        private var immutableCache: ImmutableCanvas.HBlock? = null

        override val isImmutableCached: Boolean
            get() = immutableCache != null

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            cells[drawingY][drawingX] = cell

            val otherDrawingY = if (drawingY % 2 == 0) drawingY + 1 else drawingY - 1
            val otherCell = cells[otherDrawingY][drawingX]

            cells[otherDrawingY][drawingX] = otherCell.copy(
                bright = cell.bright.merge(otherCell.bright),
            )

            immutableCache = null
        }

        override fun undoSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            // Assume SciiChar.BlockHorizontalTop

            val drawingY = sciiY * 2
            cells[drawingY][sciiX] = BlockDrawingCell(cell.ink, cell.bright)
            cells[drawingY + 1][sciiX] = BlockDrawingCell(cell.paper, cell.bright)

            immutableCache = null
        }

        override fun toImmutable() = immutableCache
            ?: ImmutableCanvas.HBlock(
                sciiWidth = sciiWidth,
                sciiHeight = sciiHeight,
                cells = cells.map { it.toList() },
            ).also { immutableCache = it }
    }

    class VBlock(
        sciiWidth: Int,
        sciiHeight: Int,
    ) : Canvas.VBlock(sciiWidth, sciiHeight), MutableCanvas<BlockDrawingCell> {
        override val cells: MutableList<MutableList<BlockDrawingCell>> =
            MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

        private var immutableCache: ImmutableCanvas.VBlock? = null

        override val isImmutableCached: Boolean
            get() = immutableCache != null

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            cells[drawingY][drawingX] = cell

            val otherDrawingX = if (drawingX % 2 == 0) drawingX + 1 else drawingX - 1
            val otherCell = cells[drawingY][otherDrawingX]

            cells[drawingY][otherDrawingX] = otherCell.copy(
                bright = cell.bright.merge(otherCell.bright),
            )

            immutableCache = null
        }

        override fun undoSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            // Assume SciiChar.BlockVerticalLeft

            val drawingX = sciiX * 2
            cells[sciiY][drawingX] = BlockDrawingCell(cell.ink, cell.bright)
            cells[sciiY][drawingX + 1] = BlockDrawingCell(cell.paper, cell.bright)

            immutableCache = null
        }

        override fun toImmutable() = immutableCache
            ?: ImmutableCanvas.VBlock(
                sciiWidth = sciiWidth,
                sciiHeight = sciiHeight,
                cells = cells.map { it.toList() },
            ).also { immutableCache = it }
    }

    class QBlock(
        override val sciiWidth: Int,
        override val sciiHeight: Int,
    ) : Canvas.QBlock(sciiWidth, sciiHeight), MutableCanvas<BlockDrawingCell> {
        override val pixels: MutableList<MutableList<Boolean>> =
            MutableList(drawingHeight) { MutableList(drawingWidth) { false } }

        override val attrs: MutableList<MutableList<BlockDrawingCell>> =
            MutableList(sciiHeight) { MutableList(sciiWidth) { BlockDrawingCell.Transparent } }

        private var immutableCache: ImmutableCanvas.QBlock? = null

        override val isImmutableCached: Boolean
            get() = immutableCache != null

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            if (cell.color == SciiColor.Transparent) {
                pixels[drawingY][drawingX] = false
            } else {
                pixels[drawingY][drawingX] = true

                val sciiX = drawingX / 2
                val sciiY = drawingY / 2
                attrs[sciiY][sciiX] = cell.merge(attrs[sciiY][sciiX])
            }

            immutableCache = null
        }

        override fun undoSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            // Assume chars between SciiChar.BLOCK_VALUE_FIRST and SciiChar.BLOCK_VALUE_LAST

            val drawingX = sciiX * 2
            val drawingY = sciiY * 2
            val charValue = cell.character.value

            pixels[drawingY][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_TR) != 0)
            pixels[drawingY][drawingX] = ((charValue and SciiChar.BLOCK_BIT_TL) != 0)
            pixels[drawingY + 1][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_BR) != 0)
            pixels[drawingY + 1][drawingX] = ((charValue and SciiChar.BLOCK_BIT_BL) != 0)

            attrs[sciiY][sciiX] = BlockDrawingCell(color = cell.ink, bright = cell.bright)
        }

        override fun toImmutable() = immutableCache
            ?: ImmutableCanvas.QBlock(
                sciiWidth = sciiWidth,
                sciiHeight = sciiHeight,
                pixels = pixels.map { it.toList() },
                attrs = attrs.map { it.toList() },
            ).also { immutableCache = it }
    }
}
