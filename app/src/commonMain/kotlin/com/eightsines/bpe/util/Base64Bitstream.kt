package com.eightsines.bpe.util

private const val BITS_PER_CHAR = 6
private const val PADDING_CHAR = '='

@Suppress("SpellCheckingInspection")
private val TO_BASE64: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()

private val FROM_BASE64: Map<Char, Int> = buildMap {
    TO_BASE64.forEachIndexed { index, char -> put(char, index) }
}

internal class Base64BitstreamWriter(prefix: String) {
    private val buffer = StringBuilder(prefix)
    private var bufferValue = 0
    private var bufferBits = 0
    private var bitsWrittenTail = 0

    fun write(value: Int, bits: Int) {
        when {
            bits < 0 || bits > 32 -> throw IllegalArgumentException("Invalid number of write bits=$bits")
            bits == 0 -> return
        }

        var bits = bits

        while (bufferBits + bits > BITS_PER_CHAR) {
            val currentBits = BITS_PER_CHAR - bufferBits
            val currentMask = (1 shl currentBits) - 1
            val currentValue = value shr (bits - currentBits)

            buffer.append(TO_BASE64[(bufferValue shl currentBits) or (currentValue and currentMask)])
            bitsWrittenTail = (bitsWrittenTail + BITS_PER_CHAR) % 24
            bufferValue = 0
            bufferBits = 0

            bits -= currentBits
        }

        val mask = (1 shl bits) - 1

        if (bufferBits + bits == BITS_PER_CHAR) {
            buffer.append(TO_BASE64[(bufferValue shl bits) or (value and mask)])
            bitsWrittenTail = (bitsWrittenTail + BITS_PER_CHAR) % 24
            bufferValue = 0
            bufferBits = 0
        } else {
            bufferValue = (bufferValue shl bits) or (value and mask)
            bufferBits += bits
        }
    }

    override fun toString() = buildString {
        append(buffer)

        if (bufferBits > 0) {
            append(TO_BASE64[bufferValue shl (BITS_PER_CHAR - bufferBits)])
            bitsWrittenTail = (bitsWrittenTail + bufferBits) % 24
        }

//        val bytesWrittenTail = (bitsWrittenTail + 7) / 8
//        val bytesExpectedTail = ((bitsWrittenTail + 5) / 6 * 6 + 7) / 8
//
//        val charsWrittenTail = (bitsWrittenTail + 5) / 6
//        val charsExpectedTail = ((bitsWrittenTail + 7) / 8 * 8 + 5) / 6

        when {
            bitsWrittenTail == 0 -> Unit

            bitsWrittenTail <= 6 -> {
                append(TO_BASE64[0])
                append(PADDING_CHAR)
                append(PADDING_CHAR)
            }

            bitsWrittenTail <= 8 -> {
                append(PADDING_CHAR)
                append(PADDING_CHAR)
            }

            bitsWrittenTail <= 12 -> {
                append(TO_BASE64[0])
                append(PADDING_CHAR)
            }

            bitsWrittenTail <= 16 -> append(PADDING_CHAR)
            bitsWrittenTail <= 18 -> append(TO_BASE64[0])
        }
    }
}

internal class Base64BitstreamReader(private val input: String, startIndex: Int) {
    var currentIndex = startIndex
        private set

    private var bufferValue = 0
    private var bufferBits = 0

    fun peek(bits: Int): Int {
        require(bits >= 1 && bits <= 16) { "Invalid number of peek bits=$bits" }

        while (bufferBits < bits) {
            if (currentIndex >= input.length) {
                return -1
            }

            val currentChar = input[currentIndex]

            if (currentChar == PADDING_CHAR) {
                return -1
            }

            val currentValue = FROM_BASE64[currentChar]
                ?: throw BagUnpackException("Can't decode base64=\"$currentChar\" at index=$currentIndex")

            ++currentIndex

            bufferValue = (bufferValue shl BITS_PER_CHAR) or currentValue
            bufferBits += BITS_PER_CHAR
        }

        return bufferValue shr (bufferBits - bits)
    }

    fun consume(bits: Int) {
        require(bits >= 1 && bits <= 16) { "Invalid number of consume bits=$bits" }

        bufferValue = bufferValue and ((1 shl (bufferBits - bits)) - 1)
        bufferBits -= bits
    }

    fun read(bits: Int): Int {
        when {
            bits < 0 || bits > 32 -> throw IllegalArgumentException("Invalid number of read bits=$bits")
            bits == 0 -> return 0
        }

        var bits = bits
        var result = 0

        while (true) {
            val currentBits = if (bufferBits >= bits) {
                bits
            } else {
                BITS_PER_CHAR
            }

            if (bufferBits >= currentBits) {
                val shiftBits = bufferBits - currentBits

                result = (result shl currentBits) or (bufferValue shr shiftBits)
                bits -= currentBits

                bufferValue = bufferValue and ((1 shl shiftBits) - 1)
                bufferBits -= currentBits

                if (bits == 0) {
                    return result
                }
            }

            if (currentIndex >= input.length) {
                throw BagUnpackException("Unexpected end of the bag while reading at index=$currentIndex")
            }

            val currentChar = input[currentIndex]

            if (currentChar == PADDING_CHAR) {
                throw BagUnpackException("Unexpected end of the bag while reading at index=$currentIndex")
            }

            val currentValue = FROM_BASE64[currentChar]
                ?: throw BagUnpackException("Can't decode base64=\"$currentChar\" at index=$currentIndex")

            ++currentIndex

            bufferValue = (bufferValue shl BITS_PER_CHAR) or currentValue
            bufferBits += BITS_PER_CHAR
        }
    }
}
