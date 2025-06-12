package com.eightsines.bpe.exporters

import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.presentation.UiSpec
import com.eightsines.bpe.util.SpecScii
import kotlin.math.max

class ScrExporter(
    private val defaultPaper: Int = 0,
    private val defaultInk: Int = 0,
    private val defaultBright: Int = 0,
    private val defaultFlash: Int = 0,
) {
    fun export(preview: SciiCanvas): List<Byte> {
        val result = mutableListOf<Byte>()
        val charsData = SpecScii.DATA

        for (third in 0..2) {
            for (line in 0..7) {
                for (row in 0..7) {
                    val sciiY = third * 8 + row

                    for (sciiX in 0..31) {
                        var charValue = max(0, preview.getSciiCell(sciiX, sciiY).character.value - SciiChar.Space.value)
                        var charRow = charValue / SPECSCII_CHAR_COLS

                        if (charRow >= SPECSCII_CHAR_ROWS) {
                            charValue = 0
                            charRow = 0
                        }

                        val charX = (charValue % SPECSCII_CHAR_COLS) * UiSpec.SCII_CELL_SIZE
                        val charY = charRow * UiSpec.SCII_CELL_SIZE
                        val charsIndex = (charY + line) * SpecScii.WIDTH + charX

                        val pixelsValue = charsData[charsIndex] * 128 +
                                charsData[charsIndex + 1] * 64 +
                                charsData[charsIndex + 2] * 32 +
                                charsData[charsIndex + 3] * 16 +
                                charsData[charsIndex + 4] * 8 +
                                charsData[charsIndex + 5] * 4 +
                                charsData[charsIndex + 6] * 2 +
                                charsData[charsIndex + 7]

                        result.add(pixelsValue.toByte())
                    }
                }
            }
        }

        for (sciiY in 0..23) {
            for (sciiX in 0..31) {
                val cell = preview.getSciiCell(sciiX, sciiY)

                val paper = if (cell.paper == SciiColor.Transparent) defaultPaper else cell.paper.value
                val ink = if (cell.ink == SciiColor.Transparent) defaultInk else cell.ink.value
                val bright = if (cell.bright == SciiLight.Transparent) defaultBright else cell.bright.value
                val flash = if (cell.flash == SciiLight.Transparent) defaultFlash else cell.flash.value

                val attrsValue = flash * 128 + bright * 64 + paper * 8 + ink
                result.add(attrsValue.toByte())
            }
        }

        return result
    }

    private companion object {
        private const val SPECSCII_CHAR_ROWS = SpecScii.HEIGHT / UiSpec.SCII_CELL_SIZE
        private const val SPECSCII_CHAR_COLS = SpecScii.WIDTH / UiSpec.SCII_CELL_SIZE
    }
}
