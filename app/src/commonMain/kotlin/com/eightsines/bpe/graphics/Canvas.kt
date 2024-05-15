package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.CellType
import com.eightsines.bpe.model.HBlockMergeCell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.model.VBlockMergeCell
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.PackableBag

enum class CanvasType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Canvas<*>>) {
    Scii(1, SciiCanvas.Polymorphic),
    HBlock(2, HBlockCanvas.Polymorphic),
    VBlock(3, VBlockCanvas.Polymorphic),
    QBlock(4, QBlockCanvas.Polymorphic),
}

interface Canvas<T : Cell> {
    val type: CanvasType
    val cellType: CellType

    val sciiWidth: Int
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    val mutations: Int

    fun copyMutable(): MutableCanvas<T>
    fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell

    companion object : BagStuffPacker<Canvas<*>> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Canvas<*>) {
            bag.put(value.type.value)
            bag.put(value.sciiWidth)
            bag.put(value.sciiHeight)

            @Suppress("UNCHECKED_CAST")
            bag.put(value.type.polymorphicPacker as BagStuffPacker<Canvas<*>>, value)
        }
    }
}

interface BlockCanvas : Canvas<BlockCell>

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

    internal object Polymorphic : BagStuffPacker<SciiCanvas> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: SciiCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell, cell)
                }
            }
        }
    }
}

abstract class HBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockCell>, BlockCanvas {
    override val type = CanvasType.HBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val cells: List<List<BlockCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 || drawingY < 0 || drawingX >= drawingWidth || drawingY >= drawingHeight) {
            BlockCell.Transparent
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

    fun getMergeCell(sciiX: Int, sciiY: Int): HBlockMergeCell {
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

    internal object Polymorphic : BagStuffPacker<HBlockCanvas> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: HBlockCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell, cell)
                }
            }
        }
    }
}

abstract class VBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockCell>, BlockCanvas {
    override val type = CanvasType.VBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<BlockCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 || drawingY < 0 || drawingX >= drawingWidth || drawingY >= drawingHeight) {
            BlockCell.Transparent
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

    fun getMergeCell(sciiX: Int, sciiY: Int): VBlockMergeCell {
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

    internal object Polymorphic : BagStuffPacker<VBlockCanvas> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: VBlockCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell, cell)
                }
            }
        }
    }
}

abstract class QBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
) : Canvas<BlockCell>, BlockCanvas {
    override val type = CanvasType.QBlock
    override val cellType = CellType.Block

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val pixels: List<List<Boolean>>
    protected abstract val attrs: List<List<BlockCell>>

    override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to (drawingY / 2)

    override fun getDrawingCell(drawingX: Int, drawingY: Int) =
        if (drawingX < 0 ||
            drawingY < 0 ||
            drawingX >= drawingWidth ||
            drawingY >= drawingHeight ||
            !pixels[drawingY][drawingX]
        ) {
            BlockCell.Transparent
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

    internal object Polymorphic : BagStuffPacker<QBlockCanvas> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: QBlockCanvas) {
            for (line in value.pixels) {
                for (pixel in line) {
                    bag.put(pixel)
                }
            }

            for (line in value.attrs) {
                for (attr in line) {
                    bag.put(Cell, attr)
                }
            }
        }
    }
}
