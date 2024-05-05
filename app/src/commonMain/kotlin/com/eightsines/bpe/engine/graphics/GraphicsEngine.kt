package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.bag.BagStuff
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.engine.canvas.CanvasType
import com.eightsines.bpe.engine.canvas.MutableCanvas
import com.eightsines.bpe.engine.canvas.MutableHBlockCanvas
import com.eightsines.bpe.engine.canvas.MutableQBlockCanvas
import com.eightsines.bpe.engine.canvas.MutableSciiCanvas
import com.eightsines.bpe.engine.canvas.MutableVBlockCanvas
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight
import com.eightsines.bpe.engine.layer.LayerUid
import com.eightsines.bpe.engine.layer.MutableBackgroundLayer
import com.eightsines.bpe.engine.layer.MutableCanvasLayer
import com.eightsines.bpe.util.UidFactory

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

    private fun executeMoveLayer(action: GraphicsAction.MoveLayer): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.uid == action.onTopOfLayerUid) {
            return null
        }

        val layerIndex = canvasLayers.indexOfFirst { it.uid.value == layer.uid.value }
        val undoOnTopOfLayerUid = if (layerIndex > 0) canvasLayers[layerIndex - 1].uid else LayerUid.Background

        if (undoOnTopOfLayerUid == action.onTopOfLayerUid) {
            return null
        }

        val undoAction = GraphicsAction.MoveLayer(layer.uid, undoOnTopOfLayerUid)
        canvasLayers.removeAt(layerIndex)

        val insertIndex = if (action.onTopOfLayerUid == LayerUid.Background) {
            0
        } else {
            canvasLayers.indexOfFirst { it.uid.value == action.onTopOfLayerUid.value } + 1
        }

        canvasLayers.add(insertIndex, layer)
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

    fun executeMergeLayers(action: GraphicsAction.MergeLayers): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null
        val withLayer = canvasLayersMap[action.withLayerUid.value] ?: return null

        if (layer.isLocked || withLayer.isLocked || layer.canvas.type != withLayer.canvas.type) {
            return null
        }

        val layerIndex = canvasLayers.indexOfFirst { it.uid.value == layer.uid.value }
        val withLayerIndex = canvasLayers.indexOfFirst { it.uid.value == withLayer.uid.value }

        val undoAction = GraphicsAction.RestoreMergedLayers(
            layer = layer,
            onTopOfLayerUid = if (layerIndex > 0) canvasLayers[layerIndex - 1].uid else LayerUid.Background,
            withLayer = withLayer,
        )

        val mergeLayer = withLayer.copyMutable()

        val canvas = layer.canvas
        val mergeCanvas = mergeLayer.canvas

        mergeCanvas.mutate { mutator ->
            for (y in 0..<minOf(canvas.sciiHeight, mergeCanvas.sciiHeight)) {
                for (x in 0..<minOf(canvas.sciiWidth, mergeCanvas.sciiHeight)) {
                    mutator.replaceMergeCell(mergeCanvas.getMergeCell(x, y))
                }
            }
        }

        canvasLayers[withLayerIndex] = mergeLayer
        canvasLayers.removeAt(layerIndex)

        canvasLayersMap.remove(layer.uid.value)
        canvasLayersMap[withLayer.uid.value] = mergeLayer

        updatePreview(ScreenBox)
        return undoAction
    }

    fun execute(action: GraphicsAction): GraphicsAction? = when (action) {
        is GraphicsAction.SetBorderColor -> executeSetBorderColor(action)
        is GraphicsAction.SetBackgroundColor -> executeSetBackgroundColor(action)
        is GraphicsAction.SetBackgroundBright -> executeSetBackgroundBright(action)
        is GraphicsAction.SetBackgroundVisible -> executeSetBackgroundVisible(action)
        is GraphicsAction.SetBackgroundLocked -> executeSetBackgroundLocked(action)
        is GraphicsAction.CreateLayer -> executeCreateLayer(action)
        is GraphicsAction.RestoreLayer -> executeRestoreLayer(action)
        is GraphicsAction.DeleteLayer -> executeDeleteLayer(action)
        is GraphicsAction.SetLayerVisible -> executeSetLayerVisible(action)
        is GraphicsAction.SetLayerLocked -> executeSetLayerLocked(action)
        is GraphicsAction.MoveLayer -> executeMoveLayer(action)
        is GraphicsAction.DrawShape -> executeDrawShape(action)
        is GraphicsAction.ReplaceCells -> executeReplaceCells(action)

        is GraphicsAction.MergeLayers -> TODO()
        is GraphicsAction.RestoreMergedLayers -> TODO()
        is GraphicsAction.ConvertLayer -> TODO()
    }

    fun putInTheBag(bag: PackableBag) {
        bag.put(
            StateStuff(
                borderColor = borderColor,
                backgroundLayer = backgroundLayer,
                canvasLayers = canvasLayers,
            )
        )
    }

    fun getOutOfTheBag(bag: UnpackableBag) {
        val stateStuff = bag.getStuff(StateStuff.Companion)

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
            canvas = when (action.type) {
                CanvasType.Scii -> MutableSciiCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
                CanvasType.HBlock -> MutableHBlockCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
                CanvasType.VBlock -> MutableVBlockCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
                CanvasType.QBlock -> MutableQBlockCanvas(SCREEN_SCII_WIDTH, SCREEN_SCII_HEIGHT)
            }
        )

        val insertIndex = if (action.onTopOfLayerUid == LayerUid.Background) {
            0
        } else {
            canvasLayers.indexOfFirst { it.uid.value == action.onTopOfLayerUid.value } + 1
        }

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(insertIndex, layer)
        canvasLayersMap[layer.uid.value] = layer

        return undoAction
    }

    private fun executeRestoreLayer(action: GraphicsAction.RestoreLayer): GraphicsAction {
        val layer = action.layer.copyMutable()

        val insertIndex = if (action.onTopOfLayerUid == LayerUid.Background) {
            0
        } else {
            canvasLayers.indexOfFirst { it.uid.value == action.onTopOfLayerUid.value } + 1
        }

        val undoAction = GraphicsAction.DeleteLayer(layer.uid)
        canvasLayers.add(insertIndex, layer)
        canvasLayersMap[action.layer.uid.value] = layer

        updatePreview(ScreenBox)
        return undoAction
    }

    private fun executeDeleteLayer(action: GraphicsAction.DeleteLayer): GraphicsAction? {
        val layer = canvasLayersMap[action.layerUid.value] ?: return null

        if (layer.isLocked) {
            return null
        }

        val layerIndex = canvasLayers.indexOfFirst { it.uid.value == layer.uid.value }

        val undoAction = GraphicsAction.RestoreLayer(
            layer = layer,
            onTopOfLayerUid = if (layerIndex > 0) canvasLayers[layerIndex - 1].uid else LayerUid.Background,
        )

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

    private fun updatePreview(box: Box) {
        preview.mutate { mutator ->
            for (y in box.y..<(box.y + box.height)) {
                for (x in box.x..<(box.x + box.width)) {
                    var cell = backgroundLayer.sciiCell

                    for (layer in canvasLayers) {
                        cell = layer.canvas.getSciiCell(x, y).merge(cell)
                    }

                    mutator.replaceSciiCell(x, y, cell)
                }
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
    ) : BagStuff {
        override val bagStuffVersion = 1

        override fun putInTheBag(bag: PackableBag) {
            bag.put(borderColor.value)
            bag.put(backgroundLayer)
            bag.put(canvasLayers.size)

            for (layer in canvasLayers) {
                bag.put(layer)
            }
        }

        companion object : BagStuff.Unpacker<StateStuff> {
            override fun getOutOfTheBag(version: Int, bag: UnpackableBag): StateStuff {
                if (version != 1) {
                    throw BagUnpackException("Unsupported version=$version for GraphicsEngine")
                }

                val borderColor = SciiColor(bag.getInt())
                val backgroundLayer = bag.getStuff(MutableBackgroundLayer.Companion)

                val canvasLayers =
                    (0..<bag.getInt()).mapTo(mutableListOf()) { bag.getStuff(MutableCanvasLayer.Companion) }

                return StateStuff(
                    borderColor = borderColor,
                    backgroundLayer = backgroundLayer,
                    canvasLayers = canvasLayers,
                )
            }
        }
    }
}
