package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.Cell
import kotlin.math.roundToInt

class Painter {
    fun interface Pencil<T : Cell> {
        fun put(x: Int, y: Int, cell: T)
    }

    fun getBBox(shape: Shape<*>) = when (shape) {
        is Shape.Point -> Box(shape.x, shape.y, 1, 1)

        is Shape.Line -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = maxOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.FillBox -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = maxOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.StrokeBox -> {
            val sx = minOf(shape.sx, shape.ex)
            val ex = maxOf(shape.sx, shape.ex)
            val sy = minOf(shape.sy, shape.ey)
            val ey = maxOf(shape.sy, shape.ey)

            Box(sx, sy, ex - sx + 1, ey - sy + 1)
        }

        is Shape.Cells -> Box(shape.x, shape.y, shape.crate.width, shape.crate.height)
    }

    fun <T : Cell> paint(shape: Shape<T>, pencil: Pencil<T>) {
        when (shape) {
            is Shape.Point -> pencil.put(shape.x, shape.y, shape.cell)

            is Shape.Line -> {
                val sx = shape.sx
                val ex = shape.ex
                val sy = shape.sy
                val ey = shape.ey

                val absSx = minOf(sx, ex)
                val absEx = maxOf(sx, ex)
                val absSy = minOf(sy, ey)
                val absEy = maxOf(sy, ey)

                val absDx = absEx - absSx
                val absDy = absEy - absSy

                if (absDx > absDy) {
                    val my = if (absDx == 0) 0.0 else (ey - sy).toDouble() / absDx.toDouble()

                    for (x in 0..absDx) {
                        pencil.put(absSx + x, sy + (x * my).roundToInt(), shape.cell)
                    }
                } else {
                    val mx = if (absDy == 0) 0.0 else (ex - sx).toDouble() / absDy.toDouble()

                    for (y in 0..absDy) {
                        pencil.put(sx + (y * mx).roundToInt(), absSy + y, shape.cell)
                    }
                }
            }

            is Shape.FillBox -> {
                val sx = minOf(shape.sx, shape.ex)
                val ex = maxOf(shape.sx, shape.ex)
                val sy = minOf(shape.sy, shape.ey)
                val ey = maxOf(shape.sy, shape.ey)

                for (y in sy..ey) {
                    for (x in sx..ex) {
                        pencil.put(x, y, shape.cell)
                    }
                }
            }

            is Shape.StrokeBox -> {
                val sx = minOf(shape.sx, shape.ex)
                val ex = maxOf(shape.sx, shape.ex)
                var sy = minOf(shape.sy, shape.ey)
                var ey = maxOf(shape.sy, shape.ey)

                for (x in sx..ex) {
                    pencil.put(x, sy, shape.cell)
                }

                if (sy != ey) {
                    for (x in sx..ex) {
                        pencil.put(x, ey, shape.cell)
                    }
                }

                ++sy
                --ey

                if (sy < ey) {
                    for (y in sy..ey) {
                        pencil.put(sx, y, shape.cell)
                    }

                    if (sx != ex) {
                        for (y in sy..ey) {
                            pencil.put(ex, y, shape.cell)
                        }
                    }
                }
            }

            is Shape.Cells -> {
                val sx = shape.x
                val sy = shape.y
                val crate = shape.crate

                for (y in 0..<crate.height) {
                    for (x in 0..<crate.width) {
                        pencil.put(sx + x, sy + y, crate.cells[y][x])
                    }
                }
            }
        }
    }
}
