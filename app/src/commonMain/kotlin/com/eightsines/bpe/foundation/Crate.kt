package com.eightsines.bpe.foundation

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireNoIllegalArgumentException

data class Crate<T : Cell>(
    val canvasType: CanvasType,
    val width: Int,
    val height: Int,
    val cells: List<List<T>>,
) {
    override fun toString() = "Crate(canvasType=$canvasType, width=$width, height=$height)"

    fun copyTransformed(transformType: TransformType) = when (transformType) {
        TransformType.FlipHorizontal -> Crate(
            canvasType = canvasType,
            width = width,
            height = height,
            cells = cells.map { it.reversed() },
        )

        TransformType.FlipVertical -> Crate(
            canvasType = canvasType,
            width = width,
            height = height,
            cells = cells.reversed(),
        )

        TransformType.RotateCW -> {
            val last = height - 1

            Crate(
                canvasType = canvasType,
                width = height,
                height = width,
                cells = List(width) { y -> List(height) { x -> cells[last - x][y] } },
            )
        }

        TransformType.RotateCCW -> {
            val last = width - 1

            Crate(
                canvasType = canvasType,
                width = height,
                height = width,
                cells = List(width) { y -> List(height) { x -> cells[x][last - y] } },
            )
        }
    }

    companion object : BagStuffPacker<Crate<*>>, BagStuffUnpacker<Crate<*>> {
        fun fromCanvasScii(
            canvas: Canvas<*>,
            sciiX: Int,
            sciiY: Int,
            sciiWidth: Int,
            sciiHeight: Int,
        ): Crate<SciiCell> = Crate(
            CanvasType.Scii,
            sciiWidth,
            sciiHeight,
            List(sciiHeight) { y -> List(sciiWidth) { x -> canvas.getSciiCell(x + sciiX, y + sciiY) } },
        )

        fun <T : Cell> fromCanvasDrawing(
            canvas: Canvas<T>,
            drawingX: Int,
            drawingY: Int,
            drawingWidth: Int,
            drawingHeight: Int,
        ): Crate<T> = Crate(
            canvas.type,
            drawingWidth,
            drawingHeight,
            List(drawingHeight) { y -> List(drawingWidth) { x -> canvas.getDrawingCell(x + drawingX, y + drawingY) } },
        )

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
            value.cells.forEach { line -> line.forEach { bag.put(Cell, it) } }
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Crate<*> {
            val canvasType = requireNoIllegalArgumentException { CanvasType.of(bag.getInt()) }
            val width = bag.getInt()
            val height = bag.getInt()
            val cells = (0..<height).map { (0..<width).map { bag.getStuff(Cell) } }

            return Crate(canvasType, width, height, cells)
        }
    }
}
