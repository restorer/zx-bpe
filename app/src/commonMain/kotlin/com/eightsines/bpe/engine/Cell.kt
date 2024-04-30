package com.eightsines.bpe.engine

sealed interface Cell {
    data class Block(val color: SciiColor, val bright: SciiLight) : Cell {
        companion object {
            val Transparent = Block(color = SciiColor.Transparent, bright = SciiLight.Transparent)
        }
    }

    data class Scii(
        val character: SciiChar,
        val ink: SciiColor,
        val paper: SciiColor,
        val bright: SciiLight,
        val flash: SciiLight,
    ) : Cell {
        companion object {
            val Transparent = Scii(
                character = SciiChar.Transparent,
                ink = SciiColor.Transparent,
                paper = SciiColor.Transparent,
                bright = SciiLight.Transparent,
                flash = SciiLight.Transparent,
            )
        }
    }
}
