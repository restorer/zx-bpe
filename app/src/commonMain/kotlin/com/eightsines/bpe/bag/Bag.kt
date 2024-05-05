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
    fun <T> getStuffOrNull(unpacker: BagStuff.Unpacker<T>): T?

    fun getBoolean(): Boolean
    fun getInt(): Int
    fun getString(): String
    fun <T> getStuff(unpacker: BagStuff.Unpacker<T>): T
}

interface BagStuff {
    val bagStuffVersion: Int

    fun putInTheBag(bag: PackableBag)

    interface Unpacker<T> {
        fun getOutOfTheBag(version: Int, bag: UnpackableBag): T
    }
}

class BagUnpackException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
