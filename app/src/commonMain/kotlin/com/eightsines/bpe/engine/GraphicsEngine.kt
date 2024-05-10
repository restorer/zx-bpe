package com.eightsines.bpe.engine

import com.eightsines.bpe.graphics.Box
import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.HBlockCanvas
import com.eightsines.bpe.graphics.MutableCanvas
import com.eightsines.bpe.graphics.MutableHBlockCanvas
import com.eightsines.bpe.graphics.MutableSciiCanvas
import com.eightsines.bpe.graphics.MutableVBlockCanvas
import com.eightsines.bpe.graphics.Painter
import com.eightsines.bpe.graphics.VBlockCanvas
import com.eightsines.bpe.layer.BackgroundLayer
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.LayerUid
import com.eightsines.bpe.layer.MutableBackgroundLayer
import com.eightsines.bpe.layer.MutableCanvasLayer
import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UidFactory
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

class GraphicsEngine(private val uidFactory: UidFactory, private val painter: Painter) {
    private var borderColor: SciiColor = SciiColor.Transparent

    private var backgroundLayer = MutableBackgroundLayer(
        isVisible = true,
        isLocked = false,
        color = SciiColor.Transparent,
        bright = SciiLight.Transparent,
    )

    private var canvasLayers = mutableListOf<MutableCanvasLayer<*>>()
    private val canvasLayersMap = mutableMapOf<String, MutableCanvasLayer<*>>()
    private val preview = MutableSciiCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)

    fun execute(action: GraphicsAction): GraphicsAction? = when (action) {
        is GraphicsAction.SetBorderColor -> executeSetBorderColor(action)
        is GraphicsAction.SetBackgroundColor -> executeSetBackgroundColor(action)
        is GraphicsAction.SetBackgroundBright -> executeSetBackgroundBright(action)
        is GraphicsAction.SetBackgroundVisible -> executeSetBackgroundVisible(action)
        is GraphicsAction.SetBackgroundLocked -> executeSetBackgroundLocked(action)
        is GraphicsAction.CreateLayer -> executeCreateLayer(action)
        is GraphicsAction.ReplaceLayer -> executeReplaceLayer(action)
        is GraphicsAction.InsertLayer -> executeInsertLayer(action)
        is GraphicsAction.DeleteLayer -> executeDeleteLayer(action)
        is GraphicsAction.SetLayerVisible -> executeSetLayerVisible(action)
        is GraphicsAction.SetLayerLocked -> executeSetLayerLocked(action)
        is GraphicsAction.MoveLayer -> executeMoveLayer(action)
        is GraphicsAction.DrawShape -> executeDrawShape(action)
        is GraphicsAction.ReplaceCells -> executeReplaceCells(action)
        is GraphicsAction.MergeLayers -> executeMergeLayers(action)
        is GraphicsAction.UndoMergeLayers -> executeUndoMergeLayers(action)
        is GraphicsAction.ConvertLayer -> executeConvertLayer(action)
    }

    fun putInTheBag(bag: PackableBag) {
        bag.put(
            StateStuff,
            StateStuff(
                borderColor = borderColor,
                backgroundLayer = backgroundLayer,
                canvasLayers = canvasLayers,
            )
        )
    }

    fun getOutOfTheBag(bag: UnpackableBag) {
        val stateStuff = bag.getStuff(StateStuff)

        borderColor = stateStuff.borderColor
        backgroundLayer = stateStuff.backgroundLayer
        canvasLayers = stateStuff.canvasLayers

        canvasLayersMap.clear()

        for (layer in canvasLayers) {
            canvasLayersMap[layer.uid.value] = layer
        }

        updatePreview(ScreenBox)
    }

    private fun executeSetBorderColor(action: GraphicsAction.SetBorderColor): GraphicsAction? =
        if (borderColor != action.color) {
            val undoAction = GraphicsAction.SetBorderColor(borderColor)
            borderColor = action.color
            undoAction
        } else {
            null
        }

    private fun executeSetBackgroundColor(action: GraphicsAction.SetBackgroundColor): GraphicsAction? =
        if (!backgroundLayer.isLocked && backgroundLayer.color != action.color) {
            val undoAction = GraphicsAction.SetBackgroundColor(backgroundLayer.color)
            backgroundLayer.color = action.color
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun executeSetBackgroundBright(action: GraphicsAction.SetBackgroundBright): GraphicsAction? =
        if (!backgroundLayer.isLocked && backgroundLayer.bright != action.light) {
            val undoAction = GraphicsAction.SetBackgroundBright(backgroundLayer.bright)
            backgroundLayer.bright = action.light
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun executeSetBackgroundVisible(action: GraphicsAction.SetBackgroundVisible): GraphicsAction? =
        if (backgroundLayer.isVisible != action.isVisible) {
            val undoAction = GraphicsAction.SetBackgroundVisible(backgroundLayer.isVisible)
            backgroundLayer.isVisible = action.isVisible
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun executeSetBackgroundLocked(action: GraphicsAction.SetBackgroundLocked): GraphicsAction? =
        if (backgroundLayer.isLocked != action.isLocked) {
            val undoAction = GraphicsAction.SetBackgroundLocked(backgroundLayer.isLocked)
            backgroundLayer.isLocked = action.isLocked
            undoAction
        } else {
            null
        }

    private fun executeCreateLayer(action: GraphicsAction.CreateLayer): GraphicsAction {
        val layer = MutableCanvasLayer(
            uid = LayerUid(uidFactory.createUid()),
            canvas = MutableCanvas.create(action.canvasType, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT),
        )

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), layer)
        canvasLayersMap[layer.uid.value] = layer

        return undoAction
    }

    private fun executeReplaceLayer(action: GraphicsAction.ReplaceLayer): GraphicsAction? {
        val existingLayer = canvasLayersMap[action.layer.uid.value] ?: return null
        val undoAction = GraphicsAction.ReplaceLayer(existingLayer)

        val layer = action.layer.copyMutable()
        canvasLayers[getLayerIndex(layer.uid)] = layer
        canvasLayersMap[action.layer.uid.value] = layer

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeInsertLayer(action: GraphicsAction.InsertLayer): GraphicsAction {
        val layer = action.layer.copyMutable()

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), layer)
        canvasLayersMap[action.layer.uid.value] = layer

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeDeleteLayer(action: GraphicsAction.DeleteLayer): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.isLocked) {
            return null
        }

        val layerIndex = getLayerInsertIndex(layer.uid)
        val undoAction = GraphicsAction.InsertLayer(layer = layer, onTopOfLayerUid = getLayerUidBelow(layerIndex))

        canvasLayers.removeAt(layerIndex)
        canvasLayersMap.remove(layer.uid.value)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeSetLayerVisible(action: GraphicsAction.SetLayerVisible): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        val undoAction = GraphicsAction.SetLayerVisible(layer.uid, layer.isVisible)
        layer.isVisible = action.isVisible
        return undoAction
    }

    private fun executeSetLayerLocked(action: GraphicsAction.SetLayerLocked): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        val undoAction = GraphicsAction.SetLayerLocked(layer.uid, layer.isLocked)
        layer.isLocked = action.isLocked
        return undoAction
    }

    private fun executeMoveLayer(action: GraphicsAction.MoveLayer): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.uid == action.onTopOfLayerUid) {
            return null
        }

        val layerIndex = getLayerIndex(layer.uid)
        val undoOnTopOfLayerUid = getLayerUidBelow(layerIndex)

        if (undoOnTopOfLayerUid == action.onTopOfLayerUid) {
            return null
        }

        val undoAction = GraphicsAction.MoveLayer(layer.uid, undoOnTopOfLayerUid)
        canvasLayers.removeAt(layerIndex)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), layer)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeDrawShape(action: GraphicsAction.DrawShape): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.isLocked || layer.canvas.cellType != action.shape.cellType) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        val canvas: MutableCanvas<Cell> = layer.canvas as MutableCanvas<Cell>

        val drawingBBox = painter.getBBox(action.shape)

        val (sciiSX, sciiSY) = canvas.toSciiPosition(drawingBBox.x, drawingBBox.y)
        val (sciiEX, sciiEY) = canvas.toSciiPosition(drawingBBox.ex, drawingBBox.ey)

        val undoAction = GraphicsAction.ReplaceCells(
            layerUid = layer.uid,
            x = sciiSX,
            y = sciiSY,
            crate = Crate.makeScii(layer.canvas, sciiSX, sciiSY, sciiEX - sciiSX + 1, sciiEY - sciiSY + 1),
        )

        canvas.mutate { mutator ->
            painter.paint(action.shape) { x, y, cell ->
                mutator.putDrawingCell(x, y, cell)
            }
        }

        return undoAction
    }

    private fun executeReplaceCells(action: GraphicsAction.ReplaceCells): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.isLocked) {
            return null
        }

        val crate = action.crate

        val undoAction = GraphicsAction.ReplaceCells(
            layerUid = layer.uid,
            x = action.x,
            y = action.y,
            crate = Crate.makeScii(layer.canvas, action.x, action.y, crate.width, crate.height),
        )

        layer.canvas.mutate { mutator ->
            for (cy in action.y..<(action.y + crate.height)) {
                for (cx in action.x..<(action.x + crate.width)) {
                    mutator.replaceSciiCell(cx, cy, crate.cells[cy][cx])
                }
            }
        }

        updatePreview(Box(action.x, action.y, crate.width, crate.height))
        return undoAction
    }

    private fun executeMergeLayers(action: GraphicsAction.MergeLayers): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        val ontoLayer = canvasLayersMap[action.ontoLayerUid.value] ?: return null

        if (layer.isLocked || ontoLayer.isLocked || layer.canvas.type != ontoLayer.canvas.type) {
            return null
        }

        val layerIndex = getLayerIndex(layer.uid)
        val ontoLayerIndex = getLayerIndex(ontoLayer.uid)

        val undoAction = GraphicsAction.UndoMergeLayers(
            insertLayer = layer,
            insertOnTopOfLayerUid = getLayerUidBelow(layerIndex),
            replaceLayer = ontoLayer,
        )

        val mergeLayer = ontoLayer.copyMutable()

        val canvas = layer.canvas
        val mergeCanvas = mergeLayer.canvas

        val mergeWidth = minOf(canvas.sciiWidth, mergeCanvas.sciiWidth)
        val mergeHeight = minOf(canvas.sciiHeight, mergeCanvas.sciiHeight)

        when {
            canvas is HBlockCanvas && mergeCanvas is MutableHBlockCanvas -> mergeCanvas.mutateHBlock { mutator ->
                walkInBox(mergeWidth, mergeHeight) { x, y ->
                    mutator.replaceMergeCell(x, y, canvas.getMergeCell(x, y).merge(mergeCanvas.getMergeCell(x, y)))
                }
            }

            canvas is VBlockCanvas && mergeCanvas is MutableVBlockCanvas -> mergeCanvas.mutateVBlock { mutator ->
                walkInBox(mergeWidth, mergeHeight) { x, y ->
                    mutator.replaceMergeCell(x, y, canvas.getMergeCell(x, y).merge(mergeCanvas.getMergeCell(x, y)))
                }
            }

            else -> mergeCanvas.mutate { mutator ->
                walkInBox(mergeWidth, mergeHeight) { x, y ->
                    mutator.replaceSciiCell(x, y, canvas.getSciiCell(x, y).merge(mergeCanvas.getSciiCell(x, y)))
                }
            }
        }

        canvasLayers[ontoLayerIndex] = mergeLayer
        canvasLayersMap[ontoLayer.uid.value] = mergeLayer

        canvasLayers.removeAt(layerIndex)
        canvasLayersMap.remove(layer.uid.value)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeUndoMergeLayers(action: GraphicsAction.UndoMergeLayers): GraphicsAction? {
        if (!canvasLayersMap.containsKey(action.replaceLayer.uid.value)) {
            return null
        }

        val undoAction = GraphicsAction.MergeLayers(
            layerUid = action.insertLayer.uid,
            ontoLayerUid = action.replaceLayer.uid,
        )

        val replaceLayer = action.replaceLayer.copyMutable()
        canvasLayers[getLayerIndex(replaceLayer.uid)] = replaceLayer
        canvasLayersMap[replaceLayer.uid.value] = replaceLayer

        val insertLayer = action.insertLayer.copyMutable()
        canvasLayers.add(getLayerInsertIndex(action.insertOnTopOfLayerUid), insertLayer)
        canvasLayersMap[insertLayer.uid.value] = insertLayer

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeConvertLayer(action: GraphicsAction.ConvertLayer): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        val canvas = layer.canvas

        if (canvas.type == action.canvasType) {
            return null
        }

        val convertedLayer = MutableCanvasLayer(
            uid = layer.uid,
            canvas = MutableCanvas.create(action.canvasType, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT),
        )

        convertedLayer.canvas.mutate { mutator ->
            walkInBox(canvas.sciiWidth, canvas.sciiHeight) { x, y ->
                mutator.replaceSciiCell(x, y, canvas.getSciiCell(x, y))
            }
        }

        val undoAction = GraphicsAction.ReplaceLayer(layer)
        canvasLayers[getLayerIndex(layer.uid)] = convertedLayer
        canvasLayersMap[layer.uid.value] = convertedLayer

        return undoAction
    }

    private fun updatePreview(box: Box) {
        preview.mutate { mutator ->
            walkInBox(box) { x, y ->
                var cell = backgroundLayer.sciiCell

                for (layer in canvasLayers) {
                    cell = layer.canvas.getSciiCell(x, y).merge(cell)
                }

                mutator.replaceSciiCell(x, y, cell)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getLayerIndex(uid: LayerUid) = canvasLayers.indexOfFirst { it.uid.value == uid.value }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getLayerInsertIndex(onTopOfLayerUid: LayerUid) =
        if (onTopOfLayerUid == LayerUid.Background) {
            0
        } else {
            canvasLayers.indexOfFirst { it.uid.value == onTopOfLayerUid.value } + 1
        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getLayerUidBelow(index: Int) =
        if (index > 0) canvasLayers[index - 1].uid else LayerUid.Background

    private inline fun walkInBox(box: Box, block: (x: Int, y: Int) -> Unit) {
        walkInBox(box.x, box.y, box.ex, box.ey, block)
    }

    private inline fun walkInBox(width: Int, height: Int, block: (x: Int, y: Int) -> Unit) {
        walkInBox(0, 0, width - 1, height - 1, block)
    }

    private inline fun walkInBox(sx: Int, sy: Int, ex: Int, ey: Int, block: (x: Int, y: Int) -> Unit) {
        for (y in sy..ey) {
            for (x in sx..ex) {
                block(x, y)
            }
        }
    }

    companion object {
        const val SCREEN_SCII_WIDTH = 32
        const val SCREEN_SCII_HEIGHT = 24

        private val ScreenBox = Box(0, 0, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
    }

    private class StateStuff(
        val borderColor: SciiColor,
        val backgroundLayer: MutableBackgroundLayer,
        val canvasLayers: MutableList<MutableCanvasLayer<*>>,
    ) {
        companion object : BagStuffPacker<StateStuff>, BagStuffUnpacker<StateStuff> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: StateStuff) {
                bag.put(value.borderColor.value)
                bag.put(BackgroundLayer, value.backgroundLayer)
                bag.put(value.canvasLayers.size)

                for (layer in value.canvasLayers) {
                    bag.put(CanvasLayer, layer)
                }
            }

            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): StateStuff {
                if (version != 1) {
                    throw UnsupportedVersionBagUnpackException("GraphicsEngine", version)
                }

                val borderColor = SciiColor(bag.getInt())
                val backgroundLayer = bag.getStuff(MutableBackgroundLayer)
                val canvasLayersSize = bag.getInt()

                val canvasLayers =
                    (0..<canvasLayersSize).mapTo(mutableListOf()) { bag.getStuff(MutableCanvasLayer) }

                return StateStuff(
                    borderColor = borderColor,
                    backgroundLayer = backgroundLayer,
                    canvasLayers = canvasLayers,
                )
            }
        }
    }
}
