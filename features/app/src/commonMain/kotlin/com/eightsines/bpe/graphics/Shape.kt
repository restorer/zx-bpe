package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.CellType
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.getList
import com.eightsines.bpe.bag.putList
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.foundation.CrateStuff

enum class ShapeType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Shape<*>>) {
    LinkedPoints(1, Shape.LinkedPoints.Polymorphic),
    Line(2, Shape.Line.Polymorphic),
    FillBox(3, Shape.FillBox.Polymorphic),
    StrokeBox(4, Shape.StrokeBox.Polymorphic),
    FillEllipse(6, Shape.FillEllipse.Polymorphic),
    StrokeEllipse(7, Shape.StrokeEllipse.Polymorphic),
    Cells(5, Shape.Cells.Polymorphic),
    // Last value is 7 (StrokeEllipse)
}

interface BoxLikeShape {
    val sx: Int
    val sy: Int
    val ex: Int
    val ey: Int
}

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
                ShapeType.LinkedPoints.value -> bag.getStuff(LinkedPoints.Polymorphic)
                ShapeType.Line.value -> bag.getStuff(Line.Polymorphic)
                ShapeType.FillBox.value -> bag.getStuff(FillBox.Polymorphic)
                ShapeType.StrokeBox.value -> bag.getStuff(StrokeBox.Polymorphic)
                ShapeType.FillEllipse.value -> bag.getStuff(FillEllipse.Polymorphic)
                ShapeType.StrokeEllipse.value -> bag.getStuff(StrokeEllipse.Polymorphic)
                ShapeType.Cells.value -> bag.getStuff(Cells.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("Shape", type)
            }
        }
    }

    data class LinkedPoints<T : Cell>(val points: List<Pair<Int, Int>>, val cell: T) : Shape<T> {
        override val type = ShapeType.LinkedPoints
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<LinkedPoints<*>>, BagStuffUnpacker<LinkedPoints<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: LinkedPoints<*>) {
                bag.putList(value.points) {
                    bag.put(it.first)
                    bag.put(it.second)
                }

                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): LinkedPoints<*> {
                requireSupportedStuffVersion("Shape.LinkedPoints", 1, version)

                val points = bag.getList { bag.getInt() to bag.getInt() }
                val cell = bag.getStuff(Cell)

                return LinkedPoints(
                    points = points,
                    cell = cell,
                )
            }
        }
    }

    data class Line<T : Cell>(
        override val sx: Int,
        override val sy: Int,
        override val ex: Int,
        override val ey: Int,
        val cell: T,
    ) : Shape<T>, BoxLikeShape {
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
                requireSupportedStuffVersion("Shape.Line", 1, version)

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

    data class FillBox<T : Cell>(
        override val sx: Int,
        override val sy: Int,
        override val ex: Int,
        override val ey: Int,
        val cell: T,
    ) : Shape<T>, BoxLikeShape {
        constructor(box: Box, cell: T) : this(box.lx, box.ly, box.rx, box.ry, cell)

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
                requireSupportedStuffVersion("Shape.FillBox", 1, version)

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

    data class StrokeBox<T : Cell>(
        override val sx: Int,
        override val sy: Int,
        override val ex: Int,
        override val ey: Int,
        val cell: T,
    ) : Shape<T>, BoxLikeShape {
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
                requireSupportedStuffVersion("Shape.StrokeBox", 1, version)

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

    data class FillEllipse<T : Cell>(
        override val sx: Int,
        override val sy: Int,
        override val ex: Int,
        override val ey: Int,
        val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.FillEllipse
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<FillEllipse<*>>, BagStuffUnpacker<FillEllipse<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: FillEllipse<*>) {
                bag.put(value.sx)
                bag.put(value.sy)
                bag.put(value.ex)
                bag.put(value.ey)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): FillEllipse<*> {
                requireSupportedStuffVersion("Shape.FillEllipse", 1, version)

                return FillEllipse(
                    sx = bag.getInt(),
                    sy = bag.getInt(),
                    ex = bag.getInt(),
                    ey = bag.getInt(),
                    cell = bag.getStuff(Cell)
                )
            }
        }
    }

    data class StrokeEllipse<T : Cell>(
        override val sx: Int,
        override val sy: Int,
        override val ex: Int,
        override val ey: Int,
        val cell: T,
    ) : Shape<T>, BoxLikeShape {
        override val type = ShapeType.StrokeEllipse
        override val cellType = cell.type

        internal object Polymorphic : BagStuffPacker<StrokeEllipse<*>>, BagStuffUnpacker<StrokeEllipse<*>> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: StrokeEllipse<*>) {
                bag.put(value.sx)
                bag.put(value.sy)
                bag.put(value.ex)
                bag.put(value.ey)
                bag.put(Cell, value.cell)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): StrokeEllipse<*> {
                requireSupportedStuffVersion("Shape.StrokeEllipse", 1, version)

                return StrokeEllipse(
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
                bag.put(CrateStuff, value.crate)
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Cells<*> {
                requireSupportedStuffVersion("Shape.Cells", 1, version)

                val x = bag.getInt()
                val y = bag.getInt()
                val crate: Crate<*> = bag.getStuff(CrateStuff)

                return Cells(x, y, crate)
            }
        }
    }
}
