package com.eightsines.bpe.engine

value class SciiLight(val value: Int) {
    companion object {
        val Transparent = SciiLight(-1)
        val Off = SciiLight(0)
        val On = SciiLight(1)
    }
}
