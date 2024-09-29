package com.eightsines.bpe.core

import kotlin.jvm.JvmInline

@JvmInline
value class SciiColor(val value: Int) {
    fun merge(onto: SciiColor) =
        if (value != VALUE_TRANSPARENT) this else onto

    override fun toString() = "SciiColor($value)"

    companion object {
        private const val VALUE_TRANSPARENT = -1

        val Transparent = SciiColor(VALUE_TRANSPARENT)

        val Black = SciiColor(0)
        val Navy = SciiColor(1)
        val Red = SciiColor(2)
        val Magenta = SciiColor(3)
        val Green = SciiColor(4)
        val Blue = SciiColor(5)
        val Yellow = SciiColor(6)
        val White = SciiColor(7)
    }
}
