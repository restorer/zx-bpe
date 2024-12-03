package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

class MutablePalette(
    var ink: SciiColor = SciiColor.Transparent,
    var paper: SciiColor = SciiColor.Transparent,
    var bright: SciiLight = SciiLight.Transparent,
    var flash: SciiLight = SciiLight.Transparent,
    var character: SciiChar = SciiChar.Transparent,
) {
    fun setFrom(other: MutablePalette) {
        ink = other.ink
        paper = other.paper
        bright = other.bright
        flash = other.flash
        character = other.character
    }

    override fun toString() = "MutablePalette(ink=$ink, paper=$paper, bright=$bright, flash=$flash, character=$character)"

    companion object : BagStuffPacker<MutablePalette>, BagStuffUnpacker<MutablePalette> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: MutablePalette) {
            bag.put(value.ink.value)
            bag.put(value.paper.value)
            bag.put(value.bright.value)
            bag.put(value.flash.value)
            bag.put(value.character.value)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): MutablePalette {
            requireSupportedStuffVersion("MutablePalette", 1, version)

            return MutablePalette(
                ink = SciiColor(bag.getInt()),
                paper = SciiColor(bag.getInt()),
                bright = SciiLight(bag.getInt()),
                flash = SciiLight(bag.getInt()),
                character = SciiChar(bag.getInt()),
            )
        }
    }
}
