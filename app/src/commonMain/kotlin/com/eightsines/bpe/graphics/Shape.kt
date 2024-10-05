package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

enum class ShapeType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Shape<*>>) {
    Points(1, Shape.Points.Polymorphic),
    Line(2, Shape.Line.Polymorphic),
    FillBox(3, Shape.FillBox.Polymorphic),
    StrokeBox(4, Shape.StrokeBox.Polymorphic),
    Cells(5, Shape.Cells.Polymorphic),
}

sealed interface Shape<T : Cell> {
    val type: ShapeType
    val cellType: CellType

    companion object : BagStuffPacker<Shape<*>>, BagStuffUnpacker<Shape<*>> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Shape<*>) {
            bag.put(value.type.value)

            @Suppress("UNCHECKED_CAST")
            bag.put(value.type.polymorphicPacker as BagStuffPacker<Shape<*>>, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Shape<*> {
            if (version != 1) {
                throw UnsupportedVersionBagUnpackException("Shape", version)
            }

            return when (val type = bag.getInt()) {
                ShapeType.Points.value -> bag.getStuff(Points.Polymorphic)
                ShapeType.Line.value -> bag.getStuff(Line.Polymorphic)
                ShapeType.FillBox.value -> bag.getStuff(FillBox.Polymorphic)
                ShapeType.StrokeBox.value -> bag.getStuff(StrokeBox.Polymorphic)
                ShapeType.Cells.value -> bag.getStuff(Cells.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("Shape", type)
            }
        }
    }

    data class Points<T : Cell>(val points: List<Pair<Int, Int>>, val cell: T) : Shape<T> {
        override val type = ShapeType.Points
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<Points<*>>, BagStuffUnpacker<Points<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Points<*>) {
                bag.put(value.points.size)

                for (point in value.points) {
                    bag.put(point.first)
                    bag.put(point.second)
                }

                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Points<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.Points", version)
                }

                val points = mutableListOf<Pair<Int, Int>>()
                val numPoints = bag.getInt()

                for (i in 0..<numPoints) {
                    points.add(bag.getInt() to bag.getInt())
                }

                return Points(
                    points = points,
                    cell = bag.getStuff(Cell)
                )
            }
        }
    }

    data class Line<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.Line
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<Line<*>>, BagStuffUnpacker<Line<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Line<*>) {
                bag.put(value.sx)
                bag.put(value.sy)
                bag.put(value.ex)
                bag.put(value.ey)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Line<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.Line", version)
                }

                return Line(
                    sx = bag.getInt(),
                    sy = bag.getInt(),
                    ex = bag.getInt(),
                    ey = bag.getInt(),
                    cell = bag.getStuff(Cell)
                )
            }
        }
    }

    data class FillBox<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        constructor(box: Box, cell: T) : this(box.x, box.y, box.ex, box.ey, cell)

        override val type = ShapeType.FillBox
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<FillBox<*>>, BagStuffUnpacker<FillBox<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: FillBox<*>) {
                bag.put(value.sx)
                bag.put(value.sy)
                bag.put(value.ex)
                bag.put(value.ey)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): FillBox<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.FillBox", version)
                }

                return FillBox(
                    sx = bag.getInt(),
                    sy = bag.getInt(),
                    ex = bag.getInt(),
                    ey = bag.getInt(),
                    cell = bag.getStuff(Cell)
                )
            }
        }
    }

    data class StrokeBox<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.StrokeBox
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<StrokeBox<*>>, BagStuffUnpacker<StrokeBox<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: StrokeBox<*>) {
                bag.put(value.sx)
                bag.put(value.sy)
                bag.put(value.ex)
                bag.put(value.ey)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): StrokeBox<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.StrokeBox", version)
                }

                return StrokeBox(
                    sx = bag.getInt(),
                    sy = bag.getInt(),
                    ex = bag.getInt(),
                    ey = bag.getInt(),
                    cell = bag.getStuff(Cell)
                )
            }
        }
    }

    data class Cells<T : Cell>(val x: Int, val y: Int, val crate: Crate<T>) : Shape<T> {
        override val type = ShapeType.Cells
        override val cellType = crate.canvasType.cellType

        internal object Polymorphic : BagStuffPacker<Cells<*>>, BagStuffUnpacker<Cells<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Cells<*>) {
                bag.put(value.x)
                bag.put(value.y)
                bag.put(Crate, value.crate)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Cells<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.Cells", version)
                }

                return Cells(
                    x = bag.getInt(),
                    y = bag.getInt(),
                    crate = bag.getStuff(Crate),
                )
            }
        }
    }
}
