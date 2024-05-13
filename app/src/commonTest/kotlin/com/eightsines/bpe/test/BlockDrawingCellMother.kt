package com.eightsines.bpe.test

import com.eightsines.bpe.model.BlockDrawingCell
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

object BlockDrawingCellMother {
    val Black = BlockDrawingCell(color = SciiColor.Black, bright = SciiLight.Transparent)
    val White = BlockDrawingCell(color = SciiColor.White, bright = SciiLight.On)
}
