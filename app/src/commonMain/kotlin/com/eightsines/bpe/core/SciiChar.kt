package com.eightsines.bpe.core

import kotlin.jvm.JvmInline

// https://en.wikipedia.org/wiki/ZX_Spectrum_character_set
// https://trilirium.eversong.ru/ZX_vault/ZX_chars.html

@JvmInline
value class SciiChar(val value: Int) {
    fun merge(onto: SciiChar) =
        if (value != VALUE_TRANSPARENT) this else onto

    override fun toString() = "SciiChar($value)"

    companion object {
        const val VALUE_TRANSPARENT = -1

        const val BLOCK_VALUE_FIRST = 0x80
        const val BLOCK_VALUE_LAST = 0x8F

        const val BLOCK_BIT_TR = 0x01
        const val BLOCK_BIT_TL = 0x02
        const val BLOCK_BIT_BR = 0x04
        const val BLOCK_BIT_BL = 0x08
        const val BLOCK_MASK = BLOCK_BIT_TR + BLOCK_BIT_TL + BLOCK_BIT_BR + BLOCK_BIT_BL

        val Transparent = SciiChar(-1)
        val Space = SciiChar(32)
        val Copyright = SciiChar(127)

        val BlockSpace = SciiChar(BLOCK_VALUE_FIRST)
        val BlockHorizontalTop = SciiChar(BLOCK_VALUE_FIRST + BLOCK_BIT_TR + BLOCK_BIT_TL)
        val BlockHorizontalBottom = SciiChar(BLOCK_VALUE_FIRST + BLOCK_BIT_BR + BLOCK_BIT_BL)
        val BlockVerticalLeft = SciiChar(BLOCK_VALUE_FIRST + BLOCK_BIT_TL + BLOCK_BIT_BL) // 0x80 + 0x02 + 0x08
        val BlockVerticalRight = SciiChar(BLOCK_VALUE_FIRST + BLOCK_BIT_TR + BLOCK_BIT_BR)
        val BlockFull = SciiChar(BLOCK_VALUE_LAST)
    }
}
