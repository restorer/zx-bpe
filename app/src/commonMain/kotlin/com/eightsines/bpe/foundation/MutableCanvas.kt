package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.HBlockMergeCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.VBlockMergeCell
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

interface MutableCanvas<T : Cell> : Canvas<T> {
    fun mutate(block: (mutator: CanvasMutator<T>) -> Unit)

    companion object : BagStuffUnpacker<MutableCanvas<*>> {
        fun create(type: CanvasType, sciiWidth: Int, sciiHeight: Int, sourceMutations: Int? = null) = when (type) {
            CanvasType.Scii -> MutableSciiCanvas(sciiWidth = sciiWidth, sciiHeight = sciiHeight, sourceMutations = sourceMutations)
            CanvasType.HBlock -> MutableHBlockCanvas(sciiWidth = sciiWidth, sciiHeight = sciiHeight, sourceMutations = sourceMutations)
            CanvasType.VBlock -> MutableVBlockCanvas(sciiWidth = sciiWidth, sciiHeight = sciiHeight, sourceMutations = sourceMutations)
            CanvasType.QBlock -> MutableQBlockCanvas(sciiWidth = sciiWidth, sciiHeight = sciiHeight, sourceMutations = sourceMutations)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableCanvas<*> {
            requireSupportedStuffVersion("MutableCanvas", 1, version)

            val type = bag.getInt()
            val sciiWidth = bag.getInt()
            val sciiHeight = bag.getInt()

            return when (type) {
                CanvasType.Scii.value -> bag.getStuff(MutableSciiCanvas.PolymorphicUnpacker(sciiWidth, sciiHeight))
                CanvasType.HBlock.value -> bag.getStuff(MutableHBlockCanvas.PolymorphicUnpacker(sciiWidth, sciiHeight))
                CanvasType.VBlock.value -> bag.getStuff(MutableVBlockCanvas.PolymorphicUnpacker(sciiWidth, sciiHeight))
                CanvasType.QBlock.value -> bag.getStuff(MutableQBlockCanvas.PolymorphicUnpacker(sciiWidth, sciiHeight))
                else -> throw UnknownPolymorphicTypeBagUnpackException("MutableCanvas", type)
            }
        }
    }
}

interface CanvasMutator<T : Cell> {
    fun clear()
    fun mergeDrawingCell(drawingX: Int, drawingY: Int, cell: T)
    fun replaceDrawingCell(drawingX: Int, drawingY: Int, cell: T)
    fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell)
}

interface MutableBlockCanvas : MutableCanvas<BlockCell>

interface HBlockCanvasMutator : CanvasMutator<BlockCell> {
    fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: HBlockMergeCell)
}

interface VBlockCanvasMutator : CanvasMutator<BlockCell> {
    fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: VBlockMergeCell)
}

class MutableSciiCanvas(
    sciiWidth: Int,
    sciiHeight: Int,
    source: MutableSciiCanvas? = null,
    sourceMutations: Int? = null,
) : SciiCanvas(sciiWidth, sciiHeight), MutableCanvas<SciiCell> {
    override val cells: MutableList<MutableList<SciiCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { SciiCell.Transparent } }

    override var mutations: Int = sourceMutations ?: source?.mutations ?: 0
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

    override fun toString() = "SciiCanvas(drawingWidth=$drawingWidth, drawingHeight=$drawingHeight, mutations=$mutations)"

    private class Mutator(private val canvas: MutableSciiCanvas) : CanvasMutator<SciiCell> {
        override fun clear() {
            canvas.cells.forEach { it.fill(SciiCell.Transparent) }
        }

        override fun mergeDrawingCell(drawingX: Int, drawingY: Int, cell: SciiCell) =
            modifyDrawingCell(drawingX, drawingY) { cell.merge(canvas.cells[drawingY][drawingX]) }

        override fun replaceDrawingCell(drawingX: Int, drawingY: Int, cell: SciiCell) =
            modifyDrawingCell(drawingX, drawingY) { cell }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            canvas.cells[sciiY][sciiX] = cell
        }

        private inline fun modifyDrawingCell(drawingX: Int, drawingY: Int, cellProvider: () -> SciiCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX >= canvas.drawingWidth || drawingY >= canvas.drawingHeight) {
                return
            }

            canvas.cells[drawingY][drawingX] = cellProvider()
        }
    }

    internal class PolymorphicUnpacker(
        private val sciiWidth: Int,
        private val sciiHeight: Int,
    ) : BagStuffUnpacker<MutableSciiCanvas> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableSciiCanvas {
            requireSupportedStuffVersion("MutableSciiCanvas", 1, version)
            val canvas = MutableSciiCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = bag.getStuff(SciiCell)
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
    sourceMutations: Int? = null,
) : HBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { BlockCell.Transparent } }

    override var mutations: Int = sourceMutations ?: source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableHBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockCell>) -> Unit) = mutateHBlock(block)

    fun mutateHBlock(block: (mutator: HBlockCanvasMutator) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    override fun toString() = "HBlockCanvas(drawingWidth=$drawingWidth, drawingHeight=$drawingHeight, mutations=$mutations)"

    class Mutator(private val canvas: MutableHBlockCanvas) : HBlockCanvasMutator {
        override fun clear() {
            canvas.cells.forEach { it.fill(BlockCell.Transparent) }
        }

        override fun mergeDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY) { cell.merge(canvas.cells[drawingY][drawingX]) }

        override fun replaceDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY) { cell }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            var ink = cell.ink
            var paper = cell.paper

            var charValue = if (cell.character.value in SciiChar.BLOCK_VALUE_FIRST..SciiChar.BLOCK_VALUE_LAST) {
                cell.character.value
            } else {
                SciiChar.BLOCK_VALUE_FIRST
            }

            if (ink == SciiColor.Transparent && paper != SciiColor.Transparent) {
                ink = paper
                paper = SciiColor.Transparent
                charValue = charValue xor SciiChar.BLOCK_MASK
            }

            val topColor = if ((charValue and SciiChar.BLOCK_BIT_TR) != 0 || (charValue and SciiChar.BLOCK_BIT_TL) != 0) {
                ink
            } else {
                paper
            }

            val bottomColor = if ((charValue and SciiChar.BLOCK_BIT_BR) != 0 || (charValue and SciiChar.BLOCK_BIT_BL) != 0) {
                ink
            } else {
                paper
            }

            val drawingY = sciiY * 2

            if (topColor == SciiColor.Transparent && bottomColor == SciiColor.Transparent) {
                canvas.cells[drawingY][sciiX] = BlockCell.Transparent
                canvas.cells[drawingY + 1][sciiX] = BlockCell.Transparent
            } else {
                canvas.cells[drawingY][sciiX] = BlockCell(topColor, cell.bright)
                canvas.cells[drawingY + 1][sciiX] = BlockCell(bottomColor, cell.bright)
            }
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: HBlockMergeCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            val drawingY = sciiY * 2

            if (cell.topColor == SciiColor.Transparent && cell.bottomColor == SciiColor.Transparent) {
                canvas.cells[drawingY][sciiX] = BlockCell.Transparent
                canvas.cells[drawingY + 1][sciiX] = BlockCell.Transparent
            } else {
                canvas.cells[drawingY][sciiX] = BlockCell(color = cell.topColor, bright = cell.bright)
                canvas.cells[drawingY + 1][sciiX] = BlockCell(color = cell.bottomColor, bright = cell.bright)
            }
        }

        private inline fun modifyDrawingCell(drawingX: Int, drawingY: Int, cellProvider: () -> BlockCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX >= canvas.drawingWidth || drawingY >= canvas.drawingHeight) {
                return
            }

            val cell = cellProvider()
            val otherDrawingY = if (drawingY % 2 == 0) drawingY + 1 else drawingY - 1
            val otherCell = canvas.cells[otherDrawingY][drawingX]

            if (cell.color == SciiColor.Transparent && otherCell.color == SciiColor.Transparent) {
                canvas.cells[drawingY][drawingX] = BlockCell.Transparent
                canvas.cells[otherDrawingY][drawingX] = BlockCell.Transparent
            } else {
                val bright = cell.bright.merge(otherCell.bright)

                canvas.cells[drawingY][drawingX] = cell.copy(bright = bright)
                canvas.cells[otherDrawingY][drawingX] = otherCell.copy(bright = bright)
            }
        }
    }

    internal class PolymorphicUnpacker(
        private val sciiWidth: Int,
        private val sciiHeight: Int,
    ) : BagStuffUnpacker<MutableHBlockCanvas> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableHBlockCanvas {
            requireSupportedStuffVersion("MutableHBlockCanvas", 1, version)
            val canvas = MutableHBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = bag.getStuff(BlockCell)
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
    sourceMutations: Int? = null,
) : VBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val cells: MutableList<MutableList<BlockCell>> =
        source?.cells?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { BlockCell.Transparent } }

    override var mutations: Int = sourceMutations ?: source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableVBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockCell>) -> Unit) = mutateVBlock(block)

    fun mutateVBlock(block: (mutator: VBlockCanvasMutator) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    override fun toString() = "VBlockCanvas(drawingWidth=$drawingWidth, drawingHeight=$drawingHeight, mutations=$mutations)"

    private class Mutator(private val canvas: MutableVBlockCanvas) : VBlockCanvasMutator {
        override fun clear() {
            canvas.cells.forEach { it.fill(BlockCell.Transparent) }
        }

        override fun mergeDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY) { cell.merge(canvas.cells[drawingY][drawingX]) }

        override fun replaceDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY) { cell }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            var ink = cell.ink
            var paper = cell.paper

            var charValue = if (cell.character.value in SciiChar.BLOCK_VALUE_FIRST..SciiChar.BLOCK_VALUE_LAST) {
                cell.character.value
            } else {
                SciiChar.BLOCK_VALUE_FIRST
            }

            if (ink == SciiColor.Transparent && paper != SciiColor.Transparent) {
                ink = paper
                paper = SciiColor.Transparent
                charValue = charValue xor SciiChar.BLOCK_MASK
            }

            val leftColor = if ((charValue and SciiChar.BLOCK_BIT_TL) != 0 || (charValue and SciiChar.BLOCK_BIT_BL) != 0) {
                ink
            } else {
                paper
            }

            val rightColor = if ((charValue and SciiChar.BLOCK_BIT_TR) != 0 || (charValue and SciiChar.BLOCK_BIT_BR) != 0) {
                ink
            } else {
                paper
            }

            val drawingX = sciiX * 2

            if (leftColor == SciiColor.Transparent && rightColor == SciiColor.Transparent) {
                canvas.cells[sciiY][drawingX] = BlockCell.Transparent
                canvas.cells[sciiY][drawingX + 1] = BlockCell.Transparent
            } else {
                canvas.cells[sciiY][drawingX] = BlockCell(leftColor, cell.bright)
                canvas.cells[sciiY][drawingX + 1] = BlockCell(rightColor, cell.bright)
            }
        }

        override fun replaceMergeCell(sciiX: Int, sciiY: Int, cell: VBlockMergeCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            val drawingX = sciiX * 2

            if (cell.leftColor == SciiColor.Transparent && cell.rightColor == SciiColor.Transparent) {
                canvas.cells[sciiY][drawingX] = BlockCell.Transparent
                canvas.cells[sciiY][drawingX + 1] = BlockCell.Transparent
            } else {
                canvas.cells[sciiY][drawingX] = BlockCell(color = cell.leftColor, bright = cell.bright)
                canvas.cells[sciiY][drawingX + 1] = BlockCell(color = cell.rightColor, bright = cell.bright)
            }
        }

        private inline fun modifyDrawingCell(drawingX: Int, drawingY: Int, cellProvider: () -> BlockCell) {
            if (drawingX < 0 || drawingY < 0 || drawingX >= canvas.drawingWidth || drawingY >= canvas.drawingHeight) {
                return
            }

            val cell = cellProvider()
            val otherDrawingX = if (drawingX % 2 == 0) drawingX + 1 else drawingX - 1
            val otherCell = canvas.cells[drawingY][otherDrawingX]

            if (cell.color == SciiColor.Transparent && otherCell.color == SciiColor.Transparent) {
                canvas.cells[drawingY][drawingX] = BlockCell.Transparent
                canvas.cells[drawingY][otherDrawingX] = BlockCell.Transparent
            } else {
                val bright = cell.bright.merge(otherCell.bright)

                canvas.cells[drawingY][drawingX] = cell.copy(bright = bright)
                canvas.cells[drawingY][otherDrawingX] = otherCell.copy(bright = bright)
            }
        }
    }

    internal class PolymorphicUnpacker(
        private val sciiWidth: Int,
        private val sciiHeight: Int,
    ) : BagStuffUnpacker<MutableVBlockCanvas> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableVBlockCanvas {
            requireSupportedStuffVersion("MutableVBlockCanvas", 1, version)
            val canvas = MutableVBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.cells[y][x] = bag.getStuff(BlockCell)
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
    sourceMutations: Int? = null,
) : QBlockCanvas(sciiWidth, sciiHeight), MutableBlockCanvas {
    override val pixels: MutableList<MutableList<Boolean>> =
        source?.pixels?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(drawingHeight) { MutableList(drawingWidth) { false } }

    override val attrs: MutableList<MutableList<BlockCell>> =
        source?.attrs?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(sciiHeight) { MutableList(sciiWidth) { BlockCell.Transparent } }

    override var mutations: Int = sourceMutations ?: source?.mutations ?: 0
        private set

    override fun copyMutable() = MutableQBlockCanvas(
        sciiWidth = sciiWidth,
        sciiHeight = sciiHeight,
        source = this,
    )

    override fun mutate(block: (mutator: CanvasMutator<BlockCell>) -> Unit) {
        ++mutations
        block(Mutator(this))
    }

    override fun toString() = "QBlockCanvas(drawingWidth=$drawingWidth, drawingHeight=$drawingHeight, mutations=$mutations)"

    private class Mutator(private val canvas: MutableQBlockCanvas) : CanvasMutator<BlockCell> {
        override fun clear() {
            canvas.pixels.forEach { it.fill(false) }
            canvas.attrs.forEach { it.fill(BlockCell.Transparent) }
        }

        override fun mergeDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY, cell) {
                canvas.pixels[drawingY][drawingX] || cell.color != SciiColor.Transparent
            }

        override fun replaceDrawingCell(drawingX: Int, drawingY: Int, cell: BlockCell) =
            modifyDrawingCell(drawingX, drawingY, cell) { cell.color != SciiColor.Transparent }

        override fun replaceSciiCell(sciiX: Int, sciiY: Int, cell: SciiCell) {
            if (sciiX < 0 || sciiY < 0 || sciiX >= canvas.sciiWidth || sciiY >= canvas.sciiHeight) {
                return
            }

            val drawingX = sciiX * 2
            val drawingY = sciiY * 2

            var charValue = if (cell.character.value in SciiChar.BLOCK_VALUE_FIRST..SciiChar.BLOCK_VALUE_LAST) {
                cell.character.value
            } else {
                SciiChar.BLOCK_VALUE_FIRST
            }

            val color = when {
                cell.ink == SciiColor.Transparent && cell.paper == SciiColor.Transparent -> {
                    charValue = SciiChar.BLOCK_VALUE_FIRST
                    SciiColor.Transparent
                }

                cell.ink == SciiColor.Transparent -> {
                    charValue = charValue xor SciiChar.BLOCK_MASK
                    cell.paper
                }

                cell.paper == SciiColor.Transparent -> cell.ink

                else -> {
                    charValue = SciiChar.BLOCK_VALUE_LAST
                    cell.ink
                }
            }

            canvas.pixels[drawingY][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_TR) != 0)
            canvas.pixels[drawingY][drawingX] = ((charValue and SciiChar.BLOCK_BIT_TL) != 0)
            canvas.pixels[drawingY + 1][drawingX + 1] = ((charValue and SciiChar.BLOCK_BIT_BR) != 0)
            canvas.pixels[drawingY + 1][drawingX] = ((charValue and SciiChar.BLOCK_BIT_BL) != 0)

            if (charValue and SciiChar.BLOCK_MASK == 0) {
                canvas.attrs[sciiY][sciiX] = BlockCell.Transparent
            } else {
                canvas.attrs[sciiY][sciiX] = BlockCell(color = color, bright = cell.bright)
            }
        }

        private inline fun modifyDrawingCell(
            drawingX: Int,
            drawingY: Int,
            cell: BlockCell,
            pixelProvider: () -> Boolean,
        ) {
            if (drawingX < 0 || drawingY < 0 || drawingX >= canvas.drawingWidth || drawingY >= canvas.drawingHeight) {
                return
            }

            canvas.pixels[drawingY][drawingX] = pixelProvider()

            val sciiX = drawingX / 2
            val sciiY = drawingY / 2

            val baseDrawingX = sciiX * 2
            val baseDrawingY = sciiY * 2

            val hasAnyPixel = canvas.pixels[baseDrawingY][baseDrawingX + 1] ||
                    canvas.pixels[baseDrawingY][baseDrawingX] ||
                    canvas.pixels[baseDrawingY + 1][baseDrawingX + 1] ||
                    canvas.pixels[baseDrawingY + 1][baseDrawingX]

            if (hasAnyPixel) {
                canvas.attrs[sciiY][sciiX] = cell.merge(canvas.attrs[sciiY][sciiX])
            } else {
                canvas.attrs[sciiY][sciiX] = BlockCell.Transparent
            }
        }
    }

    internal class PolymorphicUnpacker(
        private val sciiWidth: Int,
        private val sciiHeight: Int,
    ) : BagStuffUnpacker<MutableQBlockCanvas> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutableQBlockCanvas {
            requireSupportedStuffVersion("MutableQBlockCanvas", 1, version)
            val canvas = MutableQBlockCanvas(sciiWidth, sciiHeight)

            for (y in 0..<canvas.drawingHeight) {
                for (x in 0..<canvas.drawingWidth) {
                    canvas.pixels[y][x] = bag.getBoolean()
                }
            }

            for (y in 0..<sciiHeight) {
                for (x in 0..<sciiWidth) {
                    canvas.attrs[y][x] = bag.getStuff(BlockCell)
                }
            }

            return canvas
        }
    }
}
