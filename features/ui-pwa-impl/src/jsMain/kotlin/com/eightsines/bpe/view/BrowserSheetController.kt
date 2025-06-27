package com.eightsines.bpe.view

class BrowserSheetController {
    fun getSheetBbox(drawingWidth: Int, drawingHeight: Int, transform: DrawingTransform): BBox? {
        val ratio = computeRatio(drawingWidth, drawingHeight) ?: return null

        val sheetScale = ratio * transform.scale
        val sheetWidth = DRAWING_SHEET_WIDTH * sheetScale
        val sheetHeight = DRAWING_SHEET_HEIGHT * sheetScale

        if (sheetWidth < 1.0 || sheetHeight < 1.0) {
            return null
        }

        val centerX = drawingWidth.toDouble() * 0.5 + DRAWING_SHEET_WIDTH * ratio * transform.translateXRatio
        val centerY = drawingHeight.toDouble() * 0.5 + DRAWING_SHEET_HEIGHT * ratio * transform.translateYRatio

        return BBox(
            lx = (centerX - sheetWidth * 0.5).toInt(),
            ly = (centerY - sheetHeight * 0.5).toInt(),
            width = sheetWidth.toInt(),
            height = sheetHeight.toInt(),
        )
    }

    fun translateToSheet(drawingX: Int, drawingY: Int, drawingWidth: Int, drawingHeight: Int, transform: DrawingTransform): Pair<Int, Int>? {
        val bbox = getSheetBbox(drawingWidth, drawingHeight, transform) ?: return null

        val sheetX = (drawingX - bbox.lx) * DRAWING_SHEET_WIDTH / bbox.width
        val sheetY = (drawingY - bbox.ly) * DRAWING_SHEET_HEIGHT / bbox.height

        return sheetX.toInt() to sheetY.toInt()
    }

    fun computeTransform(
        drawingX: Int,
        drawingY: Int,
        drawingWidth: Int,
        drawingHeight: Int,
        srcSheetX: Int,
        srcSheetY: Int,
        newScale: Double,
    ): DrawingTransform? {
        val ratio = computeRatio(drawingWidth, drawingHeight) ?: return null
        val scale = newScale.coerceIn(SCALE_MIN, SCALE_MAX)

        val sheetScale = ratio * scale
        val sheetWidth = DRAWING_SHEET_WIDTH * sheetScale
        val sheetHeight = DRAWING_SHEET_HEIGHT * sheetScale

        if (sheetWidth < 1.0 || sheetHeight < 1.0) {
            return null
        }

        val centerX = (drawingX.toDouble() - srcSheetX.toDouble() * sheetWidth / DRAWING_SHEET_WIDTH) + sheetWidth * 0.5
        val centerY = (drawingY.toDouble() - srcSheetY.toDouble() * sheetHeight / DRAWING_SHEET_HEIGHT) + sheetHeight * 0.5
        val limit = (scale - 1.0) * 0.5 + 1.0

        return DrawingTransform(
            translateXRatio = ((centerX - drawingWidth.toDouble() * 0.5) / DRAWING_SHEET_WIDTH / ratio).coerceIn(-limit, limit),
            translateYRatio = ((centerY - drawingHeight.toDouble() * 0.5) / DRAWING_SHEET_HEIGHT / ratio).coerceIn(-limit, limit),
            scale = scale,
        )
    }

    private fun computeRatio(drawingWidth: Int, drawingHeight: Int): Double? {
        val drawingAvailWidth = (drawingWidth - DRAWING_SHEET_OFFSET_X2).toDouble()
        val drawingAvailHeight = (drawingHeight - DRAWING_SHEET_OFFSET_X2).toDouble()

        if (drawingAvailWidth < 1 || drawingAvailHeight < 1) {
            return null
        }

        val drawingRatio = drawingAvailWidth / drawingAvailHeight
        val sheetRatio = DRAWING_SHEET_WIDTH / DRAWING_SHEET_HEIGHT

        return if (drawingRatio < sheetRatio) {
            drawingAvailWidth / DRAWING_SHEET_WIDTH
        } else {
            drawingAvailHeight / DRAWING_SHEET_HEIGHT
        }
    }

    companion object {
        const val SELECTOR_DRAWING_SHEET = ".js-drawing-sheet"

        private const val DRAWING_SHEET_OFFSET_X2 = 16 * 2

        private const val DRAWING_SHEET_WIDTH = 320.0
        private const val DRAWING_SHEET_HEIGHT = 256.0

        private const val SCALE_MIN = 0.25
        private const val SCALE_MAX = 4.0
    }
}

data class BBox(val lx: Int, val ly: Int, val width: Int, val height: Int)
