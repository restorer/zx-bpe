package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare

@BagStuff
class Box internal constructor(
    @BagStuffWare(index = 1) val lx: Int,
    @BagStuffWare(index = 2) val ly: Int,
    @BagStuffWare(index = 3) val width: Int,
    @BagStuffWare(index = 4) val height: Int,
) {
    val rx = lx + width - 1
    val ry = ly + height - 1

    fun copyWithOffset(x: Int, y: Int) = Box(lx + x, ly + y, width, height)
    fun copyWithOffset(point: Pair<Int, Int>) = Box(lx + point.first, ly + point.second, width, height)
    fun contains(x: Int, y: Int) = x in lx..rx && y in ly..ry

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as Box

        return lx == other.lx &&
                ly == other.ly &&
                width == other.width &&
                height == other.height
    }

    override fun hashCode(): Int {
        var result = lx
        result = 31 * result + ly
        result = 31 * result + width
        result = 31 * result + height
        return result
    }

    companion object {
        fun ofSize(x: Int, y: Int, width: Int, height: Int) = Box(x, y, width, height)

        fun ofCoords(sx: Int, sy: Int, ex: Int, ey: Int): Box {
            val lx = minOf(sx, ex)
            val ly = minOf(sy, ey)
            val width = maxOf(sx, ex) - lx + 1
            val height = maxOf(sy, ey) - ly + 1

            return Box(lx, ly, width, height)
        }
    }
}

fun Box.toRect() = Rect(lx, ly, rx, ry)
