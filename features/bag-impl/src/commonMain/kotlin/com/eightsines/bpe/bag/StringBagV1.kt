package com.eightsines.bpe.bag

private const val TYPE_NULL = '_'
private const val TYPE_BOOLEAN_FALSE = 'b'
private const val TYPE_BOOLEAN_TRUE = 'B'
private const val TYPE_INT_4 = 'i'
private const val TYPE_INT_8 = 'I'
private const val TYPE_INT_16 = 'n'
private const val TYPE_INT_32 = 'N'
private const val TYPE_STRING_4 = 's'
private const val TYPE_STRING_8 = 'S'
private const val TYPE_STRING_16 = 't'
private const val TYPE_STRING_32 = 'T'
private const val TYPE_STUFF_4 = 'u'
private const val TYPE_STUFF_8 = 'U'
private const val TYPE_STUFF_16 = 'f'
private const val TYPE_STUFF_32 = 'F'

private val FROM_HEX = buildMap {
    put('0', 0)
    put('1', 1)
    put('2', 2)
    put('3', 3)
    put('4', 4)
    put('5', 5)
    put('6', 6)
    put('7', 7)
    put('8', 8)
    put('9', 9)
    put('A', 10)
    put('B', 11)
    put('C', 12)
    put('D', 13)
    put('E', 14)
    put('F', 15)
}

internal class UnpackableStringBagV1(private val input: String) : UnpackableBag {
    private val endIndex = input.length - 1
    private var lastIndex: Int = BAG_SIG_V1.length - 1

    init {
        if (!input.startsWith(BAG_SIG_V1)) {
            throw BagUnpackException("Missing \"$BAG_SIG_V1\" signature")
        }
    }

    override fun getBooleanOrNull(): Boolean? = readNullable(::readBoolean)
    override fun getIntOrNull(): Int? = readNullable(::readInt)
    override fun getStringOrNull(): String? = readNullable(::readString)
    override fun <T> getStuffOrNull(unpacker: BagStuffUnpacker<T>): T? = readNullable { readStuff(it, unpacker) }

    override fun getBoolean() = readNotNull("Boolean", ::readBoolean)
    override fun getInt() = readNotNull("Int", ::readInt)
    override fun getString() = readNotNull("String", ::readString)
    override fun <T> getStuff(unpacker: BagStuffUnpacker<in T>) = readNotNull("Stuff") { readStuff(it, unpacker) }

    private fun readBoolean(type: Char): Boolean = when (type) {
        TYPE_BOOLEAN_FALSE -> false
        TYPE_BOOLEAN_TRUE -> true
        else -> throw BagUnpackException("Unexpected type=$type while reading Boolean at index=$lastIndex")
    }

    private fun readInt(type: Char) = readNumber("Int", TYPE_INT_4, TYPE_INT_8, TYPE_INT_16, TYPE_INT_32, type)

    private fun readString(type: Char): String {
        val length = readNumber("Int", TYPE_STRING_4, TYPE_STRING_8, TYPE_STRING_16, TYPE_STRING_32, type)

        return when {
            length == 0 -> ""

            lastIndex + length <= endIndex ->
                input.substring(lastIndex + 1, lastIndex + length + 1).also { lastIndex += length }

            else ->
                throw BagUnpackException("Unexpected end of the bag while reading at index=${input.length}")
        }
    }

    private fun <T> readStuff(type: Char, unpacker: BagStuffUnpacker<in T>): T {
        val version = readNumber("Stuff", TYPE_STUFF_4, TYPE_STUFF_8, TYPE_STUFF_16, TYPE_STUFF_32, type)

        @Suppress("UNCHECKED_CAST")
        return unpacker.getOutOfTheBag(version, this) as T
    }

    private fun readChar(): Char = if (lastIndex < endIndex) {
        input[++lastIndex]
    } else {
        throw BagUnpackException("Unexpected end of the bag while reading at index=${lastIndex + 1}")
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun readHexDigit(kind: String): Int {
        val hex = readChar()
        return FROM_HEX[hex] ?: throw BagUnpackException("Can't decode hex=\"$hex\" reading $kind at index=$lastIndex")
    }

    private fun readNumber(kind: String, type1: Char, type2: Char, type3: Char, type4: Char, type: Char): Int =
        when (type) {
            type1 -> {
                val result = readHexDigit(kind)
                if (result > 7) result - 16 else result
            }

            type2 -> {
                val result = (readHexDigit(kind) shl 4) or readHexDigit(kind)
                if (result > Byte.MAX_VALUE) result - 256 else result
            }

            type3 -> {
                val result = (readHexDigit(kind) shl 12) or
                        (readHexDigit(kind) shl 8) or
                        (readHexDigit(kind) shl 4) or
                        readHexDigit(kind)

                if (result > Short.MAX_VALUE) result - 65536 else result
            }

            type4 -> (readHexDigit(kind) shl 28) or
                    (readHexDigit(kind) shl 24) or
                    (readHexDigit(kind) shl 20) or
                    (readHexDigit(kind) shl 16) or
                    (readHexDigit(kind) shl 12) or
                    (readHexDigit(kind) shl 8) or
                    (readHexDigit(kind) shl 4) or
                    readHexDigit(kind)

            else -> throw BagUnpackException("Unexpected type=$type while reading $kind at index=$lastIndex")
        }

    private inline fun <T> readNullable(reader: (type: Char) -> T): T? {
        val type = readChar()
        return if (type == TYPE_NULL) null else reader(type)
    }

    private inline fun <T> readNotNull(kind: String, reader: (type: Char) -> T): T {
        val type = readChar()

        if (type == TYPE_NULL) {
            throw BagUnpackException("Unexpected null-value while reading $kind at index=$lastIndex")
        }

        return reader(type)
    }
}
