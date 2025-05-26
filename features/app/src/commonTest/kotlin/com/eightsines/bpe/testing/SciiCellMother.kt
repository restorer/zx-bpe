package com.eightsines.bpe.testing

import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

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

    val BlueSpace = SciiCell(
        character = SciiChar.Space,
        ink = SciiColor.Blue,
        paper = SciiColor.Blue,
        bright = SciiLight.Off,
        flash = SciiLight.Off,
    )
}
