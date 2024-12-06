package com.eightsines.bpe.resources

import kotlin.jvm.JvmInline

object TextRes {
    val PALETTE_SELECT_COLOR = TextResId("palette_select_color")
    val PALETTE_SELECT_PAPER = TextResId("palette_select_paper")
    val PALETTE_SELECT_INK = TextResId("palette_select_ink")
    val PALETTE_SELECT_BRIGHT = TextResId("palette_select_bright")
    val PALETTE_SELECT_FLASH = TextResId("palette_select_flash")
    val PALETTE_SELECT_CHARACTER = TextResId("palette_select_character")

    val SELECTION_CUT = TextResId("selection_cut")
    val SELECTION_COPY = TextResId("selection_copy")
    val LAYERS = TextResId("layers")

    val TOOL_PAINT = TextResId("tool_paint")
    val TOOL_ERASE = TextResId("tool_erase")
    val TOOL_SELECT = TextResId("tool_select")
    val TOOL_PICK_COLOR = TextResId("tool_pick_color")

    val SHAPE_POINT = TextResId("shape_point")
    val SHAPE_LINE = TextResId("shape_line")
    val SHAPE_STROKE_BOX = TextResId("shape_stroke_box")
    val SHAPE_FILL_BOX = TextResId("shape_fill_box")

    val TOOLBOX_PASTE = TextResId("toolbox_paste")
    val TOOLBOX_UNDO = TextResId("toolbox_undo")
    val TOOLBOX_REDO = TextResId("toolbox_redo")
    val MENU = TextResId("menu")

    val LAYERS_CREATE = TextResId("layers_create")
    val LAYERS_MERGE = TextResId("layers_merge")
    val LAYERS_CONVERT = TextResId("layers_convert")
    val LAYERS_DELETE = TextResId("layers_delete")
    val LAYERS_MOVE_UP = TextResId("layers_move_up")
    val LAYERS_MOVE_DOWN = TextResId("layers_move_down")
    val LAYERS_UNDO_CREATE = TextResId("layers_undo_create")

    val MENU_LOAD = TextResId("menu_load")
    val MENU_SAVE = TextResId("menu_save")
    val MENU_EXPORT = TextResId("menu_export")

    val LAYER_VISIBLE = TextResId("layer_visible")
    val LAYER_INVISIBLE = TextResId("layer_invisible")
    val LAYER_LOCKED = TextResId("layer_locked")
    val LAYER_UNLOCKED = TextResId("layer_unlocked")

    val CANVAS_SCII = TextResId("canvas_scii")
    val CANVAS_HBLOCK = TextResId("canvas_hblock")
    val CANVAS_VBLOCK = TextResId("canvas_vblock")
    val CANVAS_QBLOCK = TextResId("canvas_qblock")

    val ALERT_READER_ERROR = TextResId("alert_reader_error")
    val ALERT_NULL_RESULT = TextResId("alert_null_result")
    val ALERT_UNPACK_ERROR = TextResId("alert_unpack_error")
    val ALERT_EXPORT_NOT_IMPLEMENTED = TextResId("alert_export_not_implemented")
}

@JvmInline
value class TextResId(val id: String)
