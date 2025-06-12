package com.eightsines.bpe.foundation

import kotlin.jvm.JvmInline

@JvmInline
value class SciiColor(val value: Int) {
    override fun toString() = "SciiColor($value)"

    companion object {
        val Transparent = SciiColor(-1)
        val Black = SciiColor(0)

        @Suppress("unused")
        val Navy = SciiColor(1)

        val Red = SciiColor(2)

        @Suppress("unused")
        val Magenta = SciiColor(3)

        @Suppress("unused")
        val Green = SciiColor(4)

        val Blue = SciiColor(5)
        val Yellow = SciiColor(6)
        val White = SciiColor(7)
    }
}
