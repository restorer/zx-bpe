package com.eightsines.bpe.resources

class ResourceManager {
    fun resolveText(res: TextResId) = TEXT_MAP[res.id] ?: res.id

    private companion object {
        private val TEXT_MAP: Map<String, String> = buildMap {
            put(TextRes.PaletteSelectColor.id, "Select color")
            put(TextRes.PaletteSelectPaper.id, "Select paper")
            put(TextRes.PaletteSelectInk.id, "Select ink")
            put(TextRes.PaletteSelectBright.id, "Select bright")
            put(TextRes.PaletteSelectFlash.id, "Select flash")
            put(TextRes.PaletteSelectCharacter.id, "Select character")

            put(TextRes.SelectionCut.id, "Cut")
            put(TextRes.SelectionCopy.id, "Copy")
            put(TextRes.Layers.id, "Layers")

            put(TextRes.ToolPaint.id, "Paint")
            put(TextRes.ToolErase.id, "Erase")
            put(TextRes.ToolSelect.id, "Select")
            put(TextRes.ToolPickColor.id, "Pick color")

            put(TextRes.ShapePoint.id, "Point")
            put(TextRes.ShapeLine.id, "Line")
            put(TextRes.ShapeStrokeBox.id, "Stroke box")
            put(TextRes.ShapeFillBox.id, "Fill box")

            put(TextRes.ToolboxPaste.id, "Paste")
            put(TextRes.ToolboxUndo.id, "Undo")
            put(TextRes.ToolboxRedo.id, "Redo")
            put(TextRes.Menu.id, "Menu")

            put(TextRes.LAYERS_CREATE.id, "Create layer")
            put(TextRes.LAYERS_MERGE.id, "Merge layer")
            put(TextRes.LAYERS_CONVERT.id, "Convert layer")
            put(TextRes.LAYERS_DELETE.id, "Delete layer")
            put(TextRes.LAYERS_MOVE_UP.id, "Move layer up")
            put(TextRes.LAYERS_MOVE_DOWN.id, "Move layer down")
            put(TextRes.LAYERS_UNDO_CREATE.id, "Undo create layer")

            put(TextRes.MenuLoad.id, "Load")
            put(TextRes.MenuSave.id, "Save")
            put(TextRes.MenuExport.id, "Export")

            put(TextRes.LayerVisible.id, "Visible")
            put(TextRes.LayerInvisible.id, "Invisible")
            put(TextRes.LayerLocked.id, "Locked")
            put(TextRes.LayerUnlocked.id, "Unlocked")

            put(TextRes.CanvasScii.id, "SpecSCII")
            put(TextRes.CanvasHBlock.id, "HBlock")
            put(TextRes.CanvasVBlock.id, "VBlock")
            put(TextRes.CanvasQBlock.id, "QBlock")

            put(TextRes.AlertLoadReaderError.id, "Failed to read painting (reader error).")
            put(TextRes.AlertLoadNullResult.id, "Failed to read painting (result is null).")
            put(TextRes.AlertLoadUnpackError.id, "Failed to read painting (unpack).")
            put(TextRes.AlertExportNotImplemented.id, "Export is not implemented yet.")
        }
    }
}
