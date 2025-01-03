package com.eightsines.bpe.core

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnknownPolymorphicTypeBagUnpackException
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

enum class CellType(val value: Int, internal val polymorphicPacker: BagStuffPacker<out Cell>) {
    Scii(1, SciiCell.Polymorphic),
    Block(2, BlockCell.Polymorphic);

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
    val isTransparent: Boolean

    companion object : BagStuffPacker<Cell>, BagStuffUnpacker<Cell> {
        fun makeTransparent(type: CellType) = when (type) {
            CellType.Scii -> SciiCell.Transparent
            CellType.Block -> BlockCell.Transparent
        }

        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: Cell) {
            bag.put(value.type.value)
            bag.put(value.type.polymorphicPacker, value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): Cell {
            requireSupportedStuffVersion("Cell", 1, version)

            return when (val type = bag.getInt()) {
                CellType.Scii.value -> bag.getStuff(SciiCell.Polymorphic)
                CellType.Block.value -> bag.getStuff(BlockCell.Polymorphic)
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

    override val isTransparent: Boolean
        get() = this == Transparent

    fun merge(onto: SciiCell) = if (character == SciiChar.Transparent && onto.character == SciiChar.Transparent) {
        Transparent
    } else {
        SciiCell(
            character = character.merge(onto.character),
            ink = ink.merge(onto.ink),
            paper = paper.merge(onto.paper),
            bright = bright.merge(onto.bright),
            flash = flash.merge(onto.flash),
        )
    }

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
            requireSupportedStuffVersion("SciiCell", 1, version)

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

data class BlockCell(val color: SciiColor, val bright: SciiLight) : Cell {
    override val type = CellType.Block

    override val isTransparent: Boolean
        get() = this == Transparent

    fun merge(onto: BlockCell) = BlockCell(
        color = color.merge(onto.color),
        bright = bright.merge(onto.bright),
    )

    companion object : BagStuffUnpacker<BlockCell> {
        val Transparent = BlockCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockCell {
            val cell = Cell.getOutOfTheBag(version, bag)

            if (cell !is BlockCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for BlockCell")
            }

            return cell
        }
    }

    internal object Polymorphic : BagStuffPacker<BlockCell>, BagStuffUnpacker<BlockCell> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BlockCell) {
            bag.put(value.color.value)
            bag.put(value.bright.value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockCell {
            requireSupportedStuffVersion("BlockCell", 1, version)

            return BlockCell(
                color = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
            )
        }
    }
}
