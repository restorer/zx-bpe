package com.eightsines.bpe.engine.data

data class SciiCell(
    val character: SciiChar,
    val ink: SciiColor,
    val paper: SciiColor,
    val bright: SciiLight,
    val flash: SciiLight,
) {
    fun merge(onto: SciiCell) = SciiCell(
        character = character.merge(onto.character),
        ink = ink.merge(onto.ink),
        paper = paper.merge(onto.paper),
        bright = bright.merge(onto.bright),
        flash = flash.merge(onto.flash),
    )

    companion object {
        val Transparent = SciiCell(
            character = SciiChar.Transparent,
            ink = SciiColor.Transparent,
            paper = SciiColor.Transparent,
            bright = SciiLight.Transparent,
            flash = SciiLight.Transparent,
        )
    }
}
