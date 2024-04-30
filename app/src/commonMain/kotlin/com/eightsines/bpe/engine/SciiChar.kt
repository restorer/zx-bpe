package com.eightsines.bpe.engine

value class SciiChar(val value: Int) {
    companion object {
        val Transparent = SciiChar(-1)
    }
}
