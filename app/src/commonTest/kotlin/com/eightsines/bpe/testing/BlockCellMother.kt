package com.eightsines.bpe.testing

import com.eightsines.bpe.core.BlockCell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

object BlockCellMother {
    val Black = BlockCell(color = SciiColor.Black, bright = SciiLight.Transparent)
    val WhiteBright = BlockCell(color = SciiColor.White, bright = SciiLight.On)

    fun of(value: Int) = BlockCell(color = SciiColor(value), bright = SciiLight.Transparent)
}
