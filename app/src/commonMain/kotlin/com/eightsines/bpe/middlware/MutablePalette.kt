package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

class MutablePalette(
    var ink: SciiColor = SciiColor.Transparent,
    var paper: SciiColor = SciiColor.Transparent,
    var bright: SciiLight = SciiLight.Transparent,
    var flash: SciiLight = SciiLight.Transparent,
    var character: SciiChar = SciiChar.Transparent,
)
