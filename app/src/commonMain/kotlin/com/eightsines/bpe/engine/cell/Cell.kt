package com.eightsines.bpe.engine.cell

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.VersionedPackableBag
import com.eightsines.bpe.bag.VersionedUnpackableBag
import com.eightsines.bpe.engine.data.SciiChar
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight

sealed interface Cell : BagStuff

data class SciiCell(
    val character: SciiChar,
    val ink: SciiColor,
    val paper: SciiColor,
    val bright: SciiLight,
    val flash: SciiLight,
) : Cell {
    fun merge(onto: SciiCell) = SciiCell(
        character = character.merge(onto.character),
        ink = ink.merge(onto.ink),
        paper = paper.merge(onto.paper),
        bright = bright.merge(onto.bright),
        flash = flash.merge(onto.flash),
    )

    override fun putInTheBag(versionedBag: VersionedPackableBag) =
        versionedBag.putVersion(1).run {
            put(character.value)
            put(ink.value)
            put(paper.value)
            put(bright.value)
            put(flash.value)
        }

    companion object : BagStuff.Unpacker<SciiCell> {
        val Transparent = SciiCell(
            character = SciiChar.Transparent,
            ink = SciiColor.Transparent,
            paper = SciiColor.Transparent,
            bright = SciiLight.Transparent,
            flash = SciiLight.Transparent,
        )

        override fun getOutOfTheBag(versionedBag: VersionedUnpackableBag) =
            versionedBag.getVersion().let { (version, bag) ->
                if (version != 1) {
                    throw BagUnpackException("Unsupported version=$version for SciiCell")
                }

                SciiCell(
                    character = SciiChar(bag.getInt()),
                    ink = SciiColor(bag.getInt()),
                    paper = SciiColor(bag.getInt()),
                    bright = SciiLight(bag.getInt()),
                    flash = SciiLight(bag.getInt()),
                )
            }
    }
}

data class BlockDrawingCell(val color: SciiColor, val bright: SciiLight) : Cell {
    fun merge(onto: BlockDrawingCell) = BlockDrawingCell(
        color = color.merge(onto.color),
        bright = bright.merge(onto.bright),
    )

    override fun putInTheBag(versionedBag: VersionedPackableBag) =
        versionedBag.putVersion(1).run {
            put(color.value)
            put(bright.value)
        }

    companion object : BagStuff.Unpacker<BlockDrawingCell> {
        val Transparent = BlockDrawingCell(color = SciiColor.Transparent, bright = SciiLight.Transparent)

        override fun getOutOfTheBag(versionedBag: VersionedUnpackableBag) =
            versionedBag.getVersion().let { (version, bag) ->
                if (version != 1) {
                    throw BagUnpackException("Unsupported version=$version for BlockDrawingCell")
                }

                BlockDrawingCell(
                    color = SciiColor(bag.getInt()),
                    bright = SciiLight(bag.getInt()),
                )
            }
    }
}
