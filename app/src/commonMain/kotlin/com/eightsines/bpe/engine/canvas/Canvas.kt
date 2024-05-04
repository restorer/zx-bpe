package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.VersionedPackableBag
import com.eightsines.bpe.engine.cell.BlockDrawingCell
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.HorizontalBlockMergeCell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.cell.VerticalBlockMergeCell
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface Canvas<T : Cell> : BagStuff {
    val type: CanvasType

    val sciiWidth: Int
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell

    companion object {
        const val SCREEN_SCII_WIDTH = 32
        const val SCREEN_SCII_HEIGHT = 24
    }
}

interface BlockCanvas : Canvas<BlockDrawingCell>

enum class CanvasType(val value: String) {
    Scii("scii"), HBlock("hblock"), VBlock("vblock"), QBlock("qblock"),
}

abstract class BasicCanvas<T : Cell> : Canvas<T> {
    protected abstract fun putInTheBag(bag: PackableBag)

    override fun putInTheBag(versionedBag: VersionedPackableBag) =
        versionedBag.putVersion(1).let {
            it.put(type.value)
            it.put(sciiWidth)
            it.put(sciiHeight)

            putInTheBag(it)
        }
}

abstract class SciiCanvas(override val sciiWidth: Int, override val sciiHeight: Int) : BasicCanvas<SciiCell>() {
    override val type = CanvasType.Scii

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<SciiCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY
    override fun getDrawingCell(drawingX: Int, drawingY: Int) = cells[drawingY][drawingX]
    override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell = cells[sciiY][sciiX]

    override fun putInTheBag(bag: PackableBag) = bag.run {
        for (line in cells) {
            for (cell in line) {
                put(cell)
            }
        }
    }
}

abstract class HBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : BasicCanvas<BlockDrawingCell>(), BlockCanvas {
    override val type = CanvasType.HBlock

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

        return HorizontalBlockMergeCell.makeSciiCell(
            topColor = topCell.color,
            bottomColor = bottomCell.color,
            bright = topCell.bright.merge(bottomCell.bright),
        )
    }

    fun getMergeCell(sciiX: Int, sciiY: Int): HorizontalBlockMergeCell {
        val drawingY = sciiY * 2
        val topCell = cells[drawingY][sciiX]
        val bottomCell = cells[drawingY + 1][sciiX]

        return HorizontalBlockMergeCell(
            topColor = topCell.color,
            bottomColor = bottomCell.color,
            bright = topCell.bright.merge(bottomCell.bright),
        )
    }

    override fun putInTheBag(bag: PackableBag) = bag.run {
        for (line in cells) {
            for (cell in line) {
                put(cell)
            }
        }
    }
}

abstract class VBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : BasicCanvas<BlockDrawingCell>(), BlockCanvas {
    override val type = CanvasType.VBlock

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

        return VerticalBlockMergeCell.makeSciiCell(
            leftColor = leftCell.color,
            rightColor = rightCell.color,
            bright = leftCell.bright.merge(rightCell.bright),
        )
    }

    fun getMergeCell(sciiX: Int, sciiY: Int): VerticalBlockMergeCell {
        val drawingX = sciiX * 2
        val leftCell = cells[sciiY][drawingX]
        val rightCell = cells[sciiY][drawingX + 1]

        return VerticalBlockMergeCell(
            leftColor = leftCell.color,
            rightColor = rightCell.color,
            bright = leftCell.bright.merge(rightCell.bright),
        )
    }

    override fun putInTheBag(bag: PackableBag) = bag.run {
        for (line in cells) {
            for (cell in line) {
                put(cell)
            }
        }
    }
}

abstract class QBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : BasicCanvas<BlockDrawingCell>(), BlockCanvas {
    override val type = CanvasType.QBlock

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

    override fun putInTheBag(bag: PackableBag) = bag.run {
        for (line in pixels) {
            for (pixel in line) {
                bag.put(pixel)
            }
        }

        for (line in attrs) {
            for (attr in line) {
                bag.put(attr)
            }
        }
    }
}
