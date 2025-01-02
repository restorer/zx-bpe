package com.eightsines.bpe.exporters

import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.foundation.SciiCanvas

class TapExporter(
    private val defaultBorder: Int = 0,
    private val defaultPaper: Int = 0,
    private val defaultInk: Int = 0,
    private val defaultBright: Int = 0,
    private val defaultFlash: Int = 0,
) {
    // https://github.com/restorer/zemux/blob/develop/test-extras-maker/tape_maker.rb
    // https://zxpress.ru/book_articles.php?id=1387
    // https://github.com/mosaicmap/zxs_tap2bas
    // https://github.com/speccyorg/bas2tap

    fun export(border: SciiColor, preview: SciiCanvas): List<Byte> {
        val basicData = emptyList<Byte>()

        val tapData = mutableListOf<Byte>()

        return tapData
    }

    private companion object {
        const val TYPE_PROGRAM = 0
        // const val TYPE_NUMBER_ARRAY = 1
        // const val TYPE_CHARACTER_ARRAY = 2
        // const val TYPE_BYTES = 3

        const val FLAG_HEADER = 0
        const val FLAG_DATA = 255
    }
}

private data class TapChunk(
    val flag: Int,
    val data: List<Int>,
) {
    val checksum by lazy {
        var result = flag

        for (value in data) {
            result = result xor value
        }

        result
    }
}
