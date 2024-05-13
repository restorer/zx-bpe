package com.eightsines.bpe.model

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

enum class CellType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Cell>) {
    Scii(1, SciiCell.Polymorphic),
    Block(2, BlockDrawingCell.Polymorphic);

    companion object {
        fun of(value: Int) = when (value) {
            Scii.value -> Scii
            Block.value -> Block
            else -> throw IllegalArgumentException("Unknown enum value=$value for CellType")
        }
    }
}

sealed interface Cell {
    val type: CellType

    companion object : BagStuffPacker<Cell>, BagStuffUnpacker<Cell> {
        fun makeTransparent(type: CellType) = when (type) {
            CellType.Scii -> SciiCell.Transparent
            CellType.Block -> BlockDrawingCell.Transparent
        }

        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Cell) {
            bag.put(value.type.value)

            @Suppress("UNCHECKED_CAST")
            bag.put(value.type.polymorphicPacker as BagStuffPacker<Cell>, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Cell {
            if (version != 1) {
                throw UnsupportedVersionBagUnpackException("Cell", version)
            }

            return when (val type = bag.getInt()) {
                CellType.Scii.value -> bag.getStuff(SciiCell.Polymorphic)
                CellType.Block.value -> bag.getStuff(BlockDrawingCell.Polymorphic)
                else -> throw UnknownPolymorphicTypeBagUnpackException("Cell", type)
            }
        }
    }
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

    companion object : BagStuffUnpacker<SciiCell> {
        val Transparent = SciiCell(
            character = SciiChar.Transparent,
            ink = SciiColor.Transparent,
            paper = SciiColor.Transparent,
            bright = SciiLight.Transparent,
            flash = SciiLight.Transparent,
        )

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SciiCell {
            val cell = Cell.getOutOfTheBag(version, bag)

            if (cell !is SciiCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for SciiCell")
            }

            return cell
        }
    }

    internal object Polymorphic : BagStuffPacker<SciiCell>, BagStuffUnpacker<SciiCell> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: SciiCell) {
            bag.put(value.character.value)
            bag.put(value.ink.value)
            bag.put(value.paper.value)
            bag.put(value.bright.value)
            bag.put(value.flash.value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SciiCell {
            if (version != 1) {
                throw UnsupportedVersionBagUnpackException("SciiCell", version)
            }

            return SciiCell(
                character = SciiChar(bag.getInt()),
                ink = SciiColor(bag.getInt()),
                paper = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
                flash = SciiLight(bag.getInt()),
            )
        }
    }
}

data class BlockDrawingCell(val color: SciiColor, val bright: SciiLight) : Cell {
    override val type = CellType.Block

    fun merge(onto: BlockDrawingCell) = BlockDrawingCell(
        color = color.merge(onto.color),
        bright = bright.merge(onto.bright),
    )

    companion object : BagStuffUnpacker<BlockDrawingCell> {
        val Transparent = BlockDrawingCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockDrawingCell {
            val cell = Cell.getOutOfTheBag(version, bag)

            if (cell !is BlockDrawingCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for BlockDrawingCell")
            }

            return cell
        }
    }

    internal object Polymorphic : BagStuffPacker<BlockDrawingCell>, BagStuffUnpacker<BlockDrawingCell> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BlockDrawingCell) {
            bag.put(value.color.value)
            bag.put(value.bright.value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockDrawingCell {
            if (version != 1) {
                throw UnsupportedVersionBagUnpackException("BlockDrawingCell", version)
            }

            return BlockDrawingCell(
                color = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
            )
        }
    }
}
