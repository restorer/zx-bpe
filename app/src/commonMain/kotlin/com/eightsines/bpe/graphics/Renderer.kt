package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.Canvas
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.HBlockCanvas
import com.eightsines.bpe.foundation.MutableSciiCanvas
import com.eightsines.bpe.foundation.VBlockCanvas
import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.HBlockMergeCell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.VBlockMergeCell

class Renderer {
    fun render(
        destination: MutableSciiCanvas,
        backgroundLayer: BackgroundLayer,
        layers: List<CanvasLayer<*>>,
        box: Box,
    ) {
        val backgroundCell = if (backgroundLayer.isVisible) backgroundLayer.sciiCell else SciiCell.Transparent
        val groups = groupLayers(layers)

        destination.mutate {
            for (sciiY in box.ly..box.ry) {
                for (sciiX in box.lx..box.rx) {
                    it.replaceSciiCell(sciiX, sciiY, mergeCell(backgroundCell, groups, sciiX, sciiY))
                }
            }
        }
    }

    internal fun mergeCell(
        backgroundCell: SciiCell,
        groups: List<Pair<MergeType, List<Canvas<*>>>>,
        sciiX: Int,
        sciiY: Int,
    ): SciiCell {
        var result = backgroundCell

        for ((mergeType, canvases) in groups) {
            when (mergeType) {
                MergeType.Scii -> for (canvas in canvases) {
                    result = canvas.getSciiCell(sciiX, sciiY).merge(result)
                }

                MergeType.HBlock -> {
                    var subResult = HBlockMergeCell.Transparent

                    for (canvas in canvases) {
                        subResult = (canvas as HBlockCanvas).getMergeCell(sciiX, sciiY).merge(subResult)
                    }

                    result = subResult.toSciiCell().merge(result)
                }

                MergeType.VBlock -> {
                    var subResult = VBlockMergeCell.Transparent

                    for (canvas in canvases) {
                        subResult = (canvas as VBlockCanvas).getMergeCell(sciiX, sciiY).merge(subResult)
                    }

                    result = subResult.toSciiCell().merge(result)
                }
            }
        }

        return result
    }

    internal fun groupLayers(layers: List<CanvasLayer<*>>): List<Pair<MergeType, List<Canvas<*>>>> {
        val groups = mutableListOf<Pair<MergeType, List<Canvas<*>>>>()

        var currentType: MergeType? = null
        var currentGroup = mutableListOf<Canvas<*>>()

        for (layer in layers) {
            if (!layer.isVisible) {
                continue
            }

            val mergeType = when (layer.canvas.type) {
                CanvasType.Scii, CanvasType.QBlock -> MergeType.Scii
                CanvasType.HBlock -> MergeType.HBlock
                CanvasType.VBlock -> MergeType.VBlock
            }

            if (currentType != mergeType) {
                if (currentType != null) {
                    groups.add(currentType to currentGroup)
                    currentGroup = mutableListOf()
                }

                currentType = mergeType
                currentGroup.add(layer.canvas)
            } else {
                currentGroup.add(layer.canvas)
            }
        }

        if (currentType != null) {
            groups.add(currentType to currentGroup)
        }

        return groups
    }

    internal enum class MergeType {
        Scii,
        HBlock,
        VBlock,
    }
}
