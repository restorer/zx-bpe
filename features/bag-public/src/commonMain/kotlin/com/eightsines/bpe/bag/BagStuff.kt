package com.eightsines.bpe.bag

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BagStuff(val staffPacker: String = "", val staffUnpacker: String = "", val suffix: String = "Stuff")

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BagStuffWare(val index: Int, val fieldPacker: String = "", val fieldUnpacker: String = "", val version: Int = 1)
