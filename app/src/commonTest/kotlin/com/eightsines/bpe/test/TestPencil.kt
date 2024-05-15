package com.eightsines.bpe.test

import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.model.Cell

class TestPencil<T : Cell> : Painter.Pencil<T> {
    data class Point(val x: Int, val y: Int, val cell: Cell)

    val testPoints = mutableListOf<Point>()

    override fun put(x: Int, y: Int, cell: T) {
        testPoints.add(Point(x, y, cell))
    }
}
