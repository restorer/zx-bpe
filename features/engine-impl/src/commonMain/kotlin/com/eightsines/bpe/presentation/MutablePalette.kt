package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight

@BagStuff
class MutablePalette(
    @BagStuffWare(1) var ink: SciiColor = SciiColor.Transparent,
    @BagStuffWare(2) var paper: SciiColor = SciiColor.Transparent,
    @BagStuffWare(3) var bright: SciiLight = SciiLight.Transparent,
    @BagStuffWare(4) var flash: SciiLight = SciiLight.Transparent,
    @BagStuffWare(5) var character: SciiChar = SciiChar.Transparent,
) {
    fun setFrom(other: MutablePalette) {
        ink = other.ink
        paper = other.paper
        bright = other.bright
        flash = other.flash
        character = other.character
    }

    fun clear() {
        ink = SciiColor.Transparent
        paper = SciiColor.Transparent
        bright = SciiLight.Transparent
        flash = SciiLight.Transparent
        character = SciiChar.Transparent
    }

    override fun toString() = "MutablePalette(ink=$ink, paper=$paper, bright=$bright, flash=$flash, character=$character)"
}
