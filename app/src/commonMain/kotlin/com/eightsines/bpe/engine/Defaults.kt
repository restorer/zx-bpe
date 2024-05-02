package com.eightsines.bpe.engine

import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

data class Defaults(
    val currentInk: SciiColor = SciiColor.White,
    val currentPaper: SciiColor = SciiColor.Transparent,
    val currentBright: SciiLight = SciiLight.Transparent,
    val currentFlash: SciiLight = SciiLight.Transparent,
    val currentCharacter: SciiChar = SciiChar.Transparent,

    val border: SciiColor = SciiColor.Black,
    val backgroundColor: SciiColor = SciiColor.Black,
    val backgroundBright: SciiLight = SciiLight.Off,
)
