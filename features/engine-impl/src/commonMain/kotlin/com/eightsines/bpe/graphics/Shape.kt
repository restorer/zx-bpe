package com.eightsines.bpe.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.foundation.Box
import com.eightsines.bpe.foundation.Cell
import com.eightsines.bpe.foundation.CellType
import com.eightsines.bpe.foundation.Crate

interface BoxLikeShape {
    val sx: Int
    val sy: Int
    val ex: Int
    val ey: Int
}

@BagStuff(isPolymorphic = true)
sealed interface Shape<T : Cell> {
    val cellType: CellType

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 1)
    data class LinkedPoints<T : Cell>(
        @BagStuffWare(1, packer = "putPointsInTheBag", unpacker = "getPointsOutOfTheBag") val points: List<Pair<Int, Int>>,
        @BagStuffWare(2, type = Cell::class) val cell: T,
    ) : Shape<T> {
        override val cellType = cell.type

        internal companion object {
            @Suppress("NOTHING_TO_INLINE")
            internal inline fun putPointsInTheBag(bag: PackableBag, points: List<Pair<Int, Int>>) =
                bag.putList(points) {
                    bag.put(it.first)
                    bag.put(it.second)
                }

            @Suppress("NOTHING_TO_INLINE")
            internal inline fun getPointsOutOfTheBag(bag: UnpackableBag) =
                bag.getList { bag.getInt() to bag.getInt() }
        }
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 2)
    data class Line<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val cellType = cell.type
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 3)
    data class FillBox<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        constructor(box: Box, cell: T) : this(box.lx, box.ly, box.rx, box.ry, cell)
        override val cellType = cell.type
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 4)
    data class StrokeBox<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val cellType = cell.type
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 6)
    data class FillEllipse<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val cellType = cell.type
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 7)
    data class StrokeEllipse<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val cellType = cell.type
    }

    @BagStuff(polymorphicOf = Shape::class, polymorphicId = 5)
    data class Cells<T : Cell>(
        @BagStuffWare(1) val x: Int,
        @BagStuffWare(2) val y: Int,
        @BagStuffWare(3) val crate: Crate<T>,
    ) : Shape<T> {
        override val cellType = crate.canvasType.cellType
    }
}
