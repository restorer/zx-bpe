package com.eightsines.bpe.core

import kotlin.jvm.JvmInline

@JvmInline
value class SciiLight(val value: Int) {
    fun merge(onto: SciiLight) =
        if (value != VALUE_TRANSPARENT) this else onto

    override fun toString() = "SciiLight($value)"

    companion object {
        private const val VALUE_TRANSPARENT = -1
        private const val VALUE_OFF = 0
        private const val VALUE_ON = 1

        val Transparent = SciiLight(VALUE_TRANSPARENT)
        val Off = SciiLight(VALUE_OFF)
        val On = SciiLight(VALUE_ON)
    }
}
