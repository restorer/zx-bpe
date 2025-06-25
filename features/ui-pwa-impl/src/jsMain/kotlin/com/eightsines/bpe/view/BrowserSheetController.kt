package com.eightsines.bpe.view

class BrowserSheetController {
    fun getSheetBbox(drawingWidth: Int, drawingHeight: Int, transform: DrawingTransform): BBox? {
        val drawingAvailWidth = (drawingWidth - DRAWING_SHEET_OFFSET_X2).toDouble()
        val drawingAvailHeight = (drawingHeight - DRAWING_SHEET_OFFSET_X2).toDouble()

        if (drawingAvailWidth < 1 || drawingAvailHeight < 1) {
            return null
        }

        val drawingRatio = drawingAvailWidth / drawingAvailHeight
        val sheetRatio = DRAWING_SHEET_WIDTH_D / DRAWING_SHEET_HEIGHT_D

        val ratio = if (drawingRatio < sheetRatio) {
            drawingAvailWidth / DRAWING_SHEET_WIDTH_D
        } else {
            drawingAvailHeight / DRAWING_SHEET_HEIGHT_D
        }

        val centerX = drawingWidth.toDouble() * 0.5 + DRAWING_SHEET_WIDTH_D * ratio * transform.translateXRatio
        val centerY = drawingHeight.toDouble() * 0.5 + DRAWING_SHEET_HEIGHT_D * ratio * transform.translateYRatio

        val scale = ratio * transform.scale
        val sheetWidth = DRAWING_SHEET_WIDTH_D * scale
        val sheetHeight = DRAWING_SHEET_HEIGHT_D * scale

        return BBox(
            lx = (centerX - sheetWidth * 0.5).toInt(),
            ly = (centerY - sheetHeight * 0.5).toInt(),
            width = sheetWidth.toInt(),
            height = sheetHeight.toInt(),
        )
    }

    fun translateToSheet(drawingX: Int, drawingY: Int, bbox: BBox): Pair<Int, Int>? =
        if (bbox.width < 1 || bbox.height < 1) {
            null
        } else {
            ((drawingX - bbox.lx) * DRAWING_SHEET_WIDTH / bbox.width) to ((drawingY - bbox.ly) * DRAWING_SHEET_HEIGHT / bbox.height)
        }

    companion object {
        const val SELECTOR_DRAWING_SHEET = ".js-drawing-sheet"

        private const val DRAWING_SHEET_OFFSET_X2 = 16 * 2
        private const val DRAWING_SHEET_WIDTH = 320
        private const val DRAWING_SHEET_HEIGHT = 256

        const val DRAWING_SHEET_WIDTH_D = DRAWING_SHEET_WIDTH.toDouble()
        const val DRAWING_SHEET_HEIGHT_D = DRAWING_SHEET_HEIGHT.toDouble()
    }
}

data class BBox(val lx: Int, val ly: Int, val width: Int, val height: Int)
