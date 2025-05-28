package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagSinglefield
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

@BagSinglefield(field = "value", creator = "CanvasType.of")
sealed class CanvasType {
    abstract val value: Int
    abstract val cellType: CellType
    abstract val transparentCell: Cell
    internal abstract val polymorphicPacker: BagStuffPacker<out Canvas<*>>

    abstract fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>

    data object Scii : CanvasType() {
        override val value = 1
        override val cellType = CellType.Scii
        override val transparentCell = SciiCell.Transparent
        override val polymorphicPacker = SciiCanvas.Polymorphic

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY
    }

    data object HBlock : CanvasType() {
        override val value = 2
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent
        override val polymorphicPacker = HBlockCanvas.Polymorphic

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)
    }

    data object VBlock : CanvasType() {
        override val value = 3
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent
        override val polymorphicPacker = VBlockCanvas.Polymorphic

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY
    }

    data object QBlock : CanvasType() {
        override val value = 4
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent
        override val polymorphicPacker = QBlockCanvas.Polymorphic

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to (drawingY / 2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as CanvasType
        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    companion object {
        fun of(value: Int) = when (value) {
            Scii.value -> Scii
            HBlock.value -> HBlock
            VBlock.value -> VBlock
            QBlock.value -> QBlock
            else -> throw IllegalArgumentException("Unknown enum value=$value for CanvasType")
        }
    }
}

interface Canvas<T : Cell> {
    val type: CanvasType

    val sciiWidth: Int
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    val mutations: Int

    fun copyMutable(): MutableCanvas<T>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell

    companion object : BagStuffPacker<Canvas<*>> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Canvas<*>) {
            bag.put(value.type.value)
            bag.put(value.sciiWidth)
            bag.put(value.sciiHeight)
            bag.put(value.type.polymorphicPacker, value)
        }
    }
}

interface BlockCanvas : Canvas<BlockCell>

abstract class SciiCanvas(override val sciiWidth: Int, override val sciiHeight: Int) : Canvas<SciiCell> {
    override val type = CanvasType.Scii

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<SciiCell>>

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

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val cells: List<List<BlockCell>>

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

        return if (topCell.color == SciiColor.Transparent && bottomCell.color == SciiColor.Transparent) {
            SciiCell.Transparent
        } else {
            SciiCell(
                character = SciiChar.BlockHorizontalTop,
                ink = topCell.color,
                paper = bottomCell.color,
                bright = topCell.bright.merge(bottomCell.bright),
                flash = SciiLight.Transparent,
            )
        }
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

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight

    protected abstract val cells: List<List<BlockCell>>

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

        return if (leftCell.color == SciiColor.Transparent && rightCell.color == SciiColor.Transparent) {
            SciiCell.Transparent
        } else {
            SciiCell(
                character = SciiChar.BlockVerticalLeft,
                ink = leftCell.color,
                paper = rightCell.color,
                bright = leftCell.bright.merge(rightCell.bright),
                flash = SciiLight.Transparent,
            )
        }
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

    @Suppress("LeakingThis")
    override val drawingWidth = sciiWidth * 2

    @Suppress("LeakingThis")
    override val drawingHeight = sciiHeight * 2

    protected abstract val pixels: List<List<Boolean>>
    protected abstract val attrs: List<List<BlockCell>>

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

            if (charValue == SciiChar.BLOCK_VALUE_LAST) {
                SciiCell(
                    character = SciiChar(SciiChar.BLOCK_VALUE_FIRST),
                    ink = SciiColor.Transparent,
                    paper = attr.color,
                    bright = attr.bright,
                    flash = SciiLight.Transparent,
                )
            } else {
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
