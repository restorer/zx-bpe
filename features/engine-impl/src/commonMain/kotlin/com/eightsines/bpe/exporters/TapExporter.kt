package com.eightsines.bpe.exporters

import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.foundation.SciiCell
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.isTransparent
import kotlin.experimental.xor

// https://zxpress.ru/book_articles.php?id=1387
// https://github.com/speccyorg/bas2tap
// https://github.com/scrpi/zasm/blob/master/Examples/zx_spectrum_basic_tokens.s
// https://github.com/mosaicmap/zxs_tap2bas

class TapExporter(private val fallbackColor: Int = 0) {
    private var lastPaper: Int = 0
    private var lastInk: Int = 0
    private var lastBright: Int = 0
    private var lastFlash: Int = 0

    fun export(name: String, border: SciiColor, preview: SciiCanvas): List<Byte> {
        val program = buildProgram(border, preview)

        return TapFile().run {
            appendProgram(name, program.buffer, 10)
            buffer
        }
    }

    private fun buildProgram(border: SciiColor, preview: SciiCanvas): BasicProgram {
        val program = BasicProgram()

        BasicLine(10).apply {
            append(BasicLine.TOKEN_REM)
            append("Made with BPE")
            program.appendLine(this)
        }

        val border = if (border.isTransparent) fallbackColor else border.value

        BasicLine(20).apply {
            append(BasicLine.TOKEN_INK)
            appendNumber(border)
            append(':')

            append(BasicLine.TOKEN_PAPER)
            appendNumber(border)
            append(':')

            append(BasicLine.TOKEN_FLASH)
            appendNumber(0)
            append(':')

            append(BasicLine.TOKEN_BRIGHT)
            appendNumber(0)
            append(':')

            append(BasicLine.TOKEN_INVERSE)
            appendNumber(0)
            append(':')

            append(BasicLine.TOKEN_OVER)
            appendNumber(0)
            append(':')

            append(BasicLine.TOKEN_BORDER)
            appendNumber(border)
            append(':')

            append(BasicLine.TOKEN_CLS)
            program.appendLine(this)
        }

        lastPaper = border
        lastInk = border
        lastBright = 0
        lastFlash = 0

        BasicLine(30).apply {
            append(BasicLine.TOKEN_PRINT)
            append('"')

            for (sciiX in 0..<22) {
                for (sciiY in 0..<32) {
                    appendPrintCell(this, preview.getSciiCell(sciiY, sciiX))
                }
            }

            append('"')
            program.appendLine(this)
        }

        lastPaper = border
        lastInk = -1
        lastBright = -1
        lastFlash = -1

        BasicLine(40).apply {
            append(BasicLine.TOKEN_PRINT)
            append('#')
            appendNumber(0)
            append(';')
            append(BasicLine.TOKEN_AT)
            appendNumber(0)
            append(',')
            appendNumber(0)
            append(";\"")

            for (sciiX in 22..<24) {
                for (sciiY in 0..<32) {
                    appendPrintCell(this, preview.getSciiCell(sciiY, sciiX))
                }
            }

            append('"')
            program.appendLine(this)
        }

        BasicLine(50).apply {
            append(BasicLine.TOKEN_PAUSE)
            appendNumber(1)
            append(':')
            append(BasicLine.TOKEN_GO_TO)
            appendNumber(50)
            program.appendLine(this)
        }

        return program
    }

    private fun appendPrintCell(line: BasicLine, cell: SciiCell) {
        val ink = if (cell.ink.isTransparent) fallbackColor else cell.ink.value
        val paper = if (cell.paper.isTransparent) fallbackColor else cell.paper.value
        val bright = if (cell.bright.isTransparent) 0 else cell.bright.value
        val flash = if (cell.flash.isTransparent) 0 else cell.flash.value
        val character = if (cell.character.isTransparent) ' '.code else cell.character.value

        if (ink != lastInk) {
            line.append(BasicLine.ATTR_INK)
            line.append(ink.toByte())
            lastInk = ink
        }

        if (paper != lastPaper) {
            line.append(BasicLine.ATTR_PAPER)
            line.append(paper.toByte())
            lastPaper = paper
        }

        if (bright != lastBright) {
            line.append(BasicLine.ATTR_BRIGHT)
            line.append(bright.toByte())
            lastBright = bright
        }

        if (flash != lastFlash) {
            line.append(BasicLine.ATTR_FLASH)
            line.append(flash.toByte())
            lastFlash = flash
        }

        if (character == '"'.code) {
            line.append('"'.code.toByte())
        }

        line.append(character.toByte())
    }
}

class BasicLine(val lineNumber: Int) {
    private val _buffer = mutableListOf<Byte>()

    val buffer: List<Byte>
        get() = _buffer

    fun append(value: Byte) {
        _buffer.add(value)
    }

    fun append(value: Char) {
        _buffer.add(value.code.toByte())
    }

    fun append(value: String) {
        _buffer.addAll(value.map { it.code.toByte() })
    }

    fun appendNumber(value: Int) {
        var value = value
        _buffer.addAll(value.toString().map { it.code.toByte() })

        _buffer.add(NUMBER_MARKER)
        _buffer.add(0)

        if (value >= 0) {
            _buffer.add(0)
        } else {
            _buffer.add(0xFF.toByte())
            value += 0x10000 // Bug in Spectrum ROM: INT(-65536) will result in -1
        }

        _buffer.add(value.toByte())
        _buffer.add((value ushr 8).toByte())
        _buffer.add(0)
    }

    companion object {
        const val TOKEN_AT = 0xAC.toByte()
        const val TOKEN_INK = 0xD9.toByte()
        const val TOKEN_PAPER = 0xDA.toByte()
        const val TOKEN_FLASH = 0xDB.toByte()
        const val TOKEN_BRIGHT = 0xDC.toByte()
        const val TOKEN_INVERSE = 0xDD.toByte()
        const val TOKEN_OVER = 0xDE.toByte()
        const val TOKEN_BORDER = 0xE7.toByte()
        const val TOKEN_REM = 0xEA.toByte()
        const val TOKEN_GO_TO = 0xEC.toByte()
        const val TOKEN_PAUSE = 0xF2.toByte()
        const val TOKEN_PRINT = 0xF5.toByte()
        const val TOKEN_CLS = 0xFB.toByte()

        const val ATTR_INK = 0x10.toByte()
        const val ATTR_PAPER = 0x11.toByte()
        const val ATTR_FLASH = 0x12.toByte()
        const val ATTR_BRIGHT = 0x13.toByte()

        private const val NUMBER_MARKER = 0x0E.toByte()
    }
}

class BasicProgram {
    private val _buffer = mutableListOf<Byte>()

    val buffer: List<Byte>
        get() = _buffer

    fun appendLine(line: BasicLine) {
        // MSB
        _buffer.add((line.lineNumber ushr 8).toByte())
        _buffer.add(line.lineNumber.toByte())

        // LSB
        val dataSize = line.buffer.size + 1
        _buffer.add(dataSize.toByte())
        _buffer.add((dataSize ushr 8).toByte())

        _buffer.addAll(line.buffer)
        _buffer.add(CHAR_NEWLINE)
    }

    private companion object {
        private const val CHAR_NEWLINE: Byte = 0x0D
    }
}

class TapFile {
    private val _buffer = mutableListOf<Byte>()

    val buffer: List<Byte>
        get() = _buffer

    /**
     * Adds a program to the TAP file.
     *
     * @param name Program name (up to 10 characters).
     * @param data Program data as a list of bytes.
     * @param autoStartLine The line number from which the program will start automatically.
     */
    fun appendProgram(name: String, data: List<Byte>, autoStartLine: Int) {
        val header = buildHeader(
            type = TYPE_PROGRAM,
            name = name,
            dataSize = data.size,
            param1 = autoStartLine,
            param2 = data.size, // variable area
        )

        appendBlock(FLAG_HEADER, header)
        appendBlock(FLAG_DATA, data)
    }

    /**
     * Adds a number array to the TAP file.
     *
     * @param name Array name (up to 10 characters for ZX Spectrum).
     * @param data Array data as a list of bytes.
     * @param variableName Name of the variable into which the data will be loaded.
     */
    @Suppress("unused")
    fun appendNumberArray(name: String, data: List<Byte>, variableName: Char) {
        val header = buildHeader(
            type = TYPE_NUMBER_ARRAY,
            name = name,
            dataSize = data.size,
            param1 = (variableName.code % 256) shl 8,
            param2 = 0,
        )

        appendBlock(FLAG_HEADER, header)
        appendBlock(FLAG_DATA, data)
    }

    /**
     * Adds a character array to the TAP file.
     *
     * @param name Array name (up to 10 characters for ZX Spectrum).
     * @param data Array data as a list of bytes.
     * @param variableName Name of the variable into which the data will be loaded.
     */
    @Suppress("unused")
    fun appendCharacterArray(name: String, data: List<Byte>, variableName: Char) {
        val header = buildHeader(
            type = TYPE_CHARACTER_ARRAY,
            name = name,
            dataSize = data.size,
            param1 = (variableName.code % 256) shl 8,
            param2 = 0,
        )

        appendBlock(FLAG_HEADER, header)
        appendBlock(FLAG_DATA, data)
    }

    /**
     * Adds a block of data to the TAP file with a specified start address.
     *
     * @param name Block name (up to 10 characters for ZX Spectrum).
     * @param data Data as a list of bytes.
     * @param startAddress Memory address where the data will be loaded.
     */
    @Suppress("unused")
    fun appendBytes(name: String, data: List<Byte>, startAddress: Int) {
        val header = buildHeader(
            type = TYPE_BYTES,
            name = name,
            dataSize = data.size,
            param1 = startAddress,
            param2 = 32768,
        )

        appendBlock(FLAG_HEADER, header)
        appendBlock(FLAG_DATA, data)
    }

    private fun appendBlock(flag: Byte, data: List<Byte>) {
        val blockSize = data.size + 2

        _buffer.add(blockSize.toByte())
        _buffer.add((blockSize ushr 8).toByte())

        _buffer.add(flag)
        _buffer.addAll(data)

        _buffer.add(data.fold(flag) { acc, v -> acc xor v })
    }

    private fun buildHeader(type: Byte, name: String, dataSize: Int, param1: Int, param2: Int): List<Byte> {
        val header = mutableListOf<Byte>()
        header.add(type)

        val nameBytes = name.map { it.code.toByte() }.take(10)
        header.addAll(nameBytes)
        repeat(10 - nameBytes.size) { header.add(CHAR_SPACE) }

        header.add(dataSize.toByte())
        header.add((dataSize ushr 8).toByte())

        header.add(param1.toByte())
        header.add((param1 ushr 8).toByte())

        header.add(param2.toByte())
        header.add((param2 ushr 8).toByte())

        return header
    }

    private companion object {
        private const val TYPE_PROGRAM: Byte = 0
        private const val TYPE_NUMBER_ARRAY: Byte = 1
        private const val TYPE_CHARACTER_ARRAY: Byte = 2
        private const val TYPE_BYTES: Byte = 3

        private const val FLAG_HEADER: Byte = 0
        private const val FLAG_DATA = 255.toByte()

        private const val CHAR_SPACE = ' '.code.toByte()
    }
}
