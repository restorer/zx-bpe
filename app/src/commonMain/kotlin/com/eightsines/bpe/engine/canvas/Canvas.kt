package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.engine.data.*

sealed interface Canvas<T> {
    val sciiWidth: Int
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell

    abstract class Scii(override val sciiWidth: Int, override val sciiHeight: Int) : Canvas<SciiCell> {
        @Suppress("LeakingThis")
        override val drawingWidth = sciiWidth

        @Suppress("LeakingThis")
        override val drawingHeight = sciiHeight

        protected abstract val cells: List<List<SciiCell>>

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY
        override fun getDrawingCell(drawingX: Int, drawingY: Int) = cells[drawingY][drawingX]
        override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell = cells[sciiY][sciiX]
    }

    abstract class HBlock(
        override val sciiWidth: Int,
        override val sciiHeight: Int,
    ) : Canvas<BlockDrawingCell> {
        @Suppress("LeakingThis")
        override val drawingWidth = sciiWidth

        @Suppress("LeakingThis")
        override val drawingHeight = sciiHeight * 2

        protected abstract val cells: List<List<BlockDrawingCell>>

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)
        override fun getDrawingCell(drawingX: Int, drawingY: Int) = cells[drawingY][drawingX]

        override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
            val drawingY = sciiY * 2
            val topCell = cells[drawingY][sciiX]
            val bottomCell = cells[drawingY + 1][sciiX]

            return BlockMergeCell.Horizontal.makeSciiCell(
                topColor = topCell.color,
                bottomColor = bottomCell.color,
                bright = topCell.bright.merge(bottomCell.bright),
            )
        }

        fun getMergeCell(sciiX: Int, sciiY: Int): BlockMergeCell.Horizontal {
            val drawingY = sciiY * 2
            val topCell = cells[drawingY][sciiX]
            val bottomCell = cells[drawingY + 1][sciiX]

            return BlockMergeCell.Horizontal(
                topColor = topCell.color,
                bottomColor = bottomCell.color,
                bright = topCell.bright.merge(bottomCell.bright),
            )
        }
    }

    abstract class VBlock(
        override val sciiWidth: Int,
        override val sciiHeight: Int,
    ) : Canvas<BlockDrawingCell> {
        @Suppress("LeakingThis")
        override val drawingWidth = sciiWidth * 2

        @Suppress("LeakingThis")
        override val drawingHeight = sciiHeight

        protected abstract val cells: List<List<BlockDrawingCell>>

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY
        override fun getDrawingCell(drawingX: Int, drawingY: Int) = cells[drawingY][drawingX]

        override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
            val drawingX = sciiX * 2
            val leftCell = cells[sciiY][drawingX]
            val rightCell = cells[sciiY][drawingX + 1]

            return BlockMergeCell.Vertical.makeSciiCell(
                leftColor = leftCell.color,
                rightColor = rightCell.color,
                bright = leftCell.bright.merge(rightCell.bright),
            )
        }

        fun getMergeCell(sciiX: Int, sciiY: Int): BlockMergeCell.Vertical {
            val drawingX = sciiX * 2
            val leftCell = cells[sciiY][drawingX]
            val rightCell = cells[sciiY][drawingX + 1]

            return BlockMergeCell.Vertical(
                leftColor = leftCell.color,
                rightColor = rightCell.color,
                bright = leftCell.bright.merge(rightCell.bright),
            )
        }
    }

    abstract class QBlock(
        override val sciiWidth: Int,
        override val sciiHeight: Int,
    ) : Canvas<BlockDrawingCell> {
        @Suppress("LeakingThis")
        override val drawingWidth = sciiWidth * 2

        @Suppress("LeakingThis")
        override val drawingHeight = sciiHeight * 2

        protected abstract val pixels: List<List<Boolean>>
        protected abstract val attrs: List<List<BlockDrawingCell>>

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to (drawingY / 2)

        override fun getDrawingCell(drawingX: Int, drawingY: Int) = if (pixels[drawingY][drawingX]) {
            BlockDrawingCell.Transparent
        } else {
            attrs[drawingY / 2][drawingX / 2]
        }

        override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
            val drawingX = sciiX * 2
            val drawingY = sciiY * 2

            val charValue = SciiChar.BLOCK_VALUE_FIRST +
                    (if (pixels[drawingY][drawingX + 1]) SciiChar.BLOCK_BIT_TR else 0) +
                    (if (pixels[drawingY][drawingX]) SciiChar.BLOCK_BIT_TL else 0) +
                    (if (pixels[drawingY + 1][drawingX + 1]) SciiChar.BLOCK_BIT_BR else 0) +
                    (if (pixels[drawingY + 1][drawingX]) SciiChar.BLOCK_BIT_BL else 0)

            return if (charValue == SciiChar.BLOCK_VALUE_FIRST) {
                SciiCell.Transparent
            } else {
                val attr = attrs[sciiY][sciiX]

                SciiCell(
                    character = SciiChar(charValue),
                    ink = attr.color,
                    paper = SciiColor.Transparent,
                    bright = attr.bright,
                    flash = SciiLight.Transparent,
                )
            }
        }
    }

    companion object {
        const val SCREEN_SCII_WIDTH = 32
        const val SCREEN_SCII_HEIGHT = 24
    }
}
