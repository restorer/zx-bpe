package com.eightsines.bpe.testing

import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.graphics.Painter

class TestPencil<T : Cell> : Painter.Pencil<T> {
    data class Point(val x: Int, val y: Int, val cell: Cell)

    val testPoints = mutableSetOf<Point>()

    override fun put(x: Int, y: Int, cell: T) {
        testPoints.add(Point(x, y, cell))
    }
}
