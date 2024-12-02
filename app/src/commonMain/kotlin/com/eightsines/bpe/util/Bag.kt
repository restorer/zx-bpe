package com.eightsines.bpe.util

interface PackableBag {
    fun put(value: Boolean?)
    fun put(value: Int?)
    fun put(value: String?)
    fun <T> put(packer: BagStuffPacker<out T>, value: T?)
}

fun <T> PackableBag.putList(list: List<T>, packer: (T) -> Unit) {
    put(list.size)

    for (item in list) {
        packer(item)
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
    fun <T> getStuff(unpacker: BagStuffUnpacker<T>): T
}

fun <T> UnpackableBag.getList(unpacker: () -> T): MutableList<T> {
    val size = getInt()
    return (0..<size).mapTo(mutableListOf()) { unpacker() }
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <R, U> UnpackableBag.getStuffOrNullTyped(unpacker: BagStuffUnpacker<U>): R? = getStuffOrNull(unpacker)?.let { it as R }

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <R, U> UnpackableBag.getStuffTyped(unpacker: BagStuffUnpacker<U>): R = getStuff(unpacker) as R

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

class UnknownPolymorphicTypeBagUnpackException : BagUnpackException {
    constructor(kind: String, type: Int) : super("Unknown polymorphic type=$type for $kind")
    constructor(kind: String, type: String) : super("Unknown polymorphic type=\"$type\" for $kind")
}

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
