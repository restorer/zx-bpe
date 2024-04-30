package com.eightsines.bpe.engine

sealed class Layer(val isVisible: Boolean = true, val isLocked: Boolean = false) {
    data class Scii(
        val canvas: List<List<Cell.Scii>> = MutableList(HEIGHT) { MutableList(WIDTH) { Cell.Scii.Transparent } },
    ) : Layer() {
        companion object {
            const val WIDTH = 32
            const val HEIGHT = 24
        }
    }

    data class HBlock(
        val canvas: List<List<Cell.Block>> = MutableList(HEIGHT) { MutableList(WIDTH) { Cell.Block.Transparent } },
    ) : Layer() {
        companion object {
            const val WIDTH = Scii.WIDTH
            const val HEIGHT = Scii.HEIGHT * 2
        }
    }

    data class VBlock(
        val canvas: List<List<Cell.Block>> = MutableList(HEIGHT) { MutableList(WIDTH) { Cell.Block.Transparent } },
    ) : Layer() {
        companion object {
            const val WIDTH = Scii.WIDTH * 2
            const val HEIGHT = Scii.HEIGHT
        }
    }

    data class QBlock(
        val canvas: List<List<Cell.Block>> = MutableList(HEIGHT) { MutableList(WIDTH) { Cell.Block.Transparent } },
    ) : Layer() {
        companion object {
            const val WIDTH = Scii.WIDTH * 2
            const val HEIGHT = Scii.HEIGHT * 2
        }
    }
}
