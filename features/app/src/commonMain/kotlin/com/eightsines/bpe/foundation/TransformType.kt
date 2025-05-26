package com.eightsines.bpe.foundation

enum class TransformType(val value: Int) {
    FlipHorizontal(1),
    FlipVertical(2),
    RotateCw(3),
    RotateCcw(4);

    fun inverse() = when (this) {
        FlipHorizontal -> FlipHorizontal
        FlipVertical -> FlipVertical
        RotateCw -> RotateCcw
        RotateCcw -> RotateCw
    }

    companion object {
        fun of(value: Int) = when (value) {
            FlipHorizontal.value -> FlipHorizontal
            FlipVertical.value -> FlipVertical
            RotateCw.value -> RotateCw
            RotateCcw.value -> RotateCcw
            else -> throw IllegalArgumentException("Unknown enum value=$value for TransformType")
        }
    }
}
