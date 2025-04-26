package com.eightsines.bpe.util

internal const val BAG_SIG_V1 = "BAG1"
internal const val BAG_SIG_V2 = "BAG2"

@Suppress("FunctionName")
fun PackableStringBag(): PackableBag = PackableBase64StringBag()

@Suppress("FunctionName")
fun UnpackableStringBag(input: String): UnpackableBag = when {
    input.startsWith(BAG_SIG_V1) -> UnpackableSimpleStringBag(input)
    input.startsWith(BAG_SIG_V2) -> UnpackableBase64StringBag(input)
    else -> throw BagUnpackException("Missing bag signature")
}
