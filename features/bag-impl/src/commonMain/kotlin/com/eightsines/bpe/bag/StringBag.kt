package com.eightsines.bpe.bag

internal const val BAG_SIG_V1 = "BAG1"
internal const val BAG_SIG_V2 = "BAG2"

@Suppress("FunctionName")
fun PackableStringBag(): PackableBag = PackableStringBagV2()

@Suppress("FunctionName")
fun UnpackableStringBag(input: String): UnpackableBag = when {
    input.startsWith(BAG_SIG_V1) -> UnpackableStringBagV1(input)
    input.startsWith(BAG_SIG_V2) -> UnpackableStringBagV2(input)
    else -> throw BagUnpackException("Missing bag signature")
}
