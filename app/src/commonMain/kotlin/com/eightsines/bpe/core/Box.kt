package com.eightsines.bpe.core

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

data class Box(val x: Int, val y: Int, val width: Int, val height: Int) {
    val ex = x + width - 1
    val ey = y + height - 1

    fun copyWithOffset(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun copyWithOffset(point: Pair<Int, Int>) = copy(x = this.x + point.first, y = this.y + point.second)

    fun contains(x: Int, y: Int) = x >= this.x && x <= ex && y >= this.y && y <= ey

    companion object : BagStuffPacker<Box>, BagStuffUnpacker<Box> {
        fun of(sx: Int, sy: Int, ex: Int, ey: Int): Box {
            val boxSx = minOf(sx, ex)
            val boxSy = minOf(sy, ey)
            val boxEx = maxOf(sx, ex)
            val boxEy = maxOf(sy, ey)

            return Box(boxSx, boxSy, boxEx - boxSx + 1, boxEy - boxSy + 1)
        }

        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Box) {
            bag.put(value.x)
            bag.put(value.y)
            bag.put(value.width)
            bag.put(value.height)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Box {
            requireSupportedStuffVersion("Box", 1, version)

            val x = bag.getInt()
            val y = bag.getInt()
            val width = bag.getInt()
            val height = bag.getInt()

            return Box(x, y, width, height)
        }
    }
}
