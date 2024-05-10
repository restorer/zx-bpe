package com.eightsines.bpe.model

sealed interface MergeCell {
    fun toSciiCell(): SciiCell
}

data class HBlockMergeCell(
    val topColor: SciiColor,
    val bottomColor: SciiColor,
    val bright: SciiLight,
) : MergeCell {
    override fun toSciiCell() = makeSciiCell(topColor, bottomColor, bright)

    fun merge(onto: HBlockMergeCell) = HBlockMergeCell(
        topColor = topColor.merge(onto.topColor),
        bottomColor = bottomColor.merge(onto.bottomColor),
        bright = bright.merge(onto.bright),
    )

    companion object {
        val Transparent = HBlockMergeCell(
            topColor = SciiColor.Transparent,
            bottomColor = SciiColor.Transparent,
            bright = SciiLight.Transparent,
        )

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

data class VBlockMergeCell(
    val leftColor: SciiColor,
    val rightColor: SciiColor,
    val bright: SciiLight,
) : MergeCell {
    override fun toSciiCell() = makeSciiCell(leftColor, rightColor, bright)

    fun merge(onto: VBlockMergeCell) = VBlockMergeCell(
        leftColor = leftColor.merge(onto.leftColor),
        rightColor = rightColor.merge(onto.rightColor),
        bright = bright.merge(onto.bright),
    )

    companion object {
        val Transparent = VBlockMergeCell(
            leftColor = SciiColor.Transparent,
            rightColor = SciiColor.Transparent,
            bright = SciiLight.Transparent,
        )

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
