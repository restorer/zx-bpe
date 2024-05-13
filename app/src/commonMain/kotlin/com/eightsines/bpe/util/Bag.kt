package com.eightsines.bpe.util

interface PackableBag {
    fun put(value: Boolean?)
    fun put(value: Int?)
    fun put(value: String?)
    fun <T> put(packer: BagStuffPacker<T>, value: T?)
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
