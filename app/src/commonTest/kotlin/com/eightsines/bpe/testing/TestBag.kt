package com.eightsines.bpe.testing

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag

sealed interface TestWare {
    val type: String

    data object Null : TestWare {
        override val type = "Null"
    }

    data class BooleanWare(val value: Boolean) : TestWare {
        override val type = "Boolean"
    }

    data class IntWare(val value: Int) : TestWare {
        override val type = "Int"
    }

    data class StringWare(val value: String) : TestWare {
        override val type = "String"
    }

    data class StuffWare(val value: Int) : TestWare {
        override val type = "Stuff"
    }
}

class PackableTestBag : PackableBag {
    private val _wares = mutableListOf<TestWare>()

    val wares: List<TestWare>
        get() = _wares

    override fun put(value: Boolean?) = writeNonNull(value) {
        _wares.add(TestWare.BooleanWare(it))
    }

    override fun put(value: Int?) = writeNonNull(value) {
        _wares.add(TestWare.IntWare(it))
    }

    override fun put(value: String?) = writeNonNull(value) {
        _wares.add(TestWare.StringWare(it))
    }

    override fun <T> put(packer: BagStuffPacker<out T>, value: T?) = writeNonNull(value) {
        _wares.add(TestWare.StuffWare(packer.putInTheBagVersion))

        @Suppress("UNCHECKED_CAST")
        (packer as BagStuffPacker<T>).putInTheBag(this, it)
    }

    private inline fun <T> writeNonNull(value: T?, writer: (T) -> Unit) {
        if (value == null) {
            _wares.add(TestWare.Null)
        } else {
            writer(value)
        }
    }
}

class UnpackableTestBag(private val wares: List<TestWare>) : UnpackableBag {
    private val endIndex = wares.size - 1
    private var lastIndex: Int = -1

    override fun getBooleanOrNull(): Boolean? = readNullable(::readBoolean)
    override fun getIntOrNull(): Int? = readNullable(::readInt)
    override fun getStringOrNull(): String? = readNullable(::readString)
    override fun <T> getStuffOrNull(unpacker: BagStuffUnpacker<T>): T? = readNullable { readStuff(it, unpacker) }

    override fun getBoolean() = readNotNull("Boolean", ::readBoolean)
    override fun getInt() = readNotNull("Int", ::readInt)
    override fun getString() = readNotNull("String", ::readString)
    override fun <T> getStuff(unpacker: BagStuffUnpacker<T>) = readNotNull("Stuff") { readStuff(it, unpacker) }

    private fun readBoolean(ware: TestWare): Boolean = if (ware is TestWare.BooleanWare) {
        ware.value
    } else {
        throw BagUnpackException("Unexpected type=${ware.type} while reading Boolean at index=$lastIndex")
    }

    private fun readInt(ware: TestWare): Int = if (ware is TestWare.IntWare) {
        ware.value
    } else {
        throw BagUnpackException("Unexpected type=${ware.type} while reading Int at index=$lastIndex")
    }

    private fun readString(ware: TestWare): String = if (ware is TestWare.StringWare) {
        ware.value
    } else {
        throw BagUnpackException("Unexpected type=${ware.type} while reading String at index=$lastIndex")
    }

    private fun <T> readStuff(ware: TestWare, unpacker: BagStuffUnpacker<T>): T {
        if (ware !is TestWare.StuffWare) {
            throw BagUnpackException("Unexpected type=${ware.type} while reading Stuff at index=$lastIndex")
        }

        return unpacker.getOutOfTheBag(ware.value, this)
    }

    private fun readWare(): TestWare = if (lastIndex < endIndex) {
        wares[++lastIndex]
    } else {
        throw BagUnpackException("Unexpected end of the bag while reading at index=${lastIndex + 1}")
    }

    private inline fun <T> readNullable(reader: (type: TestWare) -> T): T? {
        val ware = readWare()
        return if (ware is TestWare.Null) null else reader(ware)
    }

    private inline fun <T> readNotNull(kind: String, reader: (type: TestWare) -> T): T {
        val ware = readWare()

        if (ware is TestWare.Null) {
            throw BagUnpackException("Unexpected null-value while reading $kind at index=$lastIndex")
        }

        return reader(ware)
    }
}
