package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.BagUnpackException

data class Crate<T : Cell>(
    val canvasType: CanvasType,
    val width: Int,
    val height: Int,
    val cells: List<List<T>>,
) {
    companion object : BagStuffPacker<Crate<*>>, BagStuffUnpacker<Crate<*>> {
        fun fromCanvasScii(
            canvas: Canvas<*>,
            sciiX: Int,
            sciiY: Int,
            sciiWidth: Int,
            sciiHeight: Int,
        ): Crate<SciiCell> {
            val cells = MutableList(sciiHeight) { MutableList(sciiWidth) { SciiCell.Transparent } }

            for (y in 0..<sciiHeight) {
                for (x in 0..<sciiWidth) {
                    cells[y][x] = canvas.getSciiCell(x + sciiX, y + sciiY)
                }
            }

            return Crate(CanvasType.Scii, sciiWidth, sciiHeight, cells)
        }

        fun <T : Cell> fromCanvasDrawing(
            canvas: Canvas<T>,
            drawingX: Int,
            drawingY: Int,
            drawingWidth: Int,
            drawingHeight: Int,
        ): Crate<T> {
            val transparentCell = Cell.makeTransparent(canvas.type.cellType)
            val cells = MutableList(drawingHeight) { MutableList(drawingWidth) { transparentCell } }

            for (y in 0..<drawingHeight) {
                for (x in 0..<drawingWidth) {
                    cells[y][x] = canvas.getDrawingCell(x + drawingX, y + drawingY)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return Crate(canvas.type, drawingWidth, drawingHeight, cells) as Crate<T>
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Cell> fromCanvasDrawing(canvas: Canvas<T>, box: Box): Crate<T> = fromCanvasDrawing(
            canvas = canvas,
            drawingX = box.x,
            drawingY = box.y,
            drawingWidth = box.width,
            drawingHeight = box.height,
        )

        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Crate<*>) {
            bag.put(value.canvasType.value)
            bag.put(value.width)
            bag.put(value.height)

            for (line in value.cells) {
                for (cell in line) {
                    bag.put(Cell, cell)
                }
            }
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Crate<*> {
            val cellType = try {
                CanvasType.of(bag.getInt())
            } catch (e: IllegalArgumentException) {
                throw BagUnpackException(e.toString())
            }

            val width = bag.getInt()
            val height = bag.getInt()
            val cells: MutableList<MutableList<Cell?>> = MutableList(height) { MutableList(width) { null } }

            for (y in 0..<height) {
                for (x in 0..<width) {
                    cells[y][x] = bag.getStuff(Cell)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return Crate(cellType, width, height, cells as List<List<Cell>>)
        }
    }
}
