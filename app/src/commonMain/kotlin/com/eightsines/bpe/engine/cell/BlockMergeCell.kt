package com.eightsines.bpe.engine.cell

import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

interface BlockMergeCell {
    fun toSciiCell(): SciiCell
}

data class HorizontalBlockMergeCell(
    val topColor: SciiColor,
    val bottomColor: SciiColor,
    val bright: SciiLight,
) : BlockMergeCell {
    override fun toSciiCell() = makeSciiCell(topColor, bottomColor, bright)

    fun merge(onto: HorizontalBlockMergeCell) = HorizontalBlockMergeCell(
        topColor = topColor.merge(onto.topColor),
        bottomColor = bottomColor.merge(onto.bottomColor),
        bright = bright.merge(onto.bright),
    )

    companion object {
        fun makeSciiCell(topColor: SciiColor, bottomColor: SciiColor, bright: SciiLight) =
            if (topColor == SciiColor.Transparent && bottomColor == SciiColor.Transparent) {
                SciiCell.Transparent
            } else {
                SciiCell(
                    character = SciiChar.BlockHorizontalTop,
                    ink = topColor,
                    paper = bottomColor,
                    bright = bright,
                    flash = SciiLight.Transparent,
                )
            }
    }
}

data class VerticalBlockMergeCell(
    val leftColor: SciiColor,
    val rightColor: SciiColor,
    val bright: SciiLight,
) : BlockMergeCell {
    override fun toSciiCell() = makeSciiCell(leftColor, rightColor, bright)

    fun merge(onto: VerticalBlockMergeCell) = VerticalBlockMergeCell(
        leftColor = leftColor.merge(onto.leftColor),
        rightColor = rightColor.merge(onto.rightColor),
        bright = bright.merge(onto.bright),
    )

    companion object {
        fun makeSciiCell(leftColor: SciiColor, rightColor: SciiColor, bright: SciiLight) =
            if (leftColor == SciiColor.Transparent && rightColor == SciiColor.Transparent) {
                SciiCell.Transparent
            } else {
                SciiCell(
                    character = SciiChar.BlockVerticalLeft,
                    ink = leftColor,
                    paper = rightColor,
                    bright = bright,
                    flash = SciiLight.Transparent,
                )
            }
    }
}
