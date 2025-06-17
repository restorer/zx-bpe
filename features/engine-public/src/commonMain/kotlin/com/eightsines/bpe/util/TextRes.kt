package com.eightsines.bpe.util

enum class TextRes(val id: String) {
    PaletteSelectColor("palette_select_color"),
    PaletteSelectPaper("palette_select_paper"),
    PaletteSelectInk("palette_select_ink"),
    PaletteSelectBright("palette_select_bright"),
    PaletteSelectFlash("palette_select_flash"),
    PaletteSelectCharacter("palette_select_character"),

    SelectionPaste("selection_paste"),
    SelectionMenu("selection_menu"),
    SelectionCut("selection_cut"),
    SelectionCopy("selection_copy"),
    SelectionFlipHorizontal("selection_flip_horizontal"),
    SelectionFlipVertical("selection_flip_vertical"),
    SelectionRotateCw("selection_rotate_cw"),
    SelectionRotateCcw("selection_rotate_ccw"),
    SelectionFill("selection_fill"),
    SelectionClear("selection_clear"),
    Layers("layers"),

    ToolPaint("tool_paint"),
    ToolErase("tool_erase"),
    ToolSelect("tool_select"),
    ToolPickColor("tool_pick_color"),

    ShapePoint("shape_point"),
    ShapeLine("shape_line"),
    ShapeStrokeBox("shape_stroke_box"),
    ShapeFillBox("shape_fill_box"),
    ShapeStrokeEllipse("shape_stroke_ellipse"),
    ShapeFillEllipse("shape_fill_ellipse"),

    ToolboxUndo("toolbox_undo"),
    ToolboxRedo("toolbox_redo"),
    ToolboxModeEdge("toolbox_mode_edge"),
    ToolboxModeCenter("toolbox_mode_center"),
    Menu("menu"),

    LayersCreate("layers_create"),
    LayersCreateCancel("layers_create_cancel"),
    LayersMerge("layers_merge"),
    LayersConvert("layers_convert"),
    LayersConvertCancel("layers_convert_cancel"),
    LayersDelete("layers_delete"),
    LayersMoveUp("layers_move_up"),
    LayersMoveDown("layers_move_down"),

    MenuNew("menu_new"),
    MenuLoad("menu_load"),
    MenuSave("menu_save"),
    MenuExportTap("menu_export_tap"),
    MenuExportScr("menu_export_scr"),
    MenuExportPng("menu_export_png"),

    LayerVisible("layer_visible"),
    LayerInvisible("layer_invisible"),
    LayerLocked("layer_locked"),
    LayerUnlocked("layer_unlocked"),
    LayerMasked("layer_masked"),
    LayerUnmasked("layer_unmasked"),

    CanvasScii("canvas_scii"),
    CanvasHBlock("canvas_hblock"),
    CanvasVBlock("canvas_vblock"),
    CanvasQBlock("canvas_qblock"),

    DialogOk("dialog_ok"),
    DialogCancel("dialog_cancel"),

    AlertLoadReaderError("alert_load_reader_error"),
    AlertLoadNullResult("alert_load_null_result"),
    AlertLoadUnpackError("alert_load_unpack_error"),
    AlertExportNotImplemented("alert_export_not_implemented"),

    ConfirmNew("confirm_new"),

    PromptSaveMessage("prompt_save_message"),
    PromptSaveHint("prompt_save_hint"),

    InformerSecondary("informer_short"),
    InformerPrimary("informer_full");

    companion object {
        private var entryMap: Map<String, TextRes>? = null

        fun of(id: String) =
            (entryMap ?: entries.associateBy(TextRes::id).also { entryMap = it })[id]
    }
}