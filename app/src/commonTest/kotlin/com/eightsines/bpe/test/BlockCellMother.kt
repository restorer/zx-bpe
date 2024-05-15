package com.eightsines.bpe.test

import com.eightsines.bpe.model.BlockCell
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

object BlockCellMother {
    val Black = BlockCell(color = SciiColor.Black, bright = SciiLight.Transparent)
    val White = BlockCell(color = SciiColor.White, bright = SciiLight.On)
}
