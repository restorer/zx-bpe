package com.eightsines.bpe.resources

import com.eightsines.bpe.util.TextDescriptor
import com.eightsines.bpe.util.TextRes

class ResourceManager {
    fun resolveText(res: TextRes) = requireNotNull(textMap[res.id])

    fun resolveText(textDescriptor: TextDescriptor): String {
        var result = requireNotNull(textMap[textDescriptor.res.id])

        for ((key, value) in textDescriptor.params) {
            result = result.replace("{${key}}", value)
        }

        return result
    }

    private companion object {
        private val textMap = buildMap {
            for (res in TextRes.entries) {
                put(res.id, resolveTextInternal(res))
            }
        }

        private fun resolveTextInternal(res: TextRes) = when (res) {
            TextRes.PaletteSelectColor -> "Select color"
            TextRes.PaletteSelectPaper -> "Select paper"
            TextRes.PaletteSelectInk -> "Select ink"
            TextRes.PaletteSelectBright -> "Select bright"
            TextRes.PaletteSelectFlash -> "Select flash"
            TextRes.PaletteSelectCharacter -> "Select character"

            TextRes.SelectionPaste -> "Paste"
            TextRes.SelectionMenu -> "Actions"
            TextRes.SelectionCut -> "Cut"
            TextRes.SelectionCopy -> "Copy"
            TextRes.SelectionFlipHorizontal -> "Flip horizontal"
            TextRes.SelectionFlipVertical -> "Flip vertical"
            TextRes.SelectionRotateCw -> "Rotate right"
            TextRes.SelectionRotateCcw -> "Rotate left"
            TextRes.Layers -> "Layers"

            TextRes.ToolPaint -> "Paint"
            TextRes.ToolErase -> "Erase"
            TextRes.ToolSelect -> "Select"
            TextRes.ToolPickColor -> "Pick color"

            TextRes.ShapePoint -> "Point"
            TextRes.ShapeLine -> "Line"
            TextRes.ShapeStrokeBox -> "Stroke box"
            TextRes.ShapeFillBox -> "Fill box"
            TextRes.ShapeStrokeEllipse -> "Stroke ellipse"
            TextRes.ShapeFillEllipse -> "Fill ellipse"

            TextRes.ToolboxUndo -> "Undo"
            TextRes.ToolboxRedo -> "Redo"
            TextRes.Menu -> "Menu"

            TextRes.LayersCreate -> "Create layer"
            TextRes.LayersCreateCancel -> "Cancel create layer"
            TextRes.LayersMerge -> "Merge layer"
            TextRes.LayersConvert -> "Convert layer"
            TextRes.LayersConvertCancel -> "Cancel convert layer"
            TextRes.LayersDelete -> "Delete layer"
            TextRes.LayersMoveUp -> "Move layer up"
            TextRes.LayersMoveDown -> "Move layer down"

            TextRes.MenuNew -> "New"
            TextRes.MenuLoad -> "Load"
            TextRes.MenuSave -> "Save"
            TextRes.MenuExportTap -> "Export to TAP"
            TextRes.MenuExportScr -> "Export to SCR"
            TextRes.MenuExportPng -> "Export to PNG"

            TextRes.LayerVisible -> "Visible"
            TextRes.LayerInvisible -> "Invisible"
            TextRes.LayerLocked -> "Locked"
            TextRes.LayerUnlocked -> "Unlocked"
            TextRes.LayerMasked -> "Masked"
            TextRes.LayerUnmasked -> "Unmasked"

            TextRes.CanvasScii -> "SpecSCII"
            TextRes.CanvasHBlock -> "HBlock"
            TextRes.CanvasVBlock -> "VBlock"
            TextRes.CanvasQBlock -> "QBlock"

            TextRes.ToolboxModeEdge -> "Edge mode"
            TextRes.ToolboxModeCenter -> "Center mode"

            TextRes.DialogOk -> "OK"
            TextRes.DialogCancel -> "Cancel"

            TextRes.AlertLoadReaderError -> "Failed to read painting (reader error)."
            TextRes.AlertLoadNullResult -> "Failed to read painting (result is null)."
            TextRes.AlertLoadUnpackError -> "Failed to read painting (unpack)."
            TextRes.AlertExportNotImplemented -> "Export is not implemented yet."

            TextRes.ConfirmNew -> "Clear everything? This action cannot be undone."

            TextRes.PromptSaveMessage -> "File name (without {ext} extension):"
            TextRes.PromptSaveHint -> "Should be between 1 and {len} characters and contains only 0-9, A-Z, a-z or {specials}"

            TextRes.InformerSecondary -> "XY {x} {y}"
            TextRes.InformerPrimary -> "WH {w}×{h}"
        }
    }
}
