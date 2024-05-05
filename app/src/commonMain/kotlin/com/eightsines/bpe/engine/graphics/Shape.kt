package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.CellType

enum class ShapeType(val value: String) {
    Point("point"),
    Line("line"),
    FillBox("fillBox"),
    StrokeBox("strokeBox"),
    Cells("cells"),
}

sealed interface Shape<T : Cell> : BagStuff {
    val type: ShapeType
    val cellType: CellType

    override val bagStuffVersion: Int
        get() = 1

    companion object : BagStuff.Unpacker<Shape<*>> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Shape<*> {
            if (version != 1) {
                throw BagUnpackException("Unsupported version=$version for Shape")
            }

            return when (val type = bag.getString()) {
                ShapeType.Point.value -> Point.getOutOfTheBag(bag)
                ShapeType.Line.value -> Line.getOutOfTheBag(bag)
                ShapeType.FillBox.value -> FillBox.getOutOfTheBag(bag)
                ShapeType.StrokeBox.value -> StrokeBox.getOutOfTheBag(bag)
                ShapeType.Cells.value -> Cells.getOutOfTheBag(bag)
                else -> throw BagUnpackException("Unknown type=\"$type\" for Shape")
            }
        }
    }

    data class Point<T : Cell>(val x: Int, val y: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.Point
        override val cellType = cell.type

        override fun putInTheBag(bag: PackableBag) {
            putInTheBagBase(bag)

            bag.put(x)
            bag.put(y)
            bag.put(cell)
        }

        companion object {
            fun getOutOfTheBag(bag: UnpackableBag): Point<*> = Point(
                x = bag.getInt(),
                y = bag.getInt(),
                cell = bag.getStuff(Cell.Companion)
            )
        }
    }

    data class Line<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.Line
        override val cellType = cell.type

        override fun putInTheBag(bag: PackableBag) {
            putInTheBagBase(bag)

            bag.put(sx)
            bag.put(sy)
            bag.put(ex)
            bag.put(ey)
            bag.put(cell)
        }

        companion object {
            fun getOutOfTheBag(bag: UnpackableBag): Line<*> = Line(
                sx = bag.getInt(),
                sy = bag.getInt(),
                ex = bag.getInt(),
                ey = bag.getInt(),
                cell = bag.getStuff(Cell.Companion)
            )
        }
    }

    data class FillBox<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.FillBox
        override val cellType = cell.type

        override fun putInTheBag(bag: PackableBag) {
            putInTheBagBase(bag)

            bag.put(sx)
            bag.put(sy)
            bag.put(ex)
            bag.put(ey)
            bag.put(cell)
        }

        companion object {
            fun getOutOfTheBag(bag: UnpackableBag): FillBox<*> = FillBox(
                sx = bag.getInt(),
                sy = bag.getInt(),
                ex = bag.getInt(),
                ey = bag.getInt(),
                cell = bag.getStuff(Cell.Companion)
            )
        }
    }

    data class StrokeBox<T : Cell>(val sx: Int, val sy: Int, val ex: Int, val ey: Int, val cell: T) : Shape<T> {
        override val type = ShapeType.StrokeBox
        override val cellType = cell.type

        override fun putInTheBag(bag: PackableBag) {
            putInTheBagBase(bag)

            bag.put(sx)
            bag.put(sy)
            bag.put(ex)
            bag.put(ey)
            bag.put(cell)
        }

        companion object {
            fun getOutOfTheBag(bag: UnpackableBag): StrokeBox<*> = StrokeBox(
                sx = bag.getInt(),
                sy = bag.getInt(),
                ex = bag.getInt(),
                ey = bag.getInt(),
                cell = bag.getStuff(Cell.Companion)
            )
        }
    }

    data class Cells<T : Cell>(val x: Int, val y: Int, val crate: Crate<T>) : Shape<T> {
        override val type = ShapeType.Cells
        override val cellType = crate.cellType

        override fun putInTheBag(bag: PackableBag) {
            putInTheBagBase(bag)

            bag.put(x)
            bag.put(y)
            bag.put(crate)
        }

        companion object {
            fun getOutOfTheBag(bag: UnpackableBag) = Cells(
                x = bag.getInt(),
                y = bag.getInt(),
                crate = bag.getStuff(Crate.Companion),
            )
        }
    }
}

private fun <T : Cell> Shape<T>.putInTheBagBase(bag: PackableBag) {
    bag.put(type.value)
}
