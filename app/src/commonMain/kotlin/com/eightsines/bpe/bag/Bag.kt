package com.eightsines.bpe.bag

interface PackableBag {
    fun put(value: Boolean?)
    fun put(value: Int?)
    fun put(value: String?)
    fun put(value: BagStuff?)
}

interface UnpackableBag {
    fun getBooleanOrNull(): Boolean?
    fun getIntOrNull(): Int?
    fun getStringOrNull(): String?
    fun <T> getStuffOrNull(unpacker: VersionedUnpackableBag): T?

    fun getBoolean(): Boolean
    fun getInt(): Int
    fun getString(): String
    fun <T> getStuff(unpacker: (bag: VersionedUnpackableBag) -> T): T
}

interface VersionedPackableBag {
    fun putVersion(version: Int): PackableBag
}

interface VersionedUnpackableBag {
    fun getVersion(): Pair<Int, UnpackableBag>
}

interface BagStuff {
    fun putInTheBag(versionedBag: VersionedPackableBag)

    interface Unpacker<T> {
        fun getOutOfTheBag(versionedBag: VersionedUnpackableBag): T
    }
}

class BagUnpackException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
