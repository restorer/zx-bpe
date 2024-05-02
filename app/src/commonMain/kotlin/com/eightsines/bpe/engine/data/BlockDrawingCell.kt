package com.eightsines.bpe.engine.data

data class BlockDrawingCell(val color: SciiColor, val bright: SciiLight) {
    fun merge(onto: BlockDrawingCell) = BlockDrawingCell(
        color = color.merge(onto.color),
        bright = bright.merge(onto.bright),
    )

    companion object {
        val Transparent = BlockDrawingCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)
    }
}
