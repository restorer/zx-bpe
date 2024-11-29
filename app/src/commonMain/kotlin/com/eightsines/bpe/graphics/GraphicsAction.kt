package com.eightsines.bpe.graphics

import com.eightsines.bpe.foundation.CanvasLayer
import com.eightsines.bpe.foundation.CanvasType
import com.eightsines.bpe.foundation.LayerUid
import com.eightsines.bpe.foundation.Crate
import com.eightsines.bpe.core.Cell
import com.eightsines.bpe.core.SciiCell
import com.eightsines.bpe.core.SciiColor
import com.eightsines.bpe.core.SciiLight

sealed interface GraphicsAction {
    data class SetBackgroundBorder(val color: SciiColor) : GraphicsAction
    data class SetBackgroundColor(val color: SciiColor) : GraphicsAction
    data class SetBackgroundBright(val light: SciiLight) : GraphicsAction
    data class SetBackgroundVisible(val isVisible: Boolean) : GraphicsAction
    data class SetBackgroundLocked(val isLocked: Boolean) : GraphicsAction
    data class CreateLayer(val canvasType: CanvasType, val layerUid: LayerUid, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class ReplaceLayer(val layer: CanvasLayer<*>) : GraphicsAction
    data class InsertLayer(val layer: CanvasLayer<*>, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class DeleteLayer(val layerUid: LayerUid) : GraphicsAction
    data class SetLayerVisible(val layerUid: LayerUid, val isVisible: Boolean) : GraphicsAction
    data class SetLayerLocked(val layerUid: LayerUid, val isLocked: Boolean) : GraphicsAction
    data class MoveLayer(val layerUid: LayerUid, val onTopOfLayerUid: LayerUid) : GraphicsAction
    data class MergeShape(val layerUid: LayerUid, val shape: Shape<Cell>) : GraphicsAction
    data class ReplaceShape(val layerUid: LayerUid, val shape: Shape<Cell>) : GraphicsAction

    data class ReplaceCells(
        val layerUid: LayerUid,
        val x: Int,
        val y: Int,
        val crate: Crate<SciiCell>,
    ) : GraphicsAction

    data class MergeLayers(val layerUid: LayerUid, val ontoLayerUid: LayerUid) : GraphicsAction

    data class UndoMergeLayers(
        val insertLayer: CanvasLayer<*>,
        val insertOnTopOfLayerUid: LayerUid,
        val replaceLayer: CanvasLayer<*>,
    ) : GraphicsAction

    data class ConvertLayer(val layerUid: LayerUid, val canvasType: CanvasType) : GraphicsAction
}

data class GraphicsActionPair(val action: GraphicsAction, val undoAction: GraphicsAction)
