package com.eightsines.bpe.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.getList
import com.eightsines.bpe.bag.putList
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.foundation.Crate

enum class ShapeType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Shape<*>>) {
    LinkedPoints(1, Shape_LinkedPoints_PolymorphicStuff),
    Line(2, Shape_Line_PolymorphicStuff),
    FillBox(3, Shape_FillBox_PolymorphicStuff),
    StrokeBox(4, Shape_StrokeBox_PolymorphicStuff),
    FillEllipse(6, Shape_FillEllipse_PolymorphicStuff),
    StrokeEllipse(7, Shape_StrokeEllipse_PolymorphicStuff),
    Cells(5, Shape_Cells_PolymorphicStuff),
    // Last value is 7 (StrokeEllipse)
}

interface BoxLikeShape {
    val sx: Int
    val sy: Int
    val ex: Int
    val ey: Int
}

@BagStuff(packer = "Shape", unpacker = "Shape")
sealed interface Shape<T : Cell> {
    val type: ShapeType
    val cellType: CellType

    companion object : BagStuffPacker<Shape<*>>, BagStuffUnpacker<Shape<*>> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Shape<*>) {
            bag.put(value.type.value)
            bag.put(value.type.polymorphicPacker, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Shape<*> {
            requireSupportedStuffVersion("Shape", 1, version)

            return when (val type = bag.getInt()) {
                ShapeType.LinkedPoints.value -> bag.getStuff(Shape_LinkedPoints_PolymorphicStuff)
                ShapeType.Line.value -> bag.getStuff(Shape_Line_PolymorphicStuff)
                ShapeType.FillBox.value -> bag.getStuff(Shape_FillBox_PolymorphicStuff)
                ShapeType.StrokeBox.value -> bag.getStuff(Shape_StrokeBox_PolymorphicStuff)
                ShapeType.FillEllipse.value -> bag.getStuff(Shape_FillEllipse_PolymorphicStuff)
                ShapeType.StrokeEllipse.value -> bag.getStuff(Shape_StrokeEllipse_PolymorphicStuff)
                ShapeType.Cells.value -> bag.getStuff(Shape_Cells_PolymorphicStuff)
                else -> throw UnknownPolymorphicTypeBagUnpackException("Shape", type)
            }
        }
    }

    @BagStuff(isPolymorphic = true)
    data class LinkedPoints<T : Cell>(
        @BagStuffWare(1, packer = "putPointsInTheBag", unpacker = "getPointsOutOfTheBag") val points: List<Pair<Int, Int>>,
        @BagStuffWare(2, type = Cell::class) val cell: T,
    ) : Shape<T> {
        override val type = ShapeType.LinkedPoints
        override val cellType = cell.type

        internal companion object {
            internal fun putPointsInTheBag(bag: PackableBag, points: List<Pair<Int, Int>>) =
                bag.putList(points) {
                    bag.put(it.first)
                    bag.put(it.second)
                }

            internal fun getPointsOutOfTheBag(bag: UnpackableBag) =
                bag.getList { bag.getInt() to bag.getInt() }
        }
    }

    @BagStuff(isPolymorphic = true)
    data class Line<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.Line
        override val cellType = cell.type
    }

    @BagStuff(isPolymorphic = true)
    data class FillBox<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        constructor(box: Box, cell: T) : this(box.lx, box.ly, box.rx, box.ry, cell)

        override val type = ShapeType.FillBox
        override val cellType = cell.type
    }

    @BagStuff(isPolymorphic = true)
    data class StrokeBox<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.StrokeBox
        override val cellType = cell.type
    }

    @BagStuff(isPolymorphic = true)
    data class FillEllipse<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.FillEllipse
        override val cellType = cell.type
    }

    @BagStuff(isPolymorphic = true)
    data class StrokeEllipse<T : Cell>(
        @BagStuffWare(1) override val sx: Int,
        @BagStuffWare(2) override val sy: Int,
        @BagStuffWare(3) override val ex: Int,
        @BagStuffWare(4) override val ey: Int,
        @BagStuffWare(5, type = Cell::class) val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.StrokeEllipse
        override val cellType = cell.type
    }

    @BagStuff(isPolymorphic = true)
    data class Cells<T : Cell>(
        @BagStuffWare(1) val x: Int,
        @BagStuffWare(2) val y: Int,
        @BagStuffWare(3) val crate: Crate<T>,
    ) : Shape<T> {
        override val type = ShapeType.Cells
        override val cellType = crate.canvasType.cellType
    }
}
