package com.eightsines.bpe.foundation

import kotlin.jvm.JvmInline

@JvmInline
value class SciiLight(val value: Int) {
    override fun toString() = "SciiLight($value)"

    companion object {
        val ForceTransparent = SciiLight(-2)
        val Transparent = SciiLight(-1)
        val Off = SciiLight(0)
        val On = SciiLight(1)
    }
}
