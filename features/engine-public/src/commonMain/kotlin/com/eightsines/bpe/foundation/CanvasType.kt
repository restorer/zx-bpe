package com.eightsines.bpe.foundation

import com.eightsines.bpe.bag.BagSinglefield

@BagSinglefield(field = "value", creator = "of")
sealed class CanvasType {
    abstract val value: Int
    abstract val cellType: CellType
    abstract val transparentCell: Cell

    abstract fun toSciiPosition(drawingX: Int, drawingY: Int): Pair<Int, Int>

    data object Scii : CanvasType() {
        const val POLYMORPHIC_ID = 1

        override val value = POLYMORPHIC_ID
        override val cellType = CellType.Scii
        override val transparentCell = SciiCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to drawingY
    }

    data object HBlock : CanvasType() {
        const val POLYMORPHIC_ID = 2

        override val value = POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = drawingX to (drawingY / 2)
    }

    data object VBlock : CanvasType() {
        const val POLYMORPHIC_ID = 3

        override val value = Scii.POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to drawingY
    }

    data object QBlock : CanvasType() {
        const val POLYMORPHIC_ID = 4

        override val value = POLYMORPHIC_ID
        override val cellType = CellType.Block
        override val transparentCell = BlockCell.Transparent

        override fun toSciiPosition(drawingX: Int, drawingY: Int) = (drawingX / 2) to (drawingY / 2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        other as CanvasType
        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    companion object {
        fun of(value: Int) = when (value) {
            Scii.POLYMORPHIC_ID -> Scii
            HBlock.POLYMORPHIC_ID -> HBlock
            VBlock.POLYMORPHIC_ID -> VBlock
            QBlock.POLYMORPHIC_ID -> QBlock
            else -> throw IllegalArgumentException("Unknown enum value=$value for CanvasType")
        }
    }
}

val CanvasType?.isBlock: Boolean
    get() = when (this) {
        null -> false
        CanvasType.Scii -> false
        CanvasType.HBlock, CanvasType.VBlock, CanvasType.QBlock -> true
    }
