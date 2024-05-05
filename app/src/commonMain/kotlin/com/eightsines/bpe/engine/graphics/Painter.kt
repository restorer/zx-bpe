package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.engine.cell.Cell
import kotlin.math.roundToInt

class Painter {
    fun getBBox(shape: Shape<*>) = when (shape) {
        is Shape.Point -> Box(shape.x, shape.y, 1, 1)

        is Shape.Line -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = minOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.FillBox -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = minOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.StrokeBox -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = minOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.Cells -> Box(shape.x, shape.y, shape.crate.width, shape.crate.height)
    }

    fun <T : Cell> paint(shape: Shape<T>, block: (x: Int, y: Int, cell: T) -> Unit) {
        when (shape) {
            is Shape.Point -> block(shape.x, shape.y, shape.cell)

            is Shape.Line -> {
                val sx = minOf(shape.sx, shape.ex)
                val ex = maxOf(shape.sx, shape.ex)
                val sy = minOf(shape.sy, shape.ey)
                val ey = minOf(shape.sy, shape.ey)

                val dx = ex - sx
                val dy = ey - sy

                if (dx > dy) {
                    val my = if (dx == 0) 0.0 else dy.toDouble() / dx.toDouble()

                    for (x in 0..dx) {
                        block(sx + x, sy + (x * my).roundToInt(), shape.cell)
                    }
                } else {
                    val mx = if (dy == 0) 0.0 else dx.toDouble() / dy.toDouble()

                    for (y in 0..dy) {
                        block(sx + (y * mx).roundToInt(), sy + y, shape.cell)
                    }
                }
            }

            is Shape.FillBox -> {
                val sx = minOf(shape.sx, shape.ex)
                val ex = maxOf(shape.sx, shape.ex)
                val sy = minOf(shape.sy, shape.ey)
                val ey = minOf(shape.sy, shape.ey)

                for (x in sx..ex) {
                    for (y in sy..ey) {
                        block(x, y, shape.cell)
                    }
                }
            }

            is Shape.StrokeBox -> {
                val sx = minOf(shape.sx, shape.ex)
                val ex = maxOf(shape.sx, shape.ex)
                var sy = minOf(shape.sy, shape.ey)
                var ey = minOf(shape.sy, shape.ey)

                for (x in sx..ex) {
                    block(x, sy, shape.cell)
                }

                if (sy != ey) {
                    for (x in sx..ex) {
                        block(x, ey, shape.cell)
                    }
                }

                ++sy
                --ey

                if (sy < ey) {
                    for (y in sy..ey) {
                        block(sx, y, shape.cell)
                    }

                    if (sx != ex) {
                        for (y in sy..ey) {
                            block(ex, y, shape.cell)
                        }
                    }
                }
            }

            is Shape.Cells -> {
                val sx = shape.x
                val sy = shape.y
                val crate = shape.crate

                for (x in 0..<crate.width) {
                    for (y in 0..<crate.height) {
                        block(sx + x, sy + y, crate.cells[y][x])
                    }
                }
            }
        }
    }
}
