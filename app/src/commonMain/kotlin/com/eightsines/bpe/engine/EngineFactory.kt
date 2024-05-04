package com.eightsines.bpe.engine

/*
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

class EngineFactory(private val uidFactory: UidFactory, private val defaults: Defaults) {
    fun createEmpty() = Engine(
        uidFactory = uidFactory,
        toolState = EngineState.Tool(
            ink = defaults.ink,
            paper = defaults.paper,
            bright = defaults.bright,
            flash = defaults.flash,
            character = defaults.character,
        ),
        graphicsState = EngineState.Graphics(
            border = defaults.border,
            background = BackgroundLayer(
                isVisible = true,
                color = defaults.backgroundColor,
                bright = defaults.backgroundBright,
            ),
        )
    )

    data class Defaults(
        val ink: SciiColor = SciiColor.White,
        val paper: SciiColor = SciiColor.Transparent,
        val bright: SciiLight = SciiLight.Transparent,
        val flash: SciiLight = SciiLight.Transparent,
        val character: SciiChar = SciiChar.Transparent,

        val border: SciiColor = SciiColor.Black,
        val backgroundColor: SciiColor = SciiColor.Black,
        val backgroundBright: SciiLight = SciiLight.Off,
    )
}
*/
