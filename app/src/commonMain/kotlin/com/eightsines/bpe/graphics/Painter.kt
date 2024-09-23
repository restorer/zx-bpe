package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.Cell
import kotlin.math.abs
import kotlin.math.roundToInt

class Painter {
    fun interface Pencil<T : Cell> {
        fun put(x: Int, y: Int, cell: T)
    }

    fun getBBox(shape: Shape<*>) = when (shape) {
        is Shape.Points -> if (shape.points.isEmpty()) {
            Box(0, 0, 0, 0)
        } else {
            var sx = 0
            var sy = 0
            var ex = 0
            var ey = 0

            shape.points.forEachIndexed { index, point ->
                if (index == 0) {
                    sx = point.first
                    sy = point.second
                    ex = sx
                    ey = sy
                } else {
                    sx = minOf(sx, point.first)
                    sy = minOf(sy, point.second)
                    ex = maxOf(ex, point.first)
                    ey = maxOf(ey, point.second)
                }
            }

            Box.of(sx, sy, ex, ey)
        }

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
            is Shape.Points -> for (point in shape.points) {
                pencil.put(point.first, point.second, shape.cell)
            }

            is Shape.Line -> {
                val sx = shape.sx
                val ex = shape.ex
                val sy = shape.sy
                val ey = shape.ey

                val dx = ex - sx
                val dy = ey - sy

                val absDx = abs(dx)
                val absDy = abs(dy)

                if (absDx > absDy) {
                    val mx = if (dx < 0) -1 else 1
                    val my = if (dx == 0) 0.0 else (ey - sy).toDouble() / absDx.toDouble()

                    for (i in 0..absDx) {
                        pencil.put(sx + i * mx, sy + (i * my).roundToInt(), shape.cell)
                    }
                } else {
                    val mx = if (dy == 0) 0.0 else (ex - sx).toDouble() / absDy.toDouble()
                    val my = if (dy < 0) -1 else 1

                    for (i in 0..absDy) {
                        pencil.put(sx + (i * mx).roundToInt(), sy + i * my, shape.cell)
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
