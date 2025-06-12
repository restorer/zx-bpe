package com.eightsines.bpe.presentation

enum class BpePaintingMode(val value: Int) {
    Edge(1),
    Center(2);

    companion object {
        fun of(value: Int) = when (value) {
            Edge.value -> Edge
            Center.value -> Center
            else -> throw IllegalArgumentException("Unknown enum value=$value for BpePaintingMode")
        }
    }
}
