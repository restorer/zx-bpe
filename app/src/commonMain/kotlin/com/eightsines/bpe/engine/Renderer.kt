package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Box
import com.eightsines.bpe.graphics.Canvas
import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.HBlockCanvas
import com.eightsines.bpe.graphics.MutableSciiCanvas
import com.eightsines.bpe.graphics.VBlockCanvas
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.model.HBlockMergeCell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.VBlockMergeCell

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
            for (sciiY in box.y..box.ey) {
                for (sciiX in box.x..box.ex) {
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
