package com.eightsines.bpe.engine.cell

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

enum class CellType(val value: String) {
    Scii("scii"),
    Block("block");

    companion object {
        fun unpack(value: String) = when (value) {
            Scii.value -> Scii
            Block.value -> Block
            else -> throw BagUnpackException("Unsupported value=\"$value\" for CellType")
        }
    }
}

sealed interface Cell : BagStuff {
    val type: CellType

    override val bagStuffVersion: Int
        get() = 1

    companion object : BagStuff.Unpacker<Cell> {
        fun makeTransparent(type: CellType) = when (type) {
            CellType.Scii -> SciiCell.Transparent
            CellType.Block -> BlockDrawingCell.Transparent
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Cell {
            if (version != 1) {
                throw BagUnpackException("Unsupported version=$version for Cell")
            }

            return when (val type = bag.getString()) {
                CellType.Scii.value -> SciiCell.getOutOfTheBag(bag)
                CellType.Scii.value -> BlockDrawingCell.getOutOfTheBag(bag)
                else -> throw BagUnpackException("Unknown type=\"$type\" for Cell")
            }
        }

    }
}

private fun Cell.putInTheBagBase(bag: PackableBag) {
    bag.put(type.value)
}

data class SciiCell(
    val character: SciiChar,
    val ink: SciiColor,
    val paper: SciiColor,
    val bright: SciiLight,
    val flash: SciiLight,
) : Cell {
    override val type = CellType.Scii

    fun merge(onto: SciiCell) = SciiCell(
        character = character.merge(onto.character),
        ink = ink.merge(onto.ink),
        paper = paper.merge(onto.paper),
        bright = bright.merge(onto.bright),
        flash = flash.merge(onto.flash),
    )

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        bag.put(character.value)
        bag.put(ink.value)
        bag.put(paper.value)
        bag.put(bright.value)
        bag.put(flash.value)
    }

    companion object : BagStuff.Unpacker<SciiCell> {
        val Transparent = SciiCell(
            character = SciiChar.Transparent,
            ink = SciiColor.Transparent,
            paper = SciiColor.Transparent,
            bright = SciiLight.Transparent,
            flash = SciiLight.Transparent,
        )

        fun getOutOfTheBag(bag: UnpackableBag) = SciiCell(
            character = SciiChar(bag.getInt()),
            ink = SciiColor(bag.getInt()),
            paper = SciiColor(bag.getInt()),
            bright = SciiLight(bag.getInt()),
            flash = SciiLight(bag.getInt()),
        )

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SciiCell {
            val cell = Cell.getOutOfTheBag(version, bag)

            if (cell !is SciiCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for SciiCell")
            }

            return cell
        }
    }
}

data class BlockDrawingCell(val color: SciiColor, val bright: SciiLight) : Cell {
    override val type = CellType.Block

    fun merge(onto: BlockDrawingCell) = BlockDrawingCell(
        color = color.merge(onto.color),
        bright = bright.merge(onto.bright),
    )

    override fun putInTheBag(bag: PackableBag) {
        putInTheBagBase(bag)

        bag.put(color.value)
        bag.put(bright.value)
    }

    companion object : BagStuff.Unpacker<BlockDrawingCell> {
        val Transparent = BlockDrawingCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)

        fun getOutOfTheBag(bag: UnpackableBag) = BlockDrawingCell(
            color = SciiColor(bag.getInt()),
            bright = SciiLight(bag.getInt()),
        )

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockDrawingCell {
            val cell = Cell.getOutOfTheBag(version, bag)

            if (cell !is BlockDrawingCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for BlockDrawingCell")
            }

            return cell
        }
    }
}
