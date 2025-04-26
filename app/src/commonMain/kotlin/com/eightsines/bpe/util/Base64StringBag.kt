package com.eightsines.bpe.util

private const val BITS_PER_CHAR = 6
private const val PADDING_CHAR = '='

@Suppress("SpellCheckingInspection")
private val TO_BASE64: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()

private val FROM_BASE64: Map<Char, Int> = buildMap {
    TO_BASE64.forEachIndexed { index, char -> put(char, index) }
}

private const val TYPE_BOOLEAN_VALUE = 0b100
private const val TYPE_BOOLEAN_BITS = 3

private const val TYPE_INT4_VALUE = 0b00
private const val TYPE_INT4_BITS = 2

private const val TYPE_INT8_VALUE = 0b10100
private const val TYPE_INT8_BITS = 5

private const val TYPE_INT16_VALUE = 0b10101
private const val TYPE_INT16_BITS = 5

private const val TYPE_INT32_VALUE = 0b11110
private const val TYPE_INT32_BITS = 5

private const val TYPE_STUFF4_VALUE = 0b01
private const val TYPE_STUFF4_BITS = 2

private const val TYPE_STUFF8_VALUE = 0b10110
private const val TYPE_STUFF8_BITS = 5

private const val TYPE_STUFF16_VALUE = 0b10111
private const val TYPE_STUFF16_BITS = 5

private const val TYPE_STUFF32_VALUE = 0b11111
private const val TYPE_STUFF32_BITS = 5

private const val TYPE_STRING4_VALUE = 0b11000
private const val TYPE_STRING4_BITS = 5

private const val TYPE_STRING8_VALUE = 0b11001
private const val TYPE_STRING8_BITS = 5

private const val TYPE_STRING16_VALUE = 0b11010
private const val TYPE_STRING16_BITS = 5

private const val TYPE_STRING32_VALUE = 0b11011
private const val TYPE_STRING32_BITS = 5

private const val TYPE_NULL_VALUE = 0b1110
private const val TYPE_NULL_BITS = 4

internal class BitstreamBase64Writer(private val prefix: String) {
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

internal class BitstreamBase64Reader(private val input: String, startIndex: Int) {
    var currentIndex = startIndex
        private set

    private var bufferValue = 0
    private var bufferBits = 0

    fun peek(bits: Int): Int {
        require(bits >= 1 && bits <= BITS_PER_CHAR) { "Invalid number of peek bits=$bits" }

        if (bufferBits < bits) {
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
        require(bits >= 1 && bits <= BITS_PER_CHAR) { "Invalid number of consume bits=$bits" }

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

internal class PackableBase64StringBag : PackableBag {
    private val bitstreamWriter = BitstreamBase64Writer(BAG_SIG_V2)

    override fun put(value: Boolean?) = writeNonNull(value) {
        bitstreamWriter.write(if (it) 0b1001 else 0b1000, 4)
    }

    override fun put(value: Int?) = writeNonNull(value) {
        writeNumber(
            TYPE_INT4_VALUE,
            TYPE_INT4_BITS,
            TYPE_INT8_VALUE,
            TYPE_INT8_BITS,
            TYPE_INT16_VALUE,
            TYPE_INT16_BITS,
            TYPE_INT32_VALUE,
            TYPE_INT32_BITS,
            it,
        )
    }

    override fun put(value: String?) = writeNonNull(value) {
        val encoded = it.encodeToByteArray()

        writeNumber(
            TYPE_STRING4_VALUE,
            TYPE_STRING4_BITS,
            TYPE_STRING8_VALUE,
            TYPE_STRING8_BITS,
            TYPE_STRING16_VALUE,
            TYPE_STRING16_BITS,
            TYPE_STRING32_VALUE,
            TYPE_STRING32_BITS,
            encoded.size,
        )

        encoded.forEach { bitstreamWriter.write(it.toInt(), 8) }
    }

    override fun <T> put(packer: BagStuffPacker<out T>, value: T?) = writeNonNull(value) {
        writeNumber(
            TYPE_STUFF4_VALUE,
            TYPE_STUFF4_BITS,
            TYPE_STUFF8_VALUE,
            TYPE_STUFF8_BITS,
            TYPE_STUFF16_VALUE,
            TYPE_STUFF16_BITS,
            TYPE_STUFF32_VALUE,
            TYPE_STUFF32_BITS,
            packer.putInTheBagVersion,
        )

        @Suppress("UNCHECKED_CAST")
        (packer as BagStuffPacker<T>).putInTheBag(this, it)
    }

    override fun toString() = bitstreamWriter.toString()

    private inline fun <T> writeNonNull(value: T?, writer: (T) -> Unit) {
        if (value == null) {
            bitstreamWriter.write(TYPE_NULL_VALUE, TYPE_NULL_BITS)
        } else {
            writer(value)
        }
    }

    private fun writeNumber(
        type4Value: Int,
        type4Bits: Int,
        type8Value: Int,
        type8Bits: Int,
        type16Value: Int,
        type16Bits: Int,
        type32Value: Int,
        type32Bits: Int,
        value: Int,
    ) = when (value) {
        in -8..7 -> {
            bitstreamWriter.write(type4Value, type4Bits)
            bitstreamWriter.write(value, 4)
        }

        in Byte.MIN_VALUE..Byte.MAX_VALUE -> {
            bitstreamWriter.write(type8Value, type8Bits)
            bitstreamWriter.write(value, 8)
        }

        in Short.MIN_VALUE..Short.MAX_VALUE -> {
            bitstreamWriter.write(type16Value, type16Bits)
            bitstreamWriter.write(value, 16)
        }

        else -> {
            bitstreamWriter.write(type32Value, type32Bits)
            bitstreamWriter.write(value, 32)
        }
    }
}

internal class UnpackableBase64StringBag(input: String) : UnpackableBag {
    private val bitstreamReader = BitstreamBase64Reader(input, BAG_SIG_V2.length)

    init {
        if (!input.startsWith(BAG_SIG_V2)) {
            throw BagUnpackException("Missing \"$BAG_SIG_V2\" signature")
        }
    }

    override fun getBooleanOrNull(): Boolean? = readNullable(::readBoolean)
    override fun getIntOrNull(): Int? = readNullable(::readInt)
    override fun getStringOrNull(): String? = readNullable(::readString)
    override fun <T> getStuffOrNull(unpacker: BagStuffUnpacker<T>): T? = readNullable { readStuff(unpacker) }

    override fun getBoolean() = readNotNull("Boolean", ::readBoolean)
    override fun getInt() = readNotNull("Int", ::readInt)
    override fun getString() = readNotNull("String", ::readString)
    override fun <T> getStuff(unpacker: BagStuffUnpacker<T>) = readNotNull("Stuff") { readStuff(unpacker) }

    private fun readBoolean(): Boolean {
        if (bitstreamReader.read(TYPE_BOOLEAN_BITS) != TYPE_BOOLEAN_VALUE) {
            throw BagUnpackException("Unexpected type while reading Boolean at index=${bitstreamReader.currentIndex}")
        }

        return bitstreamReader.read(1) == 1
    }

    private fun readInt() = readNumber(
        "Int",
        TYPE_INT4_VALUE,
        TYPE_INT4_BITS,
        TYPE_INT8_VALUE,
        TYPE_INT8_BITS,
        TYPE_INT16_VALUE,
        TYPE_INT16_BITS,
        TYPE_INT32_VALUE,
        TYPE_INT32_BITS,
    )

    private fun readString(): String {
        val encodedSize = readNumber(
            "String",
            TYPE_STRING4_VALUE,
            TYPE_STRING4_BITS,
            TYPE_STRING8_VALUE,
            TYPE_STRING8_BITS,
            TYPE_STRING16_VALUE,
            TYPE_STRING16_BITS,
            TYPE_STRING32_VALUE,
            TYPE_STRING32_BITS,
        )

        return ByteArray(encodedSize) { bitstreamReader.read(8).toByte() }.decodeToString()
    }

    private fun <T> readStuff(unpacker: BagStuffUnpacker<T>): T {
        val version = readNumber(
            "Stuff",
            TYPE_STUFF4_VALUE,
            TYPE_STUFF4_BITS,
            TYPE_STUFF8_VALUE,
            TYPE_STUFF8_BITS,
            TYPE_STUFF16_VALUE,
            TYPE_STUFF16_BITS,
            TYPE_STUFF32_VALUE,
            TYPE_STUFF32_BITS,
        )

        return unpacker.getOutOfTheBag(version, this)
    }

    private fun readNumber(
        kind: String,
        type4Value: Int,
        type4Bits: Int,
        type8Value: Int,
        type8Bits: Int,
        type16Value: Int,
        type16Bits: Int,
        type32Value: Int,
        type32Bits: Int,
    ) = when {
        bitstreamReader.peek(type4Bits) == type4Value -> {
            bitstreamReader.consume(type4Bits)
            val result = bitstreamReader.read(4)
            if (result > 7) result - 16 else result
        }

        bitstreamReader.peek(type8Bits) == type8Value -> {
            bitstreamReader.consume(type8Bits)
            val result = bitstreamReader.read(8)
            if (result > Byte.MAX_VALUE) result - 256 else result
        }

        bitstreamReader.peek(type16Bits) == type16Value -> {
            bitstreamReader.consume(type16Bits)
            val result = bitstreamReader.read(16)
            if (result > Short.MAX_VALUE) result - 65536 else result
        }

        bitstreamReader.peek(type32Bits) == type32Value -> {
            bitstreamReader.consume(type32Bits)
            bitstreamReader.read(32)
        }

        else -> throw BagUnpackException("Unexpected type while reading $kind at index=${bitstreamReader.currentIndex}")
    }

    private inline fun <T> readNullable(reader: () -> T): T? {
        return if (bitstreamReader.peek(TYPE_NULL_BITS) == TYPE_NULL_VALUE) {
            bitstreamReader.consume(TYPE_NULL_BITS)
            null
        } else {
            reader()
        }
    }

    private inline fun <T> readNotNull(kind: String, reader: () -> T): T {
        if (bitstreamReader.peek(TYPE_NULL_BITS) == TYPE_NULL_VALUE) {
            throw BagUnpackException("Unexpected null-value while reading $kind at index=${bitstreamReader.currentIndex}")
        }

        return reader()
    }
}
