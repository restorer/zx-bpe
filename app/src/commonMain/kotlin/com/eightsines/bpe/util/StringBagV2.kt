package com.eightsines.bpe.util

private const val TYPE_NULL_VALUE = 0b0000001
private const val TYPE_NULL_BITS = 7

private const val TYPE_BOOLEAN_VALUE = 0b001
private const val TYPE_BOOLEAN_BITS = 3

private const val TYPE_INT4_VALUE = 0b1
private const val TYPE_INT4_BITS = 1

private const val TYPE_INT8_VALUE = 0b0001
private const val TYPE_INT8_BITS = 4

private const val TYPE_INT16_VALUE = 0b00001
private const val TYPE_INT16_BITS = 5

private const val TYPE_INT32_VALUE = 0b0000000110
private const val TYPE_INT32_BITS = 10

private const val TYPE_STUFF4_VALUE = 0b01
private const val TYPE_STUFF4_BITS = 2

private const val TYPE_STUFF8_VALUE = 0b000000001
private const val TYPE_STUFF8_BITS = 9

private const val TYPE_STUFF16_VALUE = 0b0000000111
private const val TYPE_STUFF16_BITS = 10

private const val TYPE_STUFF32_VALUE = 0b0000000101
private const val TYPE_STUFF32_BITS = 10

private const val TYPE_STRING4_VALUE = 0b000001
private const val TYPE_STRING4_BITS = 6

private const val TYPE_STRING8_VALUE = 0b000000000
private const val TYPE_STRING8_BITS = 9

private const val TYPE_STRING16_VALUE = 0b00000001001
private const val TYPE_STRING16_BITS = 11

private const val TYPE_STRING32_VALUE = 0b00000001000
private const val TYPE_STRING32_BITS = 11

internal class PackableStringBagV2 : PackableBag {
    private val bitstreamWriter = Base64BitstreamWriter(BAG_SIG_V2)

    override fun put(value: Boolean?) = writeNonNull(value) {
        bitstreamWriter.write(TYPE_BOOLEAN_VALUE, TYPE_BOOLEAN_BITS)
        bitstreamWriter.write(if (it) 1 else 0, 1)
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

    override fun put(value: String?) = writeNonNull(value) { value ->
        val encoded = value.encodeToByteArray()

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

internal class UnpackableStringBagV2(input: String) : UnpackableBag {
    private val bitstreamReader = Base64BitstreamReader(input, BAG_SIG_V2.length)

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
