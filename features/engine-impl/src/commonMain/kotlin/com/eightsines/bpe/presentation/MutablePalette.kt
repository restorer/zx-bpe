package com.eightsines.bpe.presentation

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagStuffWare
import com.eightsines.bpe.foundation.BlockCell
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.Cell
import com.eightsines.bpe.foundation.SciiCell
import com.eightsines.bpe.foundation.SciiChar
import com.eightsines.bpe.foundation.SciiColor
import com.eightsines.bpe.foundation.SciiLight
import com.eightsines.bpe.foundation.isBlock

@BagStuff
class MutablePalette(
    @BagStuffWare(1) var paintSciiInk: SciiColor = SciiColor.Transparent,
    @BagStuffWare(2) var paintSciiPaper: SciiColor = SciiColor.Transparent,
    @BagStuffWare(3) var paintSciiBright: SciiLight = SciiLight.Transparent,
    @BagStuffWare(4) var paintSciiFlash: SciiLight = SciiLight.Transparent,
    @BagStuffWare(5) var paintSciiCharacter: SciiChar = SciiChar.Transparent,
    @BagStuffWare(6, version = 2, fallback = "SciiColor.Transparent") var paintBlockColor: SciiColor = SciiColor.Transparent,
    @BagStuffWare(7, version = 2, fallback = "SciiLight.Transparent") var paintBlockBright: SciiLight = SciiLight.Transparent,
    @BagStuffWare(8, version = 3, fallback = "true") var eraseSciiInk: Boolean = true,
    @BagStuffWare(9, version = 3, fallback = "true") var eraseSciiPaper: Boolean = true,
    @BagStuffWare(10, version = 3, fallback = "true") var eraseSciiBright: Boolean = true,
    @BagStuffWare(11, version = 3, fallback = "true") var eraseSciiFlash: Boolean = true,
    @BagStuffWare(12, version = 3, fallback = "true") var eraseSciiCharacter: Boolean = true,
    @BagStuffWare(13, version = 3, fallback = "true") var eraseBlockColor: Boolean = true,
    @BagStuffWare(14, version = 3, fallback = "true") var eraseBlockBright: Boolean = true,
) {
    fun setFrom(other: MutablePalette) {
        paintSciiInk = other.paintSciiInk
        paintSciiPaper = other.paintSciiPaper
        paintSciiBright = other.paintSciiBright
        paintSciiFlash = other.paintSciiFlash
        paintSciiCharacter = other.paintSciiCharacter
        paintBlockColor = other.paintBlockColor
        paintBlockBright = other.paintBlockBright
        eraseSciiInk = other.eraseSciiInk
        eraseSciiPaper = other.eraseSciiPaper
        eraseSciiBright = other.eraseSciiBright
        eraseSciiFlash = other.eraseSciiFlash
        eraseSciiCharacter = other.eraseSciiCharacter
        eraseBlockColor = other.eraseBlockColor
        eraseBlockBright = other.eraseBlockBright
    }

    fun clear() {
        paintSciiInk = SciiColor.Transparent
        paintSciiPaper = SciiColor.Transparent
        paintSciiBright = SciiLight.Transparent
        paintSciiFlash = SciiLight.Transparent
        paintSciiCharacter = SciiChar.Transparent
        paintBlockColor = SciiColor.Transparent
        paintBlockBright = SciiLight.Transparent
        eraseSciiInk = true
        eraseSciiPaper = true
        eraseSciiBright = true
        eraseSciiFlash = true
        eraseSciiCharacter = true
        eraseBlockColor = true
        eraseBlockBright = true
    }

    override fun toString() =
        "MutablePalette(paintSciiInk=$paintSciiInk, paintSciiPaper=$paintSciiPaper, paintSciiBright=$paintSciiBright," +
                " paintSciiFlash=$paintSciiFlash, paintSciiCharacter=$paintSciiCharacter, paintBlockColor=$paintBlockColor," +
                " paintBlockBright=$paintBlockBright, eraseSciiInk=$eraseSciiInk, eraseSciiPaper=$eraseSciiPaper," +
                " eraseSciiBright=$eraseSciiBright, eraseSciiFlash=$eraseSciiFlash, eraseSciiCharacter=$eraseSciiCharacter," +
                " eraseBlockColor=$eraseBlockColor, eraseBlockBright=$eraseBlockBright)"
}

fun MutablePalette.makePaintCell(canvasType: CanvasType) = if (canvasType.isBlock) {
    BlockCell(
        color = paintBlockColor,
        bright = paintBlockBright,
    )
} else {
    SciiCell(
        character = paintSciiCharacter,
        ink = paintSciiInk,
        paper = paintSciiPaper,
        bright = paintSciiBright,
        flash = paintSciiFlash,
    )
}

fun MutablePalette.makeEraseCell(canvasType: CanvasType) = if (canvasType.isBlock) {
    BlockCell(
        color = if (eraseBlockColor) SciiColor.ForceTransparent else SciiColor.Transparent,
        bright = if (eraseBlockBright) SciiLight.ForceTransparent else SciiLight.Transparent,
    )
} else {
    SciiCell(
        character = if (eraseSciiCharacter) SciiChar.ForceTransparent else SciiChar.Transparent,
        ink = if (eraseSciiInk) SciiColor.ForceTransparent else SciiColor.Transparent,
        paper = if (eraseSciiPaper) SciiColor.ForceTransparent else SciiColor.Transparent,
        bright = if (eraseSciiBright) SciiLight.ForceTransparent else SciiLight.Transparent,
        flash = if (eraseSciiFlash) SciiLight.ForceTransparent else SciiLight.Transparent,
    )
}

fun MutablePalette.setPaintFromCell(cell: Cell) = when (cell) {
    is SciiCell -> {
        paintSciiCharacter = cell.character
        paintSciiInk = cell.ink
        paintSciiPaper = cell.paper
        paintSciiBright = cell.bright
        paintSciiFlash = cell.flash
    }

    is BlockCell -> {
        paintBlockColor = cell.color
        paintBlockBright = cell.bright
    }
}
