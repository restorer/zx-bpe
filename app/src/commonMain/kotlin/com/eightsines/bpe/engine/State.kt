package com.eightsines.bpe.engine

data class State(
    val border: SciiColor,
    val background: Cell.Block,
    val palette: Palette,
    val mergedLayers: Layer.Scii,
    val layers: List<Layer> = emptyList(),
    val selectedLayer: Layer? = null,
)
