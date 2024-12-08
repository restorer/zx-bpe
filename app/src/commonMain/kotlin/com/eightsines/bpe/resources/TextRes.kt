package com.eightsines.bpe.resources

import kotlin.jvm.JvmInline

object TextRes {
    val PaletteSelectColor = TextResId("palette_select_color")
    val PaletteSelectPaper = TextResId("palette_select_paper")
    val PaletteSelectInk = TextResId("palette_select_ink")
    val PaletteSelectBright = TextResId("palette_select_bright")
    val PaletteSelectFlash = TextResId("palette_select_flash")
    val PaletteSelectCharacter = TextResId("palette_select_character")

    val SelectionMenu = TextResId("selection_menu")
    val SelectionCut = TextResId("selection_cut")
    val SelectionCopy = TextResId("selection_copy")
    val SelectionFlipHorizontal = TextResId("selection_flip_horizontal")
    val SelectionFlipVertical = TextResId("selection_flip_vertical")
    val SelectionRotateCw = TextResId("selection_rotate_cw")
    val SelectionRotateCcw = TextResId("selection_rotate_ccw")
    val Layers = TextResId("layers")

    val ToolPaint = TextResId("tool_paint")
    val ToolErase = TextResId("tool_erase")
    val ToolSelect = TextResId("tool_select")
    val ToolPickColor = TextResId("tool_pick_color")

    val ShapePoint = TextResId("shape_point")
    val ShapeLine = TextResId("shape_line")
    val ShapeStrokeBox = TextResId("shape_stroke_box")
    val ShapeFillBox = TextResId("shape_fill_box")
    val ShapeStrokeOval = TextResId("shape_stroke_oval")
    val ShapeFillOval = TextResId("shape_fill_oval")

    val ToolboxPaste = TextResId("toolbox_paste")
    val ToolboxUndo = TextResId("toolbox_undo")
    val ToolboxRedo = TextResId("toolbox_redo")
    val Menu = TextResId("menu")

    val LayersCreate = TextResId("layers_create")
    val LayersCreateCancel = TextResId("layers_create_cancel")
    val LayersMerge = TextResId("layers_merge")
    val LayersConvert = TextResId("layers_convert")
    val LayersConvertCancel = TextResId("layers_convert_cancel")
    val LayersDelete = TextResId("layers_delete")
    val LayersMoveUp = TextResId("layers_move_up")
    val LayersMoveDown = TextResId("layers_move_down")

    val MenuLoad = TextResId("menu_load")
    val MenuSave = TextResId("menu_save")
    val MenuExport = TextResId("menu_export")

    val LayerVisible = TextResId("layer_visible")
    val LayerInvisible = TextResId("layer_invisible")
    val LayerLocked = TextResId("layer_locked")
    val LayerUnlocked = TextResId("layer_unlocked")
    val LayerMasked = TextResId("layer_masked")
    val LayerUnmasked = TextResId("layer_unmasked")

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
