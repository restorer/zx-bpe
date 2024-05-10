package com.eightsines.bpe.graphics

data class Box(val x: Int, val y: Int, val width: Int, val height: Int) {
    val ex = x + width - 1
    val ey = y + height - 1
}
