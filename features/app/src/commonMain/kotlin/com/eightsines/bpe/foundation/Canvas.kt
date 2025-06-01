package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagSinglefield
import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.core.Cell_Stuff
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

@BagSinglefield(field = "value", creator = "of")
sealed class CanvasType {
    abstract val value: Int
    abstract val cellType: CellType
    abstract val transparentCell: Cell

    abstract fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>

    data object Scii : CanvasType() {
        const val POLYMORPHIC_ID = 1

        override val value = POLYMORPHIC_ID
        override val cellType = CellType.Scii
        override val transparentCell = SciiCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY
    }

    data object HBlock : CanvasType() {
        const val POLYMORPHIC_ID = 2

        override val value = Scii.POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)
    }

    data object VBlock : CanvasType() {
        const val POLYMORPHIC_ID = 3

        override val value = Scii.POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY
    }

    data object QBlock : CanvasType() {
        const val POLYMORPHIC_ID = 4

        override val value = Scii.POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

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
            Scii.POLYMORPHIC_ID -> Scii
            HBlock.POLYMORPHIC_ID -> HBlock
            VBlock.POLYMORPHIC_ID -> VBlock
            QBlock.POLYMORPHIC_ID -> QBlock
            else -> throw IllegalArgumentException("Unknown enum value=$value for CanvasType")
        }
    }
}

@BagStuff(unpacker = "MutableCanvas", isPolymorphic = true)
interface Canvas<T : Cell> {
    val type: CanvasType

    @BagStuffWare(1)
    val sciiWidth: Int

    @BagStuffWare(2)
    val sciiHeight: Int

    val drawingWidth: Int
    val drawingHeight: Int

    val mutations: Int

    fun copyMutable(): MutableCanvas<T>
    fun getDrawingCell(drawingX: Int, drawingY: Int): T
    fun getSciiCell(sciiX: Int, sciiY: Int): SciiCell
}

interface BlockCanvas : Canvas<BlockCell>

@BagStuff(unpacker = "_", polymorphicOf = Canvas::class, polymorphicId = CanvasType.Scii.POLYMORPHIC_ID)
@BagStuffWare(1, field = "cells", packer = "putCellsInTheBag")
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

    internal companion object {
        fun putCellsInTheBag(bag: PackableBag, value: SciiCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell_Stuff, cell)
                }
            }
        }
    }
}

@BagStuff(unpacker = "_", polymorphicOf = Canvas::class, polymorphicId = 2)
@BagStuffWare(1, field = "cells", packer = "putCellsInTheBag")
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

    internal companion object {
        fun putCellsInTheBag(bag: PackableBag, value: HBlockCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell_Stuff, cell)
                }
            }
        }
    }
}

@BagStuff(unpacker = "_", polymorphicOf = Canvas::class, polymorphicId = 3)
@BagStuffWare(1, field = "cells", packer = "putCellsInTheBag")
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

    internal companion object {
        fun putCellsInTheBag(bag: PackableBag, value: VBlockCanvas) {
            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell_Stuff, cell)
                }
            }
        }
    }
}

@BagStuff(unpacker = "_", polymorphicOf = Canvas::class, polymorphicId = 4)
@BagStuffWare(1, field = "pixels", packer = "putPixelsInTheBag")
@BagStuffWare(2, field = "attrs", packer = "putAttrsInTheBag")
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

    internal companion object {
        fun putPixelsInTheBag(bag: PackableBag, value: QBlockCanvas) {
            for (line in value.pixels) {
                for (pixel in line) {
                    bag.put(pixel)
                }
            }
        }

        fun putAttrsInTheBag(bag: PackableBag, value: QBlockCanvas) {
            for (line in value.attrs) {
                for (attr in line) {
                    bag.put(Cell_Stuff, attr)
                }
            }
        }
    }
}
