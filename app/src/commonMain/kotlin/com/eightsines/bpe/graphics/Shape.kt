package com.eightsines.bpe.graphics

import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.CellType
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

enum class ShapeType(val value: String, internal val polymorphicPacker: BagStuffPacker<out Shape<*>>) {
    Point("point", Shape.Point.Polymorphic),
    Line("line", Shape.Line.Polymorphic),
    FillBox("fillBox", Shape.FillBox.Polymorphic),
    StrokeBox("strokeBox", Shape.StrokeBox.Polymorphic),
    Cells("cells", Shape.Cells.Polymorphic),
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

            return when (val type = bag.getString()) {
                ShapeType.Point.value -> bag.getStuff(Point.Polymorphic)
                ShapeType.Line.value -> bag.getStuff(Line.Polymorphic)
                ShapeType.FillBox.value -> bag.getStuff(FillBox.Polymorphic)
                ShapeType.StrokeBox.value -> bag.getStuff(StrokeBox.Polymorphic)
                ShapeType.Cells.value -> bag.getStuff(Cells.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("Shape", type)
            }
        }
    }

    data class Point<T : Cell>(val x: Int, val y: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.Point
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<Point<*>>, BagStuffUnpacker<Point<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: Point<*>) {
                bag.put(value.x)
                bag.put(value.y)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Point<*> {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("Shape.Point", version)
                }

                return Point(
                    x = bag.getInt(),
                    y = bag.getInt(),
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
        override val cellType = crate.cellType

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
