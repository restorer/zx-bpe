package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireNoIllegalArgumentException
import com.eightsines.bpe.bag.requireSupportedStuffVersion

@BagStuff(unpacker = "Crate")
data class Crate<T : Cell>(
    @BagStuffWare(1) val canvasType: CanvasType,
    @BagStuffWare(2) val width: Int,
    @BagStuffWare(3) val height: Int,
    @BagStuffWare(4, packer = "putCellsInTheBag", unpacker = "getCellsOutOfTheBag") val cells: List<List<T>>,
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

        TransformType.RotateCw -> {
            val last = height - 1

            Crate(
                canvasType = canvasType,
                width = height,
                height = width,
                cells = List(width) { y -> List(height) { x -> cells[last - x][y] } },
            )
        }

        TransformType.RotateCcw -> {
            val last = width - 1

            Crate(
                canvasType = canvasType,
                width = height,
                height = width,
                cells = List(width) { y -> List(height) { x -> cells[x][last - y] } },
            )
        }
    }

    companion object : BagStuffUnpacker<Crate<*>> {
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
            drawingX = box.lx,
            drawingY = box.ly,
            drawingWidth = box.width,
            drawingHeight = box.height,
        )

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun putCellsInTheBag(bag: PackableBag, cells: List<List<Cell>>) {
            cells.forEach { line -> line.forEach { bag.put(Cell_Stuff, it) } }
        }

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun getCellsOutOfTheBag(bag: UnpackableBag, width: Int, height: Int): List<List<Cell>> =
            (0..<height).map { (0..<width).map { bag.getStuff(Cell_Stuff) } }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Crate<*> {
            requireSupportedStuffVersion("Crate", 1, version)

            var canvasType = requireNoIllegalArgumentException { CanvasType.of(bag.getInt()) }
            val width = bag.getInt()
            val height = bag.getInt()

            val cells = getCellsOutOfTheBag(
                bag = bag,
                width = width,
                height = height,
            )

            if (canvasType == CanvasType.Scii && cells.firstOrNull()?.firstOrNull()?.type == CellType.Block) {
                println(">>> FIX")
                canvasType = CanvasType.QBlock
            }

            return Crate(
                canvasType = canvasType,
                width = width,
                height = height,
                cells = cells,
            )
        }
    }
}
