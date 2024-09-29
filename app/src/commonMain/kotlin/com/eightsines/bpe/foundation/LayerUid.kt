package com.eightsines.bpe.foundation

import kotlin.jvm.JvmInline

@JvmInline
value class LayerUid(val value: String) {
    override fun toString() = "LayerUid($value)"

    companion object {
        val Background = LayerUid("")
    }
}
