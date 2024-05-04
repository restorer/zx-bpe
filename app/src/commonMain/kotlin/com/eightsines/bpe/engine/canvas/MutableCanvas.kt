package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.VersionedUnpackableBag
import com.eightsines.bpe.engine.cell.BlockDrawingCell
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor

interface MutableCanvas<T : Cell> : Canvas<T> {
    val changeVersion: Int

    fun putDrawingCell(drawingX: Int, drawingY: Int, cell: T)
    fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell)

    companion object : BagStuff.Unpacker<MutableCanvas<*>> {
        override fun getOutOfTheBag(versionedBag: VersionedUnpackableBag): MutableCanvas<*> =
            versionedBag.getVersion().let { (version, bag) ->
                if (version != 1) {
                    throw BagUnpackException("Unsupported version=$version for MutableCanvas")
                }

                val type = bag.getString()
                val sciiWidth = bag.getInt()
                val sciiHeight = bag.getInt()

                return when (type) {
                    CanvasType.Scii.value -> MutableSciiCanvas.getOutOfTheBag(sciiWidth, sciiHeight, bag)
                    CanvasType.HBlock.value -> MutableHBlockCanvas.getOutOfTheBag(sciiWidth, sciiHeight, bag)
                    CanvasType.VBlock.value -> MutableVBlockCanvas.getOutOfTheBag(sciiWidth, sciiHeight, bag)
                    CanvasType.QBlock.value -> MutableQBlockCanvas.getOutOfTheBag(sciiWidth, sciiHeight, bag)
                    else -> throw BagUnpackException("Unknown type=\"$type\" for MutableCanvas")
                }
            }
    }
}

interface MutableBlockCanvas : MutableCanvas<BlockDrawingCell>

class MutableSciiCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
) : SciiCanvas(sciiWidth, sciiHeight), MutableCanvas<SciiCell> {
    override val cells: MutableList<MutableList<SciiCell>> =
        MutableList(drawingHeight) { MutableList(drawingWidth) { SciiCell.Transparent } }

    override var changeVersion: Int = 0
        private set

    override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: SciiCell) {
        cells[drawingY][drawingX] = if (cell.character == SciiChar.Transparent) {
            SciiCell.Transparent
        } else {
            cell
        }

        ++changeVersion
    }

    override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
        cells[sciiY][sciiX] = cell
        ++changeVersion
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableSciiCanvas {
            val canvas = MutableSciiCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(SciiCell.Companion::getOutOfTheBag)
                }
            }

            return canvas
        }
    }
}

class MutableHBlockCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
) : HBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockDrawingCell>> =
        MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

    override var changeVersion: Int = 0
        private set

    override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
        cells[drawingY][drawingX] = cell

        val otherDrawingY = if (drawingY % 2 == 0) drawingY + 1 else drawingY - 1
        val otherCell = cells[otherDrawingY][drawingX]

        cells[otherDrawingY][drawingX] = otherCell.copy(
            bright = cell.bright.merge(otherCell.bright),
        )

        ++changeVersion
    }

    override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
        // Assume that cell.character is SciiChar.BlockHorizontalTop

        val drawingY = sciiY * 2
        cells[drawingY][sciiX] = BlockDrawingCell(cell.ink, cell.bright)
        cells[drawingY + 1][sciiX] = BlockDrawingCell(cell.paper, cell.bright)

        ++changeVersion
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableHBlockCanvas {
            val canvas = MutableHBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(BlockDrawingCell.Companion::getOutOfTheBag)
                }
            }

            return canvas
        }
    }
}

class MutableVBlockCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
) : VBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockDrawingCell>> =
        MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

    override var changeVersion: Int = 0
        private set

    override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
        cells[drawingY][drawingX] = cell

        val otherDrawingX = if (drawingX % 2 == 0) drawingX + 1 else drawingX - 1
        val otherCell = cells[drawingY][otherDrawingX]

        cells[drawingY][otherDrawingX] = otherCell.copy(
            bright = cell.bright.merge(otherCell.bright),
        )

        ++changeVersion
    }

    override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
        // Assume that cell.character is SciiChar.BlockVerticalLeft

        val drawingX = sciiX * 2
        cells[sciiY][drawingX] = BlockDrawingCell(cell.ink, cell.bright)
        cells[sciiY][drawingX + 1] = BlockDrawingCell(cell.paper, cell.bright)

        ++changeVersion
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableVBlockCanvas {
            val canvas = MutableVBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(BlockDrawingCell.Companion::getOutOfTheBag)
                }
            }

            return canvas
        }
    }
}

class MutableQBlockCanvas(
    override val sciiWidth: Int,
    override val sciiHeight: Int,
    pixels: List<List<Boolean>>? = null,
    attrs: List<List<BlockDrawingCell>>? = null,
) : QBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val pixels: MutableList<MutableList<Boolean>> =
        pixels?.let { it.map { it.toMutableList() }.toMutableList() }
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { false } }

    override val attrs: MutableList<MutableList<BlockDrawingCell>> =
        attrs?.let { it.map { it.toMutableList() }.toMutableList() }
            ?: MutableList(sciiHeight) { MutableList(sciiWidth) { BlockDrawingCell.Transparent } }

    override var changeVersion: Int = 0
        private set

    override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
        if (cell.color == SciiColor.Transparent) {
            pixels[drawingY][drawingX] = false
        } else {
            pixels[drawingY][drawingX] = true

            val sciiX = drawingX / 2
            val sciiY = drawingY / 2
            attrs[sciiY][sciiX] = cell.merge(attrs[sciiY][sciiX])
        }

        ++changeVersion
    }

    override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
        // Assume that cell.character.value is between SciiChar.BLOCK_VALUE_FIRST and SciiChar.BLOCK_VALUE_LAST

        val drawingX = sciiX * 2
        val drawingY = sciiY * 2
        val charValue = cell.character.value

        pixels[drawingY][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_TR) != 0)
        pixels[drawingY][drawingX] = ((charValue and SciiChar.BLOCK_BIT_TL) != 0)
        pixels[drawingY + 1][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_BR) != 0)
        pixels[drawingY + 1][drawingX] = ((charValue and SciiChar.BLOCK_BIT_BL) != 0)

        attrs[sciiY][sciiX] = BlockDrawingCell(color = cell.ink, bright = cell.bright)
        ++changeVersion
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableQBlockCanvas {
            val canvas = MutableQBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.pixels[y][x] = bag.getBoolean()
                }
            }

            for (y in 0..<sciiHeight) {
                for (x in 0..<sciiWidth) {
                    canvas.attrs[y][x] = bag.getStuff(BlockDrawingCell.Companion::getOutOfTheBag)
                }
            }

            return canvas
        }
    }
}
