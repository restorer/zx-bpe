package com.eightsines.bpe.resources

class ResManager {
    fun resolveText(res: TextResId) = TEXT_MAP[res.id] ?: res.id

    private companion object {
        private val TEXT_MAP: Map<String, String> = buildMap {
            put(TextRes.PALETTE_SELECT_COLOR.id, "Select color")
            put(TextRes.PALETTE_SELECT_PAPER.id, "Select paper")
            put(TextRes.PALETTE_SELECT_INK.id, "Select ink")
            put(TextRes.PALETTE_SELECT_BRIGHT.id, "Select bright")
            put(TextRes.PALETTE_SELECT_FLASH.id, "Select flash")
            put(TextRes.PALETTE_SELECT_CHARACTER.id, "Select character")

            put(TextRes.SELECTION_CUT.id, "Cut")
            put(TextRes.SELECTION_COPY.id, "Copy")
            put(TextRes.LAYERS.id, "Layers")

            put(TextRes.TOOL_PAINT.id, "Paint")
            put(TextRes.TOOL_ERASE.id, "Erase")
            put(TextRes.TOOL_SELECT.id, "Select")
            put(TextRes.TOOL_PICK_COLOR.id, "Pick color")

            put(TextRes.SHAPE_POINT.id, "Point")
            put(TextRes.SHAPE_LINE.id, "Line")
            put(TextRes.SHAPE_STROKE_BOX.id, "Stroke box")
            put(TextRes.SHAPE_FILL_BOX.id, "Fill box")

            put(TextRes.TOOLBOX_PASTE.id, "Paste")
            put(TextRes.TOOLBOX_UNDO.id, "Undo")
            put(TextRes.TOOLBOX_REDO.id, "Redo")
            put(TextRes.MENU.id, "Menu")

            put(TextRes.LAYERS_CREATE.id, "Create layer")
            put(TextRes.LAYERS_MERGE.id, "Merge layer")
            put(TextRes.LAYERS_CONVERT.id, "Convert layer")
            put(TextRes.LAYERS_DELETE.id, "Delete layer")
            put(TextRes.LAYERS_MOVE_UP.id, "Move layer up")
            put(TextRes.LAYERS_MOVE_DOWN.id, "Move layer down")
            put(TextRes.LAYERS_UNDO_CREATE.id, "Undo create layer")

            put(TextRes.MENU_LOAD.id, "Load")
            put(TextRes.MENU_SAVE.id, "Save")
            put(TextRes.MENU_EXPORT.id, "Export")

            put(TextRes.LAYER_VISIBLE.id, "Visible")
            put(TextRes.LAYER_INVISIBLE.id, "Invisible")
            put(TextRes.LAYER_LOCKED.id, "Locked")
            put(TextRes.LAYER_UNLOCKED.id, "Unlocked")

            put(TextRes.CANVAS_SCII.id, "SpecSCII")
            put(TextRes.CANVAS_HBLOCK.id, "HBlock")
            put(TextRes.CANVAS_VBLOCK.id, "VBlock")
            put(TextRes.CANVAS_QBLOCK.id, "QBlock")

            put(TextRes.ALERT_READER_ERROR.id, "Failed to read painting (reader error).")
            put(TextRes.ALERT_NULL_RESULT.id, "Failed to read painting (result is null).")
            put(TextRes.ALERT_UNPACK_ERROR.id, "Failed to read painting (unpack).")
            put(TextRes.ALERT_EXPORT_NOT_IMPLEMENTED.id, "Export is not implemented yet.")
        }
    }
}
