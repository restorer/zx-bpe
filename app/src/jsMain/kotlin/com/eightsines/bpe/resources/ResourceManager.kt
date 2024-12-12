package com.eightsines.bpe.resources

class ResourceManager {
    fun resolveText(res: TextResId) = TEXT_MAP[res.id] ?: res.id

    fun resolveText(textDescriptor: TextDescriptor): String {
        var result = TEXT_MAP[textDescriptor.res.id] ?: textDescriptor.res.id

        for ((key, value) in textDescriptor.params) {
            result = result.replace("{${key}}", value)
        }

        return result
    }

    private companion object {
        private val TEXT_MAP: Map<String, String> = buildMap {
            put(TextRes.PaletteSelectColor.id, "Select color")
            put(TextRes.PaletteSelectPaper.id, "Select paper")
            put(TextRes.PaletteSelectInk.id, "Select ink")
            put(TextRes.PaletteSelectBright.id, "Select bright")
            put(TextRes.PaletteSelectFlash.id, "Select flash")
            put(TextRes.PaletteSelectCharacter.id, "Select character")

            put(TextRes.SelectionMenu.id, "Actions")
            put(TextRes.SelectionCut.id, "Cut")
            put(TextRes.SelectionCopy.id, "Copy")
            put(TextRes.SelectionFlipHorizontal.id, "Flip horizontal")
            put(TextRes.SelectionFlipVertical.id, "Flip vertical")
            put(TextRes.SelectionRotateCw.id, "Rotate right")
            put(TextRes.SelectionRotateCcw.id, "Rotate left")
            put(TextRes.Layers.id, "Layers")

            put(TextRes.ToolPaint.id, "Paint")
            put(TextRes.ToolErase.id, "Erase")
            put(TextRes.ToolSelect.id, "Select")
            put(TextRes.ToolPickColor.id, "Pick color")

            put(TextRes.ShapePoint.id, "Point")
            put(TextRes.ShapeLine.id, "Line")
            put(TextRes.ShapeStrokeBox.id, "Stroke box")
            put(TextRes.ShapeFillBox.id, "Fill box")
            put(TextRes.ShapeStrokeEllipse.id, "Stroke ellipse")
            put(TextRes.ShapeFillEllipse.id, "Fill ellipse")

            put(TextRes.ToolboxPaste.id, "Paste")
            put(TextRes.ToolboxUndo.id, "Undo")
            put(TextRes.ToolboxRedo.id, "Redo")
            put(TextRes.Menu.id, "Menu")

            put(TextRes.LayersCreate.id, "Create layer")
            put(TextRes.LayersCreateCancel.id, "Cancel create layer")
            put(TextRes.LayersMerge.id, "Merge layer")
            put(TextRes.LayersConvert.id, "Convert layer")
            put(TextRes.LayersConvertCancel.id, "Cancel convert layer")
            put(TextRes.LayersDelete.id, "Delete layer")
            put(TextRes.LayersMoveUp.id, "Move layer up")
            put(TextRes.LayersMoveDown.id, "Move layer down")

            put(TextRes.MenuLoad.id, "Load")
            put(TextRes.MenuSave.id, "Save")
            put(TextRes.MenuExport.id, "Export")

            put(TextRes.LayerVisible.id, "Visible")
            put(TextRes.LayerInvisible.id, "Invisible")
            put(TextRes.LayerLocked.id, "Locked")
            put(TextRes.LayerUnlocked.id, "Unlocked")
            put(TextRes.LayerMasked.id, "Masked")
            put(TextRes.LayerUnmasked.id, "Unmasked")

            put(TextRes.CanvasScii.id, "SpecSCII")
            put(TextRes.CanvasHBlock.id, "HBlock")
            put(TextRes.CanvasVBlock.id, "VBlock")
            put(TextRes.CanvasQBlock.id, "QBlock")

            put(TextRes.PaintingModeEdge.id, "Edge mode")
            put(TextRes.PaintingModeCenter.id, "Center mode")

            put(TextRes.AlertLoadReaderError.id, "Failed to read painting (reader error).")
            put(TextRes.AlertLoadNullResult.id, "Failed to read painting (result is null).")
            put(TextRes.AlertLoadUnpackError.id, "Failed to read painting (unpack).")
            put(TextRes.AlertExportNotImplemented.id, "Export is not implemented yet.")

            put(TextRes.InformerShort.id, "{x} {y}")
            put(TextRes.InformerFull.id, "{x} {y} — {w}×{h}")
        }
    }
}
