package com.eightsines.bpe.foundation

object Merger {
    fun mergeSciiCell(which: SciiCell, onto: SciiCell): SciiCell {
        val charValue = which.character.value
        val ontoCharValue = onto.character.value

        return when {
            charValue == SciiChar.ForceTransparent.value -> SciiCell.Transparent
            charValue == SciiChar.Transparent.value && ontoCharValue == SciiChar.Transparent.value -> SciiCell.Transparent

            charValue in SciiChar.BLOCK_VALUE_FIRST..SciiChar.BLOCK_VALUE_LAST &&
                    ontoCharValue in SciiChar.BLOCK_VALUE_FIRST..SciiChar.BLOCK_VALUE_LAST -> {

                val ontoInk = onto.ink
                val ontoPaper = onto.paper

                val trColor = if ((charValue and SciiChar.BLOCK_BIT_TR) != 0) which.ink else which.paper
                val tlColor = if ((charValue and SciiChar.BLOCK_BIT_TL) != 0) which.ink else which.paper
                val brColor = if ((charValue and SciiChar.BLOCK_BIT_BR) != 0) which.ink else which.paper
                val blColor = if ((charValue and SciiChar.BLOCK_BIT_BL) != 0) which.ink else which.paper

                val ontoTrColor = if ((ontoCharValue and SciiChar.BLOCK_BIT_TR) != 0) ontoInk else ontoPaper
                val ontoTlColor = if ((ontoCharValue and SciiChar.BLOCK_BIT_TL) != 0) ontoInk else ontoPaper
                val ontoBrColor = if ((ontoCharValue and SciiChar.BLOCK_BIT_BR) != 0) ontoInk else ontoPaper
                val ontoBlColor = if ((ontoCharValue and SciiChar.BLOCK_BIT_BL) != 0) ontoInk else ontoPaper

                val mergedTrColor = mergeColor(trColor, ontoTrColor)
                val mergedTlColor = mergeColor(tlColor, ontoTlColor)
                val mergedBrColor = mergeColor(brColor, ontoBrColor)
                val mergedBlColor = mergeColor(blColor, ontoBlColor)

                val mergedColorsMap = mutableMapOf<SciiColor, Int>()
                mergedColorsMap[mergedTrColor] = (mergedColorsMap[mergedTrColor] ?: 0) + 1
                mergedColorsMap[mergedTlColor] = (mergedColorsMap[mergedTlColor] ?: 0) + 1
                mergedColorsMap[mergedBrColor] = (mergedColorsMap[mergedBrColor] ?: 0) + 1
                mergedColorsMap[mergedBlColor] = (mergedColorsMap[mergedBlColor] ?: 0) + 1

                when (mergedColorsMap.size) {
                    1 -> {
                        if (mergedTrColor.value < 0) {
                            SciiCell.Transparent
                        } else {
                            SciiCell(
                                character = SciiChar.BlockSpace,
                                ink = mergedTrColor,
                                paper = mergedTrColor,
                                bright = mergeLight(which.bright, onto.bright),
                                flash = mergeLight(which.flash, onto.flash),
                            )
                        }
                    }

                    2 -> {
                        val (mergedInk, mergedPaper) = mergedColorsMap.keys.toList()

                        val mergedValue = SciiChar.BLOCK_VALUE_FIRST +
                                (if (mergedTrColor == mergedInk) SciiChar.BLOCK_BIT_TR else 0) +
                                (if (mergedTlColor == mergedInk) SciiChar.BLOCK_BIT_TL else 0) +
                                (if (mergedBrColor == mergedInk) SciiChar.BLOCK_BIT_BR else 0) +
                                (if (mergedBlColor == mergedInk) SciiChar.BLOCK_BIT_BL else 0)

                        SciiCell(
                            character = SciiChar(mergedValue),
                            ink = mergedInk,
                            paper = mergedPaper,
                            bright = mergeLight(which.bright, onto.bright),
                            flash = mergeLight(which.flash, onto.flash),
                        )
                    }

                    else -> SciiCell(
                        character = mergeChar(which.character, onto.character),
                        ink = mergeColor(which.ink, onto.ink),
                        paper = mergeColor(which.paper, onto.paper),
                        bright = mergeLight(which.bright, onto.bright),
                        flash = mergeLight(which.flash, onto.flash),
                    )
                }
            }

            else -> SciiCell(
                character = mergeChar(which.character, onto.character),
                ink = mergeColor(which.ink, onto.ink),
                paper = mergeColor(which.paper, onto.paper),
                bright = mergeLight(which.bright, onto.bright),
                flash = mergeLight(which.flash, onto.flash),
            )
        }
    }

    fun mergeBlockCell(which: BlockCell, onto: BlockCell) = BlockCell(
        color = mergeColor(which.color, onto.color),
        bright = mergeLight(which.bright, onto.bright),
    )

    @Suppress("NOTHING_TO_INLINE")
    inline fun mergeChar(which: SciiChar, onto: SciiChar): SciiChar =
        when (which) {
            SciiChar.ForceTransparent -> SciiChar.Transparent
            SciiChar.Transparent -> onto
            else -> which
        }

    @Suppress("NOTHING_TO_INLINE")
    inline fun mergeColor(which: SciiColor, onto: SciiColor): SciiColor =
        when (which) {
            SciiColor.ForceTransparent -> SciiColor.Transparent
            SciiColor.Transparent -> onto
            else -> which
        }

    @Suppress("NOTHING_TO_INLINE")
    inline fun mergeLight(which: SciiLight, onto: SciiLight): SciiLight =
        when (which) {
            SciiLight.ForceTransparent -> SciiLight.Transparent
            SciiLight.Transparent -> onto
            else -> which
        }
}
