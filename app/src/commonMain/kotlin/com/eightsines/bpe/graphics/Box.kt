package com.eightsines.bpe.graphics

data class Box(val x: Int, val y: Int, val width: Int, val height: Int) {
    val ex = x + width - 1
    val ey = y + height - 1

    fun copyWithOffset(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun copyWithOffset(point: Pair<Int, Int>) = copy(x = this.x + point.first, y = this.y + point.second)

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun of(sx: Int, sy: Int, ex: Int, ey: Int): Box {
            val boxSx = minOf(sx, ex)
            val boxSy = minOf(sy, ey)
            val boxEx = maxOf(sx, ex)
            val boxEy = maxOf(sy, ey)

            return Box(boxSx, boxSy, boxEx - boxSx + 1, boxEy - boxSy + 1)
        }
    }
}
