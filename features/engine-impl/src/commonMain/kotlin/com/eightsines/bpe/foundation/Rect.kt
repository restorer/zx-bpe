package com.eightsines.bpe.foundation

data class Rect(val sx: Int, val sy: Int, val ex: Int, val ey: Int) {
    val width = maxOf(sx, ex) - minOf(sx, ex) + 1
    val height = maxOf(sy, ey) - minOf(sy, ey) + 1
}
