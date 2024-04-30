package com.eightsines.bpe.engine

value class SciiColor(val value: Int) {
    companion object {
        val Transparent = SciiColor(-1)

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
