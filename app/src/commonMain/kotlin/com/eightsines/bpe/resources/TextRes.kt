package com.eightsines.bpe.resources

import kotlin.jvm.JvmInline

object TextRes {
    val PaletteSelectColor = TextResId("palette_select_color")
    val PaletteSelectPaper = TextResId("palette_select_paper")
    val PaletteSelectInk = TextResId("palette_select_ink")
    val PaletteSelectBright = TextResId("palette_select_bright")
    val PaletteSelectFlash = TextResId("palette_select_flash")
    val PaletteSelectCharacter = TextResId("palette_select_character")

    val SelectionCut = TextResId("selection_cut")
    val SelectionCopy = TextResId("selection_copy")
    val Layers = TextResId("layers")

    val ToolPaint = TextResId("tool_paint")
    val ToolErase = TextResId("tool_erase")
    val ToolSelect = TextResId("tool_select")
    val ToolPickColor = TextResId("tool_pick_color")

    val ShapePoint = TextResId("shape_point")
    val ShapeLine = TextResId("shape_line")
    val ShapeStrokeBox = TextResId("shape_stroke_box")
    val ShapeFillBox = TextResId("shape_fill_box")

    val ToolboxPaste = TextResId("toolbox_paste")
    val ToolboxUndo = TextResId("toolbox_undo")
    val ToolboxRedo = TextResId("toolbox_redo")
    val Menu = TextResId("menu")

    val LAYERS_CREATE = TextResId("layers_create")
    val LAYERS_MERGE = TextResId("layers_merge")
    val LAYERS_CONVERT = TextResId("layers_convert")
    val LAYERS_DELETE = TextResId("layers_delete")
    val LAYERS_MOVE_UP = TextResId("layers_move_up")
    val LAYERS_MOVE_DOWN = TextResId("layers_move_down")
    val LAYERS_UNDO_CREATE = TextResId("layers_undo_create")

    val MenuLoad = TextResId("menu_load")
    val MenuSave = TextResId("menu_save")
    val MenuExport = TextResId("menu_export")

    val LayerVisible = TextResId("layer_visible")
    val LayerInvisible = TextResId("layer_invisible")
    val LayerLocked = TextResId("layer_locked")
    val LayerUnlocked = TextResId("layer_unlocked")

    val CanvasScii = TextResId("canvas_scii")
    val CanvasHBlock = TextResId("canvas_hblock")
    val CanvasVBlock = TextResId("canvas_vblock")
    val CanvasQBlock = TextResId("canvas_qblock")

    val AlertLoadReaderError = TextResId("alert_load_reader_error")
    val AlertLoadNullResult = TextResId("alert_load_null_result")
    val AlertLoadUnpackError = TextResId("alert_load_unpack_error")
    val AlertExportNotImplemented = TextResId("alert_export_not_implemented")
}

@JvmInline
value class TextResId(val id: String)
