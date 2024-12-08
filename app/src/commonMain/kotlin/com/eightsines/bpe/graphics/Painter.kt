package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Painter {
    fun interface Pencil<T : Cell> {
        fun put(x: Int, y: Int, cell: T)
    }

    fun getBBox(shape: Shape<*>): Box = when (shape) {
        is Shape.Points -> getBBoxPoints(shape)
        is Shape.Line -> getBBoxBoxLike(shape)
        is Shape.FillBox -> getBBoxBoxLike(shape)
        is Shape.StrokeBox -> getBBoxBoxLike(shape)
        is Shape.FillEllipse -> getBBoxBoxLike(shape)
        is Shape.StrokeEllipse -> getBBoxBoxLike(shape)
        is Shape.Cells -> Box(shape.x, shape.y, shape.crate.width, shape.crate.height)
    }

    fun <T : Cell> paint(shape: Shape<T>, pencil: Pencil<T>) {
        when (shape) {
            is Shape.Points -> paintPoints(shape, pencil)
            is Shape.Line -> paintLine(shape, pencil)
            is Shape.FillBox -> paintFillBox(shape, pencil)
            is Shape.StrokeBox -> paintStrokeBox(shape, pencil)
            is Shape.FillEllipse -> paintFillEllipse(shape, pencil)
            is Shape.StrokeEllipse -> paintStrokeEllipse(shape, pencil)
            is Shape.Cells -> paintCells(shape, pencil)
        }
    }

    private fun getBBoxPoints(shape: Shape.Points<*>) = if (shape.points.isEmpty()) {
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

    private fun getBBoxBoxLike(shape: BoxLikeShape): Box {
        val sx = minOf(shape.sx, shape.ex)
        val ex = maxOf(shape.sx, shape.ex)
        val sy = minOf(shape.sy, shape.ey)
        val ey = maxOf(shape.sy, shape.ey)

        return Box(sx, sy, ex - sx + 1, ey - sy + 1)
    }

    private fun <T : Cell> paintPoints(shape: Shape.Points<T>, pencil: Pencil<T>) {
        for (point in shape.points) {
            pencil.put(point.first, point.second, shape.cell)
        }
    }

    private fun <T : Cell> paintLine(shape: Shape.Line<T>, pencil: Pencil<T>) {
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

    private fun <T : Cell> paintFillBox(shape: Shape.FillBox<T>, pencil: Pencil<T>) {
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

    private fun <T : Cell> paintStrokeBox(shape: Shape.StrokeBox<T>, pencil: Pencil<T>) {
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

        if (sy <= ey) {
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

    private fun <T : Cell> paintFillEllipse(shape: Shape.FillEllipse<T>, pencil: Pencil<T>) {
        val sx = minOf(shape.sx, shape.ex)
        val ex = maxOf(shape.sx, shape.ex)
        val sy = minOf(shape.sy, shape.ey)
        val ey = maxOf(shape.sy, shape.ey)

        val midX = (sx + ex).toDouble() * 0.5
        val midY = (sy + ey).toDouble() * 0.5

        val radX = (ex - sx + 1).toDouble() * 0.5 - 0.125
        val radY = (ey - sy + 1).toDouble() * 0.5 - 0.125

        val sqRadX = radX * radX
        val sqRadY = radY * radY
        val compare = sqRadX * sqRadY

        for (y in sy..ey) {
            val oy = y.toDouble() - midY
            val part = oy * oy * sqRadX

            for (x in sx..ex) {
                val ox = x.toDouble() - midX

                if (ox * ox * sqRadY + part <= compare) {
                    pencil.put(x, y, shape.cell)
                }
            }
        }
    }

    private fun <T : Cell> paintStrokeEllipse(shape: Shape.StrokeEllipse<T>, pencil: Pencil<T>) {
        val sx = minOf(shape.sx, shape.ex)
        val ex = maxOf(shape.sx, shape.ex)
        val sy = minOf(shape.sy, shape.ey)
        val ey = maxOf(shape.sy, shape.ey)

        val midX = (sx + ex).toDouble() * 0.5
        val midY = (sy + ey).toDouble() * 0.5

        val radX = (ex - sx + 1).toDouble() * 0.5 - 0.125
        val radY = (ey - sy + 1).toDouble() * 0.5 - 0.125

        val sqRadX = radX * radX
        val sqRadY = radY * radY
        val compare = sqRadX * sqRadY

        val alreadyDrawn = mutableSetOf<Pair<Int, Int>>()

        for (y in sy..ey) {
            val oy = y.toDouble() - midY
            val ox = sqrt((compare - oy * oy * sqRadX) / sqRadY)

            val x1 = (midX + ox).toInt()
            val x2 = ex - x1 + sx

            pencil.put(x1, y, shape.cell)
            alreadyDrawn.add(x1 to y)

            if (x1 != x2) {
                pencil.put(x2, y, shape.cell)
                alreadyDrawn.add(x2 to y)
            }
        }

        // Lame, but OK for now
        for (x in sx..ex) {
            val ox = x.toDouble() - midX
            val oy = sqrt((compare - ox * ox * sqRadY) / sqRadX)

            val y1 = (midY + oy).toInt()
            val y2 = ey - y1 + sy

            if (!alreadyDrawn.contains(x to y1)) {
                pencil.put(x, y1, shape.cell)

                if (y1 != y2 && !alreadyDrawn.contains(x to y2)) {
                    pencil.put(x, y2, shape.cell)
                }
            }
        }
    }

    private fun <T : Cell> paintCells(shape: Shape.Cells<T>, pencil: Pencil<T>) {
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
