package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.UnpackableBag

enum class CellType(val value: Int) {
    Scii(1),
    Block(2);

    companion object {
        fun of(value: Int) = when (value) {
            Scii.value -> Scii
            Block.value -> Block
            else -> throw IllegalArgumentException("Unknown enum value=$value for CellType")
        }
    }
}

@BagStuff(isPolymorphic = true)
sealed interface Cell {
    val type: CellType
    val isTransparent: Boolean
}

@BagStuff(polymorphicOf = Cell::class, polymorphicId = 1)
data class SciiCell(
    @BagStuffWare(1) val character: SciiChar,
    @BagStuffWare(2) val ink: SciiColor,
    @BagStuffWare(3) val paper: SciiColor,
    @BagStuffWare(4) val bright: SciiLight,
    @BagStuffWare(5) val flash: SciiLight,
) : Cell {
    override val type = CellType.Scii

    override val isTransparent: Boolean
        get() = this == Transparent

    companion object : BagStuffUnpacker<SciiCell> {
        val Transparent = SciiCell(
            character = SciiChar.Transparent,
            ink = SciiColor.Transparent,
            paper = SciiColor.Transparent,
            bright = SciiLight.Transparent,
            flash = SciiLight.Transparent,
        )

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): SciiCell {
            val cell = Cell_Stuff.getOutOfTheBag(version, bag)

            if (cell !is SciiCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for SciiCell")
            }

            return cell
        }
    }
}

@BagStuff(polymorphicOf = Cell::class, polymorphicId = 2)
data class BlockCell(
    @BagStuffWare(1) val color: SciiColor,
    @BagStuffWare(2) val bright: SciiLight,
) : Cell {
    override val type = CellType.Block

    override val isTransparent: Boolean
        get() = this == Transparent

    companion object : BagStuffUnpacker<BlockCell> {
        val Transparent = BlockCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BlockCell {
            val cell = Cell_Stuff.getOutOfTheBag(version, bag)

            if (cell !is BlockCell) {
                throw BagUnpackException("Mismatched type=\"${cell.type}\" for BlockCell")
            }

            return cell
        }
    }
}
