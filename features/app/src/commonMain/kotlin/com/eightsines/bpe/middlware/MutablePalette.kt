package com.eightsines.bpe.middlware

import com.eightsines.bpe.core.SciiChar
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion

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
