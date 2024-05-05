package com.eightsines.bpe.engine.canvas

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.cell.BlockDrawingCell
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.HBlockMergeCell
import com.eightsines.bpe.engine.cell.MergeCell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.cell.VBlockMergeCell
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor

interface MutableCanvas<T : Cell> : Canvas<T> {
    val mutations: Int

    fun mutate(block: (mutator: CanvasMutator<T>) -> Unit)

    companion object : BagStuff.Unpacker<MutableCanvas<*>> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableCanvas<*> {
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

interface CanvasMutator<T : Cell> {
    fun clear()
    fun putDrawingCell(drawingX: Int, drawingY: Int, cell: T)
    fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell)
    fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: MergeCell)
}

interface MutableBlockCanvas : MutableCanvas<BlockDrawingCell>

class MutableSciiCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
    source: MutableSciiCanvas? = null,
) : SciiCanvas(sciiWidth, sciiHeight), MutableCanvas<SciiCell> {
    override val cells: MutableList<MutableList<SciiCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { SciiCell.Transparent } }

    override var mutations: Int = source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableSciiCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<SciiCell>) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    private class Mutator(private val canvas: MutableSciiCanvas) : CanvasMutator<SciiCell> {
        override fun clear() {
            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = SciiCell.Transparent
                }
            }
        }

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: SciiCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX > canvas.drawingWidth || drawingY > canvas.drawingHeight) {
                return
            }

            canvas.cells[drawingY][drawingX] = if (cell.character == SciiChar.Transparent) {
                SciiCell.Transparent
            } else {
                cell
            }
        }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX > canvas.sciiWidth || sciiY > canvas.sciiHeight) {
                return
            }

            canvas.cells[sciiY][sciiX] = cell
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: MergeCell) =
            replaceSciiCell(sciiX, sciiY, cell.toSciiCell())
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableSciiCanvas {
            val canvas = MutableSciiCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(SciiCell.Companion)
                }
            }

            return canvas
        }
    }
}

class MutableHBlockCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
    source: MutableHBlockCanvas? = null,
) : HBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockDrawingCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

    override var mutations: Int = source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableHBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockDrawingCell>) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    private class Mutator(private val canvas: MutableHBlockCanvas) : CanvasMutator<BlockDrawingCell> {
        override fun clear() {
            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = BlockDrawingCell.Transparent
                }
            }
        }

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX > canvas.drawingWidth || drawingY > canvas.drawingHeight) {
                return
            }

            canvas.cells[drawingY][drawingX] = cell

            val otherDrawingY = if (drawingY % 2 == 0) drawingY + 1 else drawingY - 1
            val otherCell = canvas.cells[otherDrawingY][drawingX]

            canvas.cells[otherDrawingY][drawingX] = otherCell.copy(
                bright = cell.bright.merge(otherCell.bright),
            )
        }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            // Assume that cell.character is SciiChar.BlockHorizontalTop

            if (sciiX < 0 || sciiY < 0 || sciiX > canvas.sciiWidth || sciiY > canvas.sciiHeight) {
                return
            }

            val drawingY = sciiY * 2
            canvas.cells[drawingY][sciiX] = BlockDrawingCell(cell.ink, cell.bright)
            canvas.cells[drawingY + 1][sciiX] = BlockDrawingCell(cell.paper, cell.bright)
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: MergeCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX > canvas.sciiWidth || sciiY > canvas.sciiHeight) {
                return
            }

            if (cell !is HBlockMergeCell) {
                replaceSciiCell(sciiX, sciiY, cell.toSciiCell())
                return
            }

            val drawingY = sciiY * 2

            canvas.cells[drawingY][sciiX] = BlockDrawingCell(
                color = cell.topColor,
                bright = cell.bright,
            )

            canvas.cells[drawingY + 1][sciiX] = BlockDrawingCell(
                color = cell.bottomColor,
                bright = cell.bright,
            )
        }
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableHBlockCanvas {
            val canvas = MutableHBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(BlockDrawingCell.Companion)
                }
            }

            return canvas
        }
    }
}

class MutableVBlockCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
    source: MutableVBlockCanvas? = null,
) : VBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockDrawingCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { BlockDrawingCell.Transparent } }

    override var mutations: Int = source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableVBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockDrawingCell>) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    private class Mutator(private val canvas: MutableVBlockCanvas) : CanvasMutator<BlockDrawingCell> {
        override fun clear() {
            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = BlockDrawingCell.Transparent
                }
            }
        }

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX > canvas.drawingWidth || drawingY > canvas.drawingHeight) {
                return
            }

            canvas.cells[drawingY][drawingX] = cell

            val otherDrawingX = if (drawingX % 2 == 0) drawingX + 1 else drawingX - 1
            val otherCell = canvas.cells[drawingY][otherDrawingX]

            canvas.cells[drawingY][otherDrawingX] = otherCell.copy(
                bright = cell.bright.merge(otherCell.bright),
            )
        }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (cell.character != SciiChar.BlockVerticalLeft ||
                sciiX < 0 ||
                sciiY < 0 ||
                sciiX > canvas.sciiWidth ||
                sciiY > canvas.sciiHeight
            ) {
                return
            }

            val drawingX = sciiX * 2
            canvas.cells[sciiY][drawingX] = BlockDrawingCell(cell.ink, cell.bright)
            canvas.cells[sciiY][drawingX + 1] = BlockDrawingCell(cell.paper, cell.bright)
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: MergeCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX > canvas.sciiWidth || sciiY > canvas.sciiHeight) {
                return
            }

            if (cell !is VBlockMergeCell) {
                replaceSciiCell(sciiX, sciiY, cell.toSciiCell())
                return
            }

            val drawingX = sciiX * 2

            canvas.cells[sciiY][drawingX] = BlockDrawingCell(
                color = cell.leftColor,
                bright = cell.bright,
            )

            canvas.cells[sciiY][drawingX + 1] = BlockDrawingCell(
                color = cell.rightColor,
                bright = cell.bright,
            )
        }
    }

    companion object {
        fun getOutOfTheBag(sciiWidth: Int, sciiHeight: Int, bag: UnpackableBag): MutableVBlockCanvas {
            val canvas = MutableVBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingHeight) {
                    canvas.cells[y][x] = bag.getStuff(BlockDrawingCell.Companion)
                }
            }

            return canvas
        }
    }
}

class MutableQBlockCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
    source: MutableQBlockCanvas? = null,
) : QBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val pixels: MutableList<MutableList<Boolean>> =
        source?.pixels?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { false } }

    override val attrs: MutableList<MutableList<BlockDrawingCell>> =
        source?.attrs?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(sciiHeight) { MutableList(sciiWidth) { BlockDrawingCell.Transparent } }

    override var mutations: Int = source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableQBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockDrawingCell>) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    private class Mutator(private val canvas: MutableQBlockCanvas) : CanvasMutator<BlockDrawingCell> {
        override fun clear() {
            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.pixels[y][x] = false
                }
            }

            for (y in 0..<canvas.sciiHeight) {
                for (x in 0..<canvas.sciiWidth) {
                    canvas.attrs[y][x] = BlockDrawingCell.Transparent
                }
            }
        }

        override fun putDrawingCell(drawingX: Int, drawingY: Int, cell: BlockDrawingCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX > canvas.drawingWidth || drawingY > canvas.drawingHeight) {
                return
            }

            if (cell.color == SciiColor.Transparent) {
                canvas.pixels[drawingY][drawingX] = false
            } else {
                canvas.pixels[drawingY][drawingX] = true

                val sciiX = drawingX / 2
                val sciiY = drawingY / 2
                canvas.attrs[sciiY][sciiX] = cell.merge(canvas.attrs[sciiY][sciiX])
            }
        }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            // Assume that cell.character.value is between SciiChar.BLOCK_VALUE_FIRST and SciiChar.BLOCK_VALUE_LAST

            if (sciiX < 0 || sciiY < 0 || sciiX > canvas.sciiWidth || sciiY > canvas.sciiHeight) {
                return
            }

            val drawingX = sciiX * 2
            val drawingY = sciiY * 2
            val charValue = cell.character.value

            canvas.pixels[drawingY][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_TR) != 0)
            canvas.pixels[drawingY][drawingX] = ((charValue and SciiChar.BLOCK_BIT_TL) != 0)
            canvas.pixels[drawingY + 1][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_BR) != 0)
            canvas.pixels[drawingY + 1][drawingX] = ((charValue and SciiChar.BLOCK_BIT_BL) != 0)

            canvas.attrs[sciiY][sciiX] = BlockDrawingCell(color = cell.ink, bright = cell.bright)
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: MergeCell) =
            replaceSciiCell(sciiX, sciiY, cell.toSciiCell())
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
                    canvas.attrs[y][x] = bag.getStuff(BlockDrawingCell.Companion)
                }
            }

            return canvas
        }
    }
}
