package com.eightsines.bpe.engine.data

sealed interface BlockMergeCell {
    fun toSciiCell(): SciiCell

    data class Horizontal(val topColor: SciiColor, val bottomColor: SciiColor, val bright: SciiLight) : BlockMergeCell {
        override fun toSciiCell() = makeSciiCell(topColor, bottomColor, bright)

        fun merge(onto: Horizontal) = Horizontal(
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

    data class Vertical(val leftColor: SciiColor, val rightColor: SciiColor, val bright: SciiLight) : BlockMergeCell {
        override fun toSciiCell() = makeSciiCell(leftColor, rightColor, bright)

        fun merge(onto: Vertical) = Vertical(
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
}
