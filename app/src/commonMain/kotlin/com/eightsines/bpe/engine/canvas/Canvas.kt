package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.engine.cell.BlockDrawingCell
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.CellType
import com.eightsines.bpe.engine.cell.HBlockMergeCell
import com.eightsines.bpe.engine.cell.MergeCell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.cell.SciiMergeCell
import com.eightsines.bpe.engine.cell.VBlockMergeCell
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

enum class CanvasType(val value: String) {
    Scii("scii"),
    HBlock("hblock"),
    VBlock("vblock"),
    QBlock("qblock"),
}

interface Canvas<T : Cell> : BagStuff {
    val type: CanvasType
    val cellType: CellType

    val sciiWidth: Int
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    override val bagStuffVersion: Int
        get() = 1

    fun copyMutable(): MutableCanvas<T>
    fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell
    fun getMergeCell(sciiX: Int, sciiY: Int): MergeCell
}

interface BlockCanvas : Canvas<BlockDrawingCell>

private fun <T : Cell> Canvas<T>.putInTheBagBase(bag: PackableBag) {
    bag.put(type.value)
    bag.put(sciiWidth)
    bag.put(sciiHeight)
}

abstract class SciiCanvas(override val sciiWidth: Int, override val sciiHeight: Int) : Canvas<SciiCell> {
    override val type = CanvasType.Scii
    override val cellType = CellType.Scii

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<SciiCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 || drawingY < 0 || drawingX >= drawingWidth || drawingY >= drawingHeight) {
            SciiCell.Transparent
        } else {
            cells[drawingY][drawingX]
        }

    override fun getSciiCell(sciiX: Int, sciiY: Int) =
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            SciiCell.Transparent
        } else {
            cells[sciiY][sciiX]
        }

    override fun getMergeCell(sciiX: Int, sciiY: Int) = SciiMergeCell(getSciiCell(sciiX, sciiY))

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        for (line in cells) {
            for (cell in line) {
                bag.put(cell)
            }
        }
    }
}

abstract class HBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockDrawingCell>, BlockCanvas {
    override val type = CanvasType.HBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val cells: List<List<BlockDrawingCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 || drawingY < 0 || drawingX >= drawingWidth || drawingY >= drawingHeight) {
            BlockDrawingCell.Transparent
        } else {
            cells[drawingY][drawingX]
        }

    override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            return SciiCell.Transparent
        }

        val drawingY = sciiY * 2
        val topCell = cells[drawingY][sciiX]
        val bottomCell = cells[drawingY + 1][sciiX]

        return HBlockMergeCell.makeSciiCell(
            topColor = topCell.color,
            bottomColor = bottomCell.color,
            bright = topCell.bright.merge(bottomCell.bright),
        )
    }

    override fun getMergeCell(sciiX: Int, sciiY: Int): HBlockMergeCell {
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            return HBlockMergeCell.Transparent
        }

        val drawingY = sciiY * 2
        val topCell = cells[drawingY][sciiX]
        val bottomCell = cells[drawingY + 1][sciiX]

        return HBlockMergeCell(
            topColor = topCell.color,
            bottomColor = bottomCell.color,
            bright = topCell.bright.merge(bottomCell.bright),
        )
    }

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        for (line in cells) {
            for (cell in line) {
                bag.put(cell)
            }
        }
    }
}

abstract class VBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockDrawingCell>, BlockCanvas {
    override val type = CanvasType.VBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<BlockDrawingCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 || drawingY < 0 || drawingX >= drawingWidth || drawingY >= drawingHeight) {
            BlockDrawingCell.Transparent
        } else {
            cells[drawingY][drawingX]
        }

    override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            return SciiCell.Transparent
        }

        val drawingX = sciiX * 2
        val leftCell = cells[sciiY][drawingX]
        val rightCell = cells[sciiY][drawingX + 1]

        return VBlockMergeCell.makeSciiCell(
            leftColor = leftCell.color,
            rightColor = rightCell.color,
            bright = leftCell.bright.merge(rightCell.bright),
        )
    }

    override fun getMergeCell(sciiX: Int, sciiY: Int): VBlockMergeCell {
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            return VBlockMergeCell.Transparent
        }

        val drawingX = sciiX * 2
        val leftCell = cells[sciiY][drawingX]
        val rightCell = cells[sciiY][drawingX + 1]

        return VBlockMergeCell(
            leftColor = leftCell.color,
            rightColor = rightCell.color,
            bright = leftCell.bright.merge(rightCell.bright),
        )
    }

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        for (line in cells) {
            for (cell in line) {
                bag.put(cell)
            }
        }
    }
}

abstract class QBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockDrawingCell>, BlockCanvas {
    override val type = CanvasType.QBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val pixels: List<List<Boolean>>
    protected abstract val attrs: List<List<BlockDrawingCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to (drawingY / 2)

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 ||
            drawingY < 0 ||
            drawingX >= drawingWidth ||
            drawingY >= drawingHeight ||
            !pixels[drawingY][drawingX]
        ) {
            BlockDrawingCell.Transparent
        } else {
            attrs[drawingY / 2][drawingX / 2]
        }

    override fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell {
        if (sciiX < 0 || sciiY < 0 || sciiX >= sciiWidth || sciiY >= sciiHeight) {
            return SciiCell.Transparent
        }

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

    override fun getMergeCell(sciiX: Int, sciiY: Int) = SciiMergeCell(getSciiCell(sciiX, sciiY))

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

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
