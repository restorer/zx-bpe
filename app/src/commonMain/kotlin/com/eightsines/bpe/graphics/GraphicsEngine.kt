package com.eightsines.bpe.graphics

import com.eightsines.bpe.core.Box
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight
import com.eightsines.bpe.foundation.BackgroundLayer
import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.foundation.HBlockCanvas
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.MutableBackgroundLayer
import com.eightsines.bpe.foundation.MutableCanvas
import com.eightsines.bpe.foundation.MutableCanvasLayer
import com.eightsines.bpe.foundation.MutableHBlockCanvas
import com.eightsines.bpe.foundation.MutableSciiCanvas
import com.eightsines.bpe.foundation.MutableVBlockCanvas
import com.eightsines.bpe.foundation.SciiCanvas
import com.eightsines.bpe.foundation.VBlockCanvas
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

class GraphicsEngine(
    private val logger: Logger,
    private val painter: Painter,
    private val renderer: Renderer,
) {
    private var backgroundLayer = MutableBackgroundLayer(
        isVisible = true,
        isLocked = false,
        border = SciiColor.Transparent,
        color = SciiColor.Transparent,
        bright = SciiLight.Transparent,
    )

    private var canvasLayers = mutableListOf<MutableCanvasLayer<*>>()
    private val canvasLayersMap = mutableMapOf<String, MutableCanvasLayer<*>>()
    private val preview = MutableSciiCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)

    val state = object : GraphicsState {
        override val backgroundLayer: BackgroundLayer
            get() = this@GraphicsEngine.backgroundLayer

        override val canvasLayers: List<CanvasLayer<*>>
            get() = this@GraphicsEngine.canvasLayers

        override val canvasLayersMap: Map<String, CanvasLayer<*>>
            get() = this@GraphicsEngine.canvasLayersMap

        override val preview: SciiCanvas
            get() = this@GraphicsEngine.preview

        override fun toString() =
            "GraphicsState(backgroundLayer=$backgroundLayer, canvasLayers=$canvasLayers, canvasLayersMap=$canvasLayersMap, preview=$preview)"
    }

    fun canExecute(action: GraphicsAction): Boolean = when (action) {
        is GraphicsAction.SetBackgroundBorder -> canSetBackgroundBorder(action)
        is GraphicsAction.SetBackgroundColor -> canSetBackgroundColor(action)
        is GraphicsAction.SetBackgroundBright -> canSetBackgroundBright(action)
        is GraphicsAction.SetBackgroundVisible -> canSetBackgroundVisible(action)
        is GraphicsAction.SetBackgroundLocked -> canSetBackgroundLocked(action)
        is GraphicsAction.CreateLayer -> canCreateLayer()
        is GraphicsAction.ReplaceLayer -> canReplaceLayer(action) != null
        is GraphicsAction.InsertLayer -> canInsertLayer()
        is GraphicsAction.DeleteLayer -> canDeleteLayer(action) != null
        is GraphicsAction.SetLayerVisible -> canSetLayerVisible(action) != null
        is GraphicsAction.SetLayerLocked -> canSetLayerLocked(action) != null
        is GraphicsAction.MoveLayer -> canMoveLayer(action) != null
        is GraphicsAction.MergeShape -> canMergeShape(action) != null
        is GraphicsAction.ReplaceShape -> canReplaceShape(action) != null
        is GraphicsAction.ReplaceCells -> canReplaceCells(action) != null
        is GraphicsAction.MergeLayers -> canMergeLayers(action) != null
        is GraphicsAction.UndoMergeLayers -> canUndoMergeLayers(action)
        is GraphicsAction.ConvertLayer -> canConvertLayer(action) != null
    }

    fun execute(action: GraphicsAction): GraphicsAction? {
        logger.note("GraphicsEngine.execute:begin") {
            put("action", action.toString())
        }

        val undoAction = when (action) {
            is GraphicsAction.SetBackgroundBorder -> executeSetBackgroundBorder(action)
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
            is GraphicsAction.MergeShape -> executeMergeShape(action)
            is GraphicsAction.ReplaceShape -> executeReplaceShape(action)
            is GraphicsAction.ReplaceCells -> executeReplaceCells(action)
            is GraphicsAction.MergeLayers -> executeMergeLayers(action)
            is GraphicsAction.UndoMergeLayers -> executeUndoMergeLayers(action)
            is GraphicsAction.ConvertLayer -> executeConvertLayer(action)
        }

        logger.note("GraphicsEngine.execute:end") {
            put("state", state.toString())
            put("undoAction", undoAction.toString())
        }

        return undoAction
    }

    fun putInTheBag(bag: PackableBag) {
        bag.put(
            StateStuff,
            StateStuff(backgroundLayer = backgroundLayer, canvasLayers = canvasLayers),
        )
    }

    fun getOutOfTheBag(bag: UnpackableBag) {
        val stateStuff = bag.getStuff(StateStuff)

        backgroundLayer = stateStuff.backgroundLayer
        canvasLayers = stateStuff.canvasLayers

        canvasLayersMap.clear()

        for (layer in canvasLayers) {
            canvasLayersMap[layer.uid.value] = layer
        }

        updatePreview(ScreenBox)
    }

    private fun canSetBackgroundBorder(action: GraphicsAction.SetBackgroundBorder) =
        !backgroundLayer.isLocked && backgroundLayer.border != action.color

    private fun executeSetBackgroundBorder(action: GraphicsAction.SetBackgroundBorder): GraphicsAction? =
        if (canSetBackgroundBorder(action)) {
            val undoAction = GraphicsAction.SetBackgroundBorder(backgroundLayer.border)
            backgroundLayer.border = action.color
            undoAction
        } else {
            null
        }

    private fun canSetBackgroundColor(action: GraphicsAction.SetBackgroundColor) =
        !backgroundLayer.isLocked && backgroundLayer.color != action.color

    private fun executeSetBackgroundColor(action: GraphicsAction.SetBackgroundColor): GraphicsAction? =
        if (canSetBackgroundColor(action)) {
            val undoAction = GraphicsAction.SetBackgroundColor(backgroundLayer.color)
            backgroundLayer.color = action.color
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun canSetBackgroundBright(action: GraphicsAction.SetBackgroundBright) =
        !backgroundLayer.isLocked && backgroundLayer.bright != action.light

    private fun executeSetBackgroundBright(action: GraphicsAction.SetBackgroundBright): GraphicsAction? =
        if (canSetBackgroundBright(action)) {
            val undoAction = GraphicsAction.SetBackgroundBright(backgroundLayer.bright)
            backgroundLayer.bright = action.light
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun canSetBackgroundVisible(action: GraphicsAction.SetBackgroundVisible) =
        backgroundLayer.isVisible != action.isVisible

    private fun executeSetBackgroundVisible(action: GraphicsAction.SetBackgroundVisible): GraphicsAction? =
        if (canSetBackgroundVisible(action)) {
            val undoAction = GraphicsAction.SetBackgroundVisible(backgroundLayer.isVisible)
            backgroundLayer.isVisible = action.isVisible
            updatePreview(ScreenBox)
            undoAction
        } else {
            null
        }

    private fun canSetBackgroundLocked(action: GraphicsAction.SetBackgroundLocked) =
        backgroundLayer.isLocked != action.isLocked

    private fun executeSetBackgroundLocked(action: GraphicsAction.SetBackgroundLocked): GraphicsAction? =
        if (canSetBackgroundLocked(action)) {
            val undoAction = GraphicsAction.SetBackgroundLocked(backgroundLayer.isLocked)
            backgroundLayer.isLocked = action.isLocked
            undoAction
        } else {
            null
        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun canCreateLayer() = true

    private fun executeCreateLayer(action: GraphicsAction.CreateLayer): GraphicsAction {
        val layer = MutableCanvasLayer(
            uid = action.layerUid,
            canvas = MutableCanvas.create(action.canvasType, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT),
        )

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), layer)
        canvasLayersMap[layer.uid.value] = layer

        return undoAction
    }

    private fun canReplaceLayer(action: GraphicsAction.ReplaceLayer) =
        canvasLayersMap[action.layer.uid.value]

    private fun executeReplaceLayer(action: GraphicsAction.ReplaceLayer): GraphicsAction? {
        val existingLayer = canReplaceLayer(action) ?: return null
        val undoAction = GraphicsAction.ReplaceLayer(existingLayer)

        val layer = action.layer.copyMutable()
        canvasLayers[getLayerIndex(layer.uid)] = layer
        canvasLayersMap[action.layer.uid.value] = layer

        updatePreview(ScreenBox)
        return undoAction
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun canInsertLayer() = true

    private fun executeInsertLayer(action: GraphicsAction.InsertLayer): GraphicsAction {
        val layer = action.layer.copyMutable()

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), layer)
        canvasLayersMap[action.layer.uid.value] = layer

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun canDeleteLayer(action: GraphicsAction.DeleteLayer): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isLocked) null else layer
    }

    private fun executeDeleteLayer(action: GraphicsAction.DeleteLayer): GraphicsAction? {
        val layer = canDeleteLayer(action) ?: return null

        val layerIndex = getLayerIndex(layer.uid)
        val undoAction = GraphicsAction.InsertLayer(layer = layer, onTopOfLayerUid = getLayerUidBelow(layerIndex))

        canvasLayers.removeAt(layerIndex)
        canvasLayersMap.remove(layer.uid.value)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun canSetLayerVisible(action: GraphicsAction.SetLayerVisible): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isVisible == action.isVisible) null else layer
    }

    private fun executeSetLayerVisible(action: GraphicsAction.SetLayerVisible): GraphicsAction? {
        val layer = canSetLayerVisible(action) ?: return null

        val undoAction = GraphicsAction.SetLayerVisible(layer.uid, layer.isVisible)
        layer.isVisible = action.isVisible
        return undoAction
    }

    private fun canSetLayerLocked(action: GraphicsAction.SetLayerLocked): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isLocked == action.isLocked) null else layer
    }

    private fun executeSetLayerLocked(action: GraphicsAction.SetLayerLocked): GraphicsAction? {
        val layer = canSetLayerLocked(action) ?: return null

        val undoAction = GraphicsAction.SetLayerLocked(layer.uid, layer.isLocked)
        layer.isLocked = action.isLocked
        return undoAction
    }

    private fun canMoveLayer(action: GraphicsAction.MoveLayer): MoveLayerData? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.uid == action.onTopOfLayerUid) {
            return null
        }

        val layerIndex = getLayerIndex(layer.uid)
        val undoOnTopOfLayerUid = getLayerUidBelow(layerIndex)

        if (undoOnTopOfLayerUid == action.onTopOfLayerUid) {
            return null
        }

        return MoveLayerData(layer, layerIndex, undoOnTopOfLayerUid)
    }

    private fun executeMoveLayer(action: GraphicsAction.MoveLayer): GraphicsAction? {
        val data = canMoveLayer(action) ?: return null

        val undoAction = GraphicsAction.MoveLayer(data.layer.uid, data.undoOnTopOfLayerUid)
        canvasLayers.removeAt(data.layerIndex)
        canvasLayers.add(getLayerInsertIndex(action.onTopOfLayerUid), data.layer)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun canMergeShape(action: GraphicsAction.MergeShape): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isLocked || layer.canvas.type.cellType != action.shape.cellType) null else layer
    }

    private fun executeMergeShape(action: GraphicsAction.MergeShape): GraphicsAction? {
        val layer = canMergeShape(action) ?: return null

        @Suppress("UNCHECKED_CAST")
        val canvas: MutableCanvas<Cell> = layer.canvas as MutableCanvas<Cell>

        val drawingBBox = painter.getBBox(action.shape)

        val (sciiSX, sciiSY) = canvas.type.toSciiPosition(drawingBBox.x, drawingBBox.y)
        val (sciiEX, sciiEY) = canvas.type.toSciiPosition(drawingBBox.ex, drawingBBox.ey)

        val undoAction = GraphicsAction.ReplaceCells(
            layerUid = layer.uid,
            x = sciiSX,
            y = sciiSY,
            crate = Crate.fromCanvasScii(layer.canvas, sciiSX, sciiSY, sciiEX - sciiSX + 1, sciiEY - sciiSY + 1),
        )

        canvas.mutate { mutator ->
            painter.paint(action.shape) { x, y, cell ->
                mutator.mergeDrawingCell(x, y, cell)
            }
        }

        updatePreview(Box.of(sciiSX, sciiSY, sciiEX, sciiEY))
        return undoAction
    }

    private fun canReplaceShape(action: GraphicsAction.ReplaceShape): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isLocked || layer.canvas.type.cellType != action.shape.cellType) null else layer
    }

    private fun executeReplaceShape(action: GraphicsAction.ReplaceShape): GraphicsAction? {
        val layer = canReplaceShape(action) ?: return null

        @Suppress("UNCHECKED_CAST")
        val canvas: MutableCanvas<Cell> = layer.canvas as MutableCanvas<Cell>

        val drawingBBox = painter.getBBox(action.shape)

        val (sciiSX, sciiSY) = canvas.type.toSciiPosition(drawingBBox.x, drawingBBox.y)
        val (sciiEX, sciiEY) = canvas.type.toSciiPosition(drawingBBox.ex, drawingBBox.ey)

        val undoAction = GraphicsAction.ReplaceCells(
            layerUid = layer.uid,
            x = sciiSX,
            y = sciiSY,
            crate = Crate.fromCanvasScii(layer.canvas, sciiSX, sciiSY, sciiEX - sciiSX + 1, sciiEY - sciiSY + 1),
        )

        canvas.mutate { mutator ->
            painter.paint(action.shape) { x, y, cell ->
                mutator.replaceDrawingCell(x, y, cell)
            }
        }

        updatePreview(Box.of(sciiSX, sciiSY, sciiEX, sciiEY))
        return undoAction
    }

    private fun canReplaceCells(action: GraphicsAction.ReplaceCells): MutableCanvasLayer<*>? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        return if (layer.isLocked) null else layer
    }

    private fun executeReplaceCells(action: GraphicsAction.ReplaceCells): GraphicsAction? {
        val layer = canReplaceCells(action) ?: return null
        val crate = action.crate

        val undoAction = GraphicsAction.ReplaceCells(
            layerUid = layer.uid,
            x = action.x,
            y = action.y,
            crate = Crate.fromCanvasScii(layer.canvas, action.x, action.y, crate.width, crate.height),
        )

        layer.canvas.mutate { mutator ->
            for (cy in 0..<crate.height) {
                for (cx in 0..<crate.width) {
                    mutator.replaceSciiCell(action.x + cx, action.y + cy, crate.cells[cy][cx])
                }
            }
        }

        updatePreview(Box(action.x, action.y, crate.width, crate.height))
        return undoAction
    }

    private fun canMergeLayers(action: GraphicsAction.MergeLayers): MergeLayersData? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        val ontoLayer = canvasLayersMap[action.ontoLayerUid.value] ?: return null

        if (layer.isLocked || ontoLayer.isLocked || layer.canvas.type != ontoLayer.canvas.type) {
            return null
        }

        return MergeLayersData(layer, ontoLayer)
    }

    private fun executeMergeLayers(action: GraphicsAction.MergeLayers): GraphicsAction? {
        val data = canMergeLayers(action) ?: return null

        val layerIndex = getLayerIndex(data.layer.uid)
        val ontoLayerIndex = getLayerIndex(data.ontoLayer.uid)

        val undoAction = GraphicsAction.UndoMergeLayers(
            insertLayer = data.layer,
            insertOnTopOfLayerUid = getLayerUidBelow(layerIndex),
            replaceLayer = data.ontoLayer,
        )

        val mergeLayer = data.ontoLayer.copyMutable()

        val canvas = data.layer.canvas
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
        canvasLayersMap[data.ontoLayer.uid.value] = mergeLayer

        canvasLayers.removeAt(layerIndex)
        canvasLayersMap.remove(data.layer.uid.value)

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun canUndoMergeLayers(action: GraphicsAction.UndoMergeLayers) =
        canvasLayersMap.containsKey(action.replaceLayer.uid.value)

    private fun executeUndoMergeLayers(action: GraphicsAction.UndoMergeLayers): GraphicsAction? {
        if (!canUndoMergeLayers(action)) {
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

    private fun canConvertLayer(action: GraphicsAction.ConvertLayer): ConvertLayerData? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        val canvas = layer.canvas

        if (canvas.type == action.canvasType) {
            return null
        }

        return ConvertLayerData(layer, canvas)
    }

    private fun executeConvertLayer(action: GraphicsAction.ConvertLayer): GraphicsAction? {
        val data = canConvertLayer(action) ?: return null

        val convertedLayer = MutableCanvasLayer(
            uid = data.layer.uid,
            canvas = MutableCanvas.create(action.canvasType, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT, data.layer.canvas.mutations),
        )

        convertedLayer.canvas.mutate { mutator ->
            walkInBox(data.canvas.sciiWidth, data.canvas.sciiHeight) { x, y ->
                mutator.replaceSciiCell(x, y, data.canvas.getSciiCell(x, y))
            }
        }

        val undoAction = GraphicsAction.ReplaceLayer(data.layer)
        canvasLayers[getLayerIndex(data.layer.uid)] = convertedLayer
        canvasLayersMap[data.layer.uid.value] = convertedLayer

        updatePreview(ScreenBox)
        return undoAction
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updatePreview(box: Box) = renderer.render(preview, backgroundLayer, canvasLayers, box)

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

    private inline fun walkInBox(width: Int, height: Int, block: (x: Int, y: Int) -> Unit) {
        for (y in 0..<height) {
            for (x in 0..<width) {
                block(x, y)
            }
        }
    }

    private data class MoveLayerData(
        val layer: MutableCanvasLayer<*>,
        val layerIndex: Int,
        val undoOnTopOfLayerUid: LayerUid,
    )

    private data class MergeLayersData(val layer: MutableCanvasLayer<*>, val ontoLayer: MutableCanvasLayer<*>)
    private data class ConvertLayerData(val layer: MutableCanvasLayer<*>, val canvas: MutableCanvas<*>)

    companion object {
        const val SCREEN_SCII_WIDTH = 32
        const val SCREEN_SCII_HEIGHT = 24

        private val ScreenBox = Box(0, 0, SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
    }

    private class StateStuff(
        val backgroundLayer: MutableBackgroundLayer,
        val canvasLayers: MutableList<MutableCanvasLayer<*>>,
    ) {
        companion object : BagStuffPacker<StateStuff>, BagStuffUnpacker<StateStuff> {
            override val putInTheBagVersion = 1

            override fun putInTheBag(bag: PackableBag, value: StateStuff) {
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

                val backgroundLayer = bag.getStuff(MutableBackgroundLayer)
                val canvasLayersSize = bag.getInt()

                val canvasLayers =
                    (0..<canvasLayersSize).mapTo(mutableListOf()) { bag.getStuff(MutableCanvasLayer) }

                return StateStuff(
                    backgroundLayer = backgroundLayer,
                    canvasLayers = canvasLayers,
                )
            }
        }
    }
}
