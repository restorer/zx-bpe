package com.eightsines.bpe.engine.graphics

import com.eightsines.bpe.engine.canvas.CanvasType
import com.eightsines.bpe.engine.cell.Cell
import com.eightsines.bpe.engine.cell.SciiCell
import com.eightsines.bpe.engine.data.SciiColor
import com.eightsines.bpe.engine.data.SciiLight
import com.eightsines.bpe.engine.layer.CanvasLayer
import com.eightsines.bpe.engine.layer.LayerUid

sealed interface GraphicsAction {
    data class SetBorderColor(val color: SciiColor) : GraphicsAction
    data class SetBackgroundColor(val color: SciiColor) : GraphicsAction
    data class SetBackgroundBright(val light: SciiLight) : GraphicsAction
    data class SetBackgroundVisible(val isVisible: Boolean) : GraphicsAction
    data class SetBackgroundLocked(val isLocked: Boolean) : GraphicsAction
    data class CreateLayer(val type: CanvasType, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class RestoreLayer(val layer: CanvasLayer<*>, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class DeleteLayer(val layerUid: LayerUid) : GraphicsAction
    data class SetLayerVisible(val layerUid: LayerUid, val isVisible: Boolean) : GraphicsAction
    data class SetLayerLocked(val layerUid: LayerUid, val isLocked: Boolean) : GraphicsAction
    data class MoveLayer(val layerUid: LayerUid, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class DrawShape(val layerUid: LayerUid, val shape: Shape<Cell>) : GraphicsAction

    data class ReplaceCells(
        val layerUid: LayerUid,
        val x: Int,
        val y: Int,
        val crate: Crate<SciiCell>,
    ) : GraphicsAction

    data class MergeLayers(val layerUid: LayerUid, val withLayerUid: LayerUid) : GraphicsAction

    data class RestoreMergedLayers(
        val layer: CanvasLayer<*>,
        val onTopOfLayerUid: LayerUid,
        val withLayer: CanvasLayer<*>,
    ) : GraphicsAction

    data class ConvertLayer(val layerUid: LayerUid, val canvasType: CanvasType) : GraphicsAction
}
