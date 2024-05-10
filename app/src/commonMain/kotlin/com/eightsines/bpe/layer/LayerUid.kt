package com.eightsines.bpe.layer

import kotlin.jvm.JvmInline

@JvmInline
value class LayerUid(val value: String) {
    companion object {
        val Background = LayerUid("")
    }
}
