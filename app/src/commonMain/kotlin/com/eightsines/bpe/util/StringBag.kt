package com.eightsines.bpe.util

internal const val BAG_SIG_V1 = "BAG1"
internal const val BAG_SIG_V2 = "BAG2"

@Suppress("FunctionName")
fun PackableStringBag(): PackableBag = PackableSimpleStringBag()

@Suppress("FunctionName")
fun UnpackableStringBag(input: String): UnpackableBag = UnpackableSimpleStringBag(input)
