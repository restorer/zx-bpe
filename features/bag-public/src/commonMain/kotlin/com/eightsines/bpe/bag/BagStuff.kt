package com.eightsines.bpe.bag

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BagStuff(val packer: String = "", val unpacker: String = "")

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BagStuffWare(val index: Int, val packer: String = "", val unpacker: String = "", val version: Int = 1)
