package com.eightsines.bpe.testing

import com.eightsines.bpe.foundation.BlockCell
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight

object BlockCellMother {
    val Black = BlockCell(color = SciiColor.Black, bright = SciiLight.Transparent)
    val WhiteBright = BlockCell(color = SciiColor.White, bright = SciiLight.On)

    fun of(value: Int) = BlockCell(color = SciiColor(value), bright = SciiLight.Transparent)
}
