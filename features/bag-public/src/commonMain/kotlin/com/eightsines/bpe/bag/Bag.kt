package com.eightsines.bpe.bag

interface PackableBag {
    fun put(value: Boolean?)
    fun put(value: Int?)
    fun put(value: String?)
    fun <T> put(packer: BagStuffPacker<out T>, value: T?)

    fun <T> putList(list: List<T>, packer: (T) -> Unit) {
        put(list.size)

        for (item in list) {
            packer(item)
        }
    }
}

interface UnpackableBag {
    fun getBooleanOrNull(): Boolean?
    fun getIntOrNull(): Int?
    fun getStringOrNull(): String?
    fun <T> getStuffOrNull(unpacker: BagStuffUnpacker<T>): T?

    fun getBoolean(): Boolean
    fun getInt(): Int
    fun getString(): String
    fun <T> getStuff(unpacker: BagStuffUnpacker<in T>): T

    fun <T> getList(unpacker: () -> T): MutableList<T> {
        val size = getInt()
        return (0..<size).mapTo(mutableListOf()) { unpacker() }
    }
}

interface BagStuffPacker<T> {
    val putInTheBagVersion: Int
    fun putInTheBag(bag: PackableBag, value: T)
}

interface BagStuffUnpacker<T> {
    fun getOutOfTheBag(version: Int, bag: UnpackableBag): T
}

open class BagUnpackException(message: String) : RuntimeException(message)

class UnsupportedVersionBagUnpackException(kind: String, version: Int) :
    BagUnpackException("Unsupported version=$version for $kind")

class UnknownPolymorphicTypeBagUnpackException(kind: String, type: Int) :
    BagUnpackException("Unknown polymorphic type=$type for $kind")

@Suppress("NOTHING_TO_INLINE")
inline fun requireSupportedStuffVersion(kind: String, maxSupportedVersion: Int, version: Int) {
    if (version > maxSupportedVersion) {
        throw UnsupportedVersionBagUnpackException(kind, version)
    }
}

inline fun <T> requireNoIllegalArgumentException(block: () -> T) = try {
    block()
} catch (e: IllegalArgumentException) {
    throw BagUnpackException(e.toString())
}
