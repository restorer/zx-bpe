package com.eightsines.bpe.test

import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiChar
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight

object SciiCellMother {
    val BlockHorizontalTop = SciiCell(
        character = SciiChar.BlockHorizontalTop,
        ink = SciiColor.White,
        paper = SciiColor.Black,
        bright = SciiLight.On,
        flash = SciiLight.Transparent,
    )

    val BlockVerticalLeft = SciiCell(
        character = SciiChar.BlockVerticalLeft,
        ink = SciiColor.Black,
        paper = SciiColor.White,
        bright = SciiLight.On,
        flash = SciiLight.Transparent,
    )

    val RedSpace = SciiCell(
        character = SciiChar.Space,
        ink = SciiColor.Transparent,
        paper = SciiColor.Red,
        bright = SciiLight.Transparent,
        flash = SciiLight.Transparent,
    )
}
