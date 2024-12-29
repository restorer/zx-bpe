package com.eightsines.bpe.core

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

class Box private constructor(val lx: Int, val ly: Int, val width: Int, val height: Int) {
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

    companion object : BagStuffPacker<Box>, BagStuffUnpacker<Box> {
        fun ofSize(x: Int, y: Int, width: Int, height: Int) = Box(x, y, width, height)

        fun ofCoords(sx: Int, sy: Int, ex: Int, ey: Int): Box {
            val lx = minOf(sx, ex)
            val ly = minOf(sy, ey)
            val width = maxOf(sx, ex) - lx + 1
            val height = maxOf(sy, ey) - ly + 1

            return Box(lx, ly, width, height)
        }

        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Box) {
            bag.put(value.lx)
            bag.put(value.ly)
            bag.put(value.width)
            bag.put(value.height)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Box {
            requireSupportedStuffVersion("Box", 1, version)

            val lx = bag.getInt()
            val ly = bag.getInt()
            val width = bag.getInt()
            val height = bag.getInt()

            return Box(lx, ly, width, height)
        }
    }
}

fun Box.toRect() = Rect(lx, ly, rx, ry)
