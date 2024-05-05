package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.canvas.Canvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.CellType
import com.eightsines.bpe.engine.cell.SciiCell

data class Crate<T : Cell>(
    val width: Int,
    val height: Int,
    val cellType: CellType,
    val cells: List<List<T>>,
) : BagStuff {
    override val bagStuffVersion = 1

    override fun putInTheBag(bag: PackableBag) {
        bag.put(width)
        bag.put(height)
        bag.put(cellType.value)

        for (line in cells) {
            for (cell in line) {
                bag.put(cell)
            }
        }
    }

    companion object : BagStuff.Unpacker<Crate<*>> {
        fun makeScii(
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

            return Crate(sciiWidth, sciiHeight, CellType.Scii, cells)
        }

        fun <T : Cell> makeDrawing(
            canvas: Canvas<T>,
            drawingX: Int,
            drawingY: Int,
            drawingWidth: Int,
            drawingHeight: Int,
        ): Crate<T> {
            val transparentCell = Cell.makeTransparent(canvas.cellType)
            val cells = MutableList(drawingHeight) { MutableList(drawingWidth) { transparentCell } }

            for (y in 0..<drawingHeight) {
                for (x in 0..<drawingWidth) {
                    cells[y][x] = canvas.getDrawingCell(x + drawingX, y + drawingY)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return Crate(drawingWidth, drawingHeight, canvas.cellType, cells) as Crate<T>
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Crate<*> {
            val width = bag.getInt()
            val height = bag.getInt()
            val cellType = CellType.unpack(bag.getString())
            val cells: MutableList<MutableList<Cell?>> = MutableList(width) { MutableList(height) { null } }

            for (y in 0..<height) {
                for (x in 0..<width) {
                    cells[y][x] = bag.getStuff(Cell.Companion)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return Crate(width, height, cellType, cells as List<List<Cell>>)
        }
    }
}
