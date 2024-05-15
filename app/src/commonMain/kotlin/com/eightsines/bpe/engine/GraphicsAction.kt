package com.eightsines.bpe.engine

import com.eightsines.bpe.model.Cell
import com.eightsines.bpe.model.SciiCell
import com.eightsines.bpe.model.SciiColor
import com.eightsines.bpe.model.SciiLight
import com.eightsines.bpe.graphics.CanvasType
import com.eightsines.bpe.graphics.Crate
import com.eightsines.bpe.graphics.Shape
import com.eightsines.bpe.layer.CanvasLayer
import com.eightsines.bpe.layer.LayerUid

sealed interface GraphicsAction {
    data class SetBorderColor(val color: SciiColor) : GraphicsAction
    data class SetBackgroundColor(val color: SciiColor) : GraphicsAction
    data class SetBackgroundBright(val light: SciiLight) : GraphicsAction
    data class SetBackgroundVisible(val isVisible: Boolean) : GraphicsAction
    data class SetBackgroundLocked(val isLocked: Boolean) : GraphicsAction
    data class CreateLayer(val canvasType: CanvasType, val onTopOfLayerUid: LayerUid) : GraphicsAction
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
