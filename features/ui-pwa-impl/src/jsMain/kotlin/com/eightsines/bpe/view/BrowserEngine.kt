package com.eightsines.bpe.view

import com.eightsines.bpe.bag.BagStuffPacker
import com.eightsines.bpe.bag.BagStuffUnpacker
import com.eightsines.bpe.bag.BagUnpackException
import com.eightsines.bpe.bag.PackableBag
import com.eightsines.bpe.bag.PackableStringBag
import com.eightsines.bpe.bag.UnpackableBag
import com.eightsines.bpe.bag.UnpackableStringBag
import com.eightsines.bpe.bag.requireSupportedStuffVersion
import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.util.KeyCode
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.Severity
import com.eightsines.bpe.util.TextDescriptor
import com.eightsines.bpe.util.TextRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.w3c.dom.Document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Window
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

class BrowserEngine(
    private val logger: Logger,
    private val window: Window,
    private val document: Document,
    private val uiEngine: UiEngine,
    private val sheetController: BrowserSheetController,
    mainDispatcher: CoroutineDispatcher,
) {
    private val _browserStateFlow = MutableStateFlow(BrowserState(uiState = uiEngine.state, transform = DrawingTransform()))
    private val drawingSheet = document.querySelector(BrowserSheetController.SELECTOR_DRAWING_SHEET) as? HTMLCanvasElement
    private var isInitiallyLoaded = false
    private var fileName = DEFAULT_FILE_NAME
    private var lastDownBrowserKey: BrowserKey? = null
    private var lastDialogPromptInput = ""
    private var sheetMode: SheetMode = SheetMode.None
    private var lastDrawingPoints: List<Pair<Int, Int>> = emptyList()

    val browserStateFlow: Flow<BrowserState>
        get() = _browserStateFlow

    init {
        CoroutineScope(
            SupervisorJob() +
                    mainDispatcher +
                    CoroutineExceptionHandler { _, t -> logger.critical(t.toString()) }
        ).launch {
            @OptIn(FlowPreview::class)
            uiEngine.visuallyChangedFlow
                .filter { isInitiallyLoaded }
                .debounce(AUTOSAVE_TIMEOUT)
                .collect { trySaveToStorage() }
        }

        tryLoadFromStorage()
        isInitiallyLoaded = true
    }

    fun execute(action: BrowserAction) {
        val severity = if (action is BrowserAction.Ui ||
            action is BrowserAction.DrawingEnter ||
            action is BrowserAction.DrawingDown ||
            action is BrowserAction.DrawingMove ||
            action is BrowserAction.DrawingUp ||
            action is BrowserAction.DrawingLeave
        ) Severity.Trace else Severity.Note

        logger.log(severity, "BrowserEngine.execute") {
            put("action", action.toString())
        }

        when (action) {
            is BrowserAction.Ui -> executeUi(action.action)

            is BrowserAction.DrawingEnter -> executeDrawingEnter(action)
            is BrowserAction.DrawingDown -> executeDrawingDown(action)
            is BrowserAction.DrawingMove -> executeDrawingMove(action)
            is BrowserAction.DrawingUp -> executeDrawingUp(action)
            is BrowserAction.DrawingWheel -> executeDrawingWheel(action)
            is BrowserAction.DrawingLeave -> executeDrawingLeave()

            is BrowserAction.KeyDown -> executeKeyDown(action)
            is BrowserAction.KeyUp -> executeKeyUp(action)

            is BrowserAction.DialogHide -> executeDialogHide()
            is BrowserAction.DialogOk -> executeDialogOk()
            is BrowserAction.DialogPromptInput -> executeDialogPromptInput(action)

            is BrowserAction.PaintingNew -> executePaintingNew()
            is BrowserAction.PaintingLoad -> executePaintingLoad(action)
            is BrowserAction.PaintingSave -> executePaintingSave()
            is BrowserAction.PaintingExportTap -> executePaintingExportTap()
            is BrowserAction.PaintingExportScr -> executePaintingExportScr()
            is BrowserAction.PaintingExportPng -> executePaintingExportPng()
        }
    }

    private fun executeUi(action: UiAction) {
        uiEngine.execute(action)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
    }

    private fun executeDrawingEnter(action: BrowserAction.DrawingEnter) {
        val point = translateToSheet(action.x, action.y, action.width, action.height) ?: return

        sheetMode = SheetMode.Sheet
        lastDrawingPoints = listOf(action.x to action.y)

        executeUi(UiAction.SheetEnter(point.first, point.second))
    }

    private fun executeDrawingDown(action: BrowserAction.DrawingDown) {
        if (action.points.isEmpty()) {
            return
        }

        lastDrawingPoints = action.points

        if (action.points.size > 1) {
            val drawingPoint1 = action.points[0]
            val drawingPoint2 = action.points[1]

            val drawingX = (drawingPoint1.first + drawingPoint2.first) / 2
            val drawingY = (drawingPoint1.second + drawingPoint2.second) / 2

            val sheetPoint = translateToSheet(drawingX, drawingY, action.width, action.height)
                ?: return

            val offsetX = drawingPoint2.first - drawingPoint1.first
            val offsetY = drawingPoint2.second - drawingPoint1.second
            val distance = sqrt((offsetX * offsetX + offsetY * offsetY).toDouble())

            if (sheetMode is SheetMode.Sheet || sheetMode is SheetMode.SheetScale) {
                executeUi(UiAction.SheetLeave)
            }

            sheetMode = SheetMode.PinchZoom(
                sheetX = sheetPoint.first,
                sheetY = sheetPoint.second,
                scale = _browserStateFlow.value.transform.scale,
                distance = distance,
            )
        } else {
            val drawingPoint = action.points[0]
            val sheetPoint = translateToSheet(drawingPoint, action.width, action.height)

            when {
                lastDownBrowserKey == BrowserKey.Space ->
                    if (sheetMode !is SheetMode.Move && sheetPoint != null) {
                        if (sheetMode is SheetMode.Sheet || sheetMode is SheetMode.SheetScale) {
                            executeUi(UiAction.SheetLeave)
                        }

                        sheetMode = SheetMode.Move(
                            sheetX = sheetPoint.first,
                            sheetY = sheetPoint.second,
                            scale = _browserStateFlow.value.transform.scale,
                        )
                    }

                else ->
                    performSheetAction(drawingPoint, sheetPoint) {
                        executeUi(UiAction.SheetDown(it.first, it.second))
                    }
            }
        }
    }

    private fun executeDrawingMove(action: BrowserAction.DrawingMove) {
        if (action.points.isEmpty()) {
            return
        }

        lastDrawingPoints = action.points

        if (action.points.size > 1) {
            val sheetMode = this.sheetMode as? SheetMode.PinchZoom ?: return

            val drawingPoint1 = action.points[0]
            val drawingPoint2 = action.points[1]

            val drawingX = (drawingPoint1.first + drawingPoint2.first) / 2
            val drawingY = (drawingPoint1.second + drawingPoint2.second) / 2

            val offsetX = drawingPoint2.first - drawingPoint1.first
            val offsetY = drawingPoint2.second - drawingPoint1.second
            val distance = sqrt((offsetX * offsetX + offsetY * offsetY).toDouble())

            sheetController.computeTransform(
                drawingX = drawingX,
                drawingY = drawingY,
                drawingWidth = action.width,
                drawingHeight = action.height,
                srcSheetX = sheetMode.sheetX,
                srcSheetY = sheetMode.sheetY,
                newScale = sheetMode.scale * maxOf(1.0, distance) / maxOf(1.0, sheetMode.distance),
            )?.let { _browserStateFlow.value = _browserStateFlow.value.copy(transform = it) }
        } else {
            val drawingPoint = action.points[0]
            val sheetMode = this.sheetMode

            if (sheetMode is SheetMode.Move) {
                sheetController.computeTransform(
                    drawingX = drawingPoint.first,
                    drawingY = drawingPoint.second,
                    drawingWidth = action.width,
                    drawingHeight = action.height,
                    srcSheetX = sheetMode.sheetX,
                    srcSheetY = sheetMode.sheetY,
                    newScale = sheetMode.scale,
                )?.let { _browserStateFlow.value = _browserStateFlow.value.copy(transform = it) }
            } else {
                performSheetAction(
                    drawingPoint,
                    translateToSheet(drawingPoint, action.width, action.height),
                ) {
                    executeUi(UiAction.SheetMove(it.first, it.second))
                }
            }
        }
    }

    private fun executeDrawingUp(action: BrowserAction.DrawingUp) {
        val points = action.points.ifEmpty { lastDrawingPoints }
        lastDrawingPoints = emptyList()

        if (points.isEmpty()) {
            if (sheetMode !is SheetMode.None) {
                sheetMode = SheetMode.None
                executeUi(UiAction.SheetLeave)
            }

            return
        }

        if (points.size > 1) {
            if (sheetMode is SheetMode.PinchZoom) {
                val drawingPoint1 = points[0]
                val drawingPoint2 = points[1]

                val drawingX = (drawingPoint1.first + drawingPoint2.first) / 2
                val drawingY = (drawingPoint1.second + drawingPoint2.second) / 2

                val sheetPoint = translateToSheet(drawingX, drawingY, action.width, action.height)

                sheetMode = if (sheetPoint == null) {
                    SheetMode.None
                } else {
                    executeUi(UiAction.SheetEnter(sheetPoint.first, sheetPoint.second))
                    SheetMode.Sheet
                }
            }
        } else {
            val drawingPoint = points[0]
            val sheetPoint = translateToSheet(drawingPoint, action.width, action.height)

            if (sheetMode is SheetMode.Move) {
                sheetMode = if (sheetPoint == null) {
                    SheetMode.None
                } else {
                    executeUi(UiAction.SheetEnter(sheetPoint.first, sheetPoint.second))
                    SheetMode.Sheet
                }
            } else {
                performSheetAction(drawingPoint, sheetPoint) {
                    executeUi(UiAction.SheetUp(it.first, it.second))
                }
            }
        }
    }

    private fun executeDrawingWheel(action: BrowserAction.DrawingWheel) {
        if (action.deltaY > -1.0 && action.deltaY < 1.0 || sheetMode is SheetMode.Move) {
            return
        }

        val sheetMode = (sheetMode as? SheetMode.SheetScale)
            ?: run {
                val point = translateToSheet(action.x, action.y, action.width, action.height)
                    ?: return

                SheetMode.SheetScale(
                    drawingX = action.x,
                    drawingY = action.y,
                    sheetX = point.first,
                    sheetY = point.second,
                    scale = _browserStateFlow.value.transform.scale,
                ).also { this.sheetMode = it }
            }

        sheetController.computeTransform(
            drawingX = action.x,
            drawingY = action.y,
            drawingWidth = action.width,
            drawingHeight = action.height,
            srcSheetX = sheetMode.sheetX,
            srcSheetY = sheetMode.sheetY,
            newScale = sheetMode.scale + action.deltaY * -0.01,
        )?.let {
            sheetMode.scale = it.scale
            _browserStateFlow.value = _browserStateFlow.value.copy(transform = it)
        }
    }

    private fun executeDrawingLeave() {
        sheetMode = SheetMode.None
        executeUi(UiAction.SheetLeave)
    }

    private fun executeKeyDown(action: BrowserAction.KeyDown) {
        lastDownBrowserKey = action.browserKey

        if (_browserStateFlow.value.dialog == null) {
            BROWSER_HOTKEYS[action.browserKey]?.let(::executeUi)
        }
    }

    private fun executeKeyUp(action: BrowserAction.KeyUp) {
        val lastDownBrowserKey = this.lastDownBrowserKey
        this.lastDownBrowserKey = null

        if (lastDownBrowserKey != action.browserKey || action.browserKey.keyModifiers != 0) {
            return
        }

        val dialog = _browserStateFlow.value.dialog

        when {
            dialog != null -> when (action.browserKey.keyCode) {
                KeyCode.Escape -> executeDialogHide()
                KeyCode.Enter -> executeDialogOk()
            }

            action.browserKey.keyCode == KeyCode.Escape -> executeUi(UiAction.CloseActivePanel)
        }
    }

    private fun executeDialogHide() {
        _browserStateFlow.value = _browserStateFlow.value.copy(dialog = null)
    }

    private fun executeDialogOk() {
        val dialog = _browserStateFlow.value.dialog

        when {
            dialog is BrowserDialog.Confirm && dialog.tag === CONFIRM_TAG_NEW ->
                performPaintingNewConfirmed()

            dialog is BrowserDialog.Prompt && dialog.tag is PromptTag ->
                performPaintingSaveOrExportConfirmed(dialog.tag, lastDialogPromptInput)
        }
    }

    private fun executeDialogPromptInput(action: BrowserAction.DialogPromptInput) {
        lastDialogPromptInput = action.value
    }

    private fun executePaintingNew() {
        _browserStateFlow.value = _browserStateFlow.value.copy(
            dialog = BrowserDialog.Confirm(CONFIRM_TAG_NEW, TextRes.ConfirmNew),
        )
    }

    private fun performPaintingNewConfirmed() {
        uiEngine.clear()
        uiEngine.execute(UiAction.MenuClick)
        fileName = DEFAULT_FILE_NAME

        _browserStateFlow.value = _browserStateFlow.value.copy(
            uiState = uiEngine.state,
            dialog = null,
        )
    }

    private fun executePaintingLoad(action: BrowserAction.PaintingLoad) {
        logger.note("BrowserEngine.executeLoad:begin")
        val webFileList = action.inputElement.files

        if (webFileList == null) {
            logger.note("BrowserEngine.executeLoad:error (file list is null)")
            return
        }

        if (webFileList.length == 0) {
            logger.note("BrowserEngine.executeLoad:error (file list is empty)")
            return
        }

        val webFile = webFileList.item(0)

        if (webFile == null) {
            logger.note("BrowserEngine.executeLoad:error (file is null)")
            return
        }

        val reader = FileReader()

        reader.addEventListener("loadend", {
            action.inputElement.value = ""

            if (reader.error != null) {
                logger.general("BrowserEngine.executeLoad:error (error)") {
                    put("error", reader.error.toString())
                }

                _browserStateFlow.value = _browserStateFlow.value.copy(dialog = BrowserDialog.Alert(TextRes.AlertLoadReaderError))
                return@addEventListener
            }

            val bagData = reader.result as? String

            if (bagData == null) {
                logger.general("BrowserEngine.executeLoad:error (result is null)")
                _browserStateFlow.value = _browserStateFlow.value.copy(dialog = BrowserDialog.Alert(TextRes.AlertLoadNullResult))
                return@addEventListener
            }

            logger.trace("BrowserEngine.executeLoad:unpacking")

            try {
                UnpackableStringBag(bagData).getStuff(uiEngine.selfUnpacker())
            } catch (e: BagUnpackException) {
                logger.general("BrowserEngine.executeLoad:error (unpack)") {
                    put("exception", e.toString())
                }

                _browserStateFlow.value = _browserStateFlow.value.copy(dialog = BrowserDialog.Alert(TextRes.AlertLoadUnpackError))
                return@addEventListener
            }

            uiEngine.execute(UiAction.MenuClick)
            _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
            fileName = webFile.name.replace(Regex(Regex.escape(FILE_EXT_BPE) + "\$"), "")

            logger.note("BrowserEngine.executeLoad:end")
        })

        logger.trace("BrowserEngine.executeLoad:reading") {
            put("name", webFile.name)
        }

        reader.readAsText(webFile)
        logger.trace("BrowserEngine.executeLoad:waiting")
    }

    private fun executePaintingSave() {
        performShowSaveOrExportPrompt(PromptTag.Save)
    }

    private fun executePaintingExportTap() {
        performShowSaveOrExportPrompt(PromptTag.ExportTap)
    }

    private fun executePaintingExportScr() {
        performShowSaveOrExportPrompt(PromptTag.ExportScr)
    }

    private fun executePaintingExportPng() {
        performShowSaveOrExportPrompt(PromptTag.ExportPng)
    }

    private fun performShowSaveOrExportPrompt(tag: PromptTag) {
        _browserStateFlow.value = _browserStateFlow.value.copy(
            dialog = BrowserDialog.Prompt(
                tag = tag,
                message = TextDescriptor(
                    TextRes.PromptSaveMessage,
                    buildMap { put("ext", getFileExtensionByPromptTag(tag)) },
                ),
                value = fileName,
            ),
        )
    }

    private fun performPaintingSaveOrExportConfirmed(tag: PromptTag, promptValue: String) {
        val newFileName = promptValue.trim()

        if (!FILE_NAME_REGEX.matches(newFileName)) {
            _browserStateFlow.value = _browserStateFlow.value.copy(
                dialog = BrowserDialog.Prompt(
                    tag = tag,
                    message = TextDescriptor(
                        TextRes.PromptSaveMessage,
                        buildMap { put("ext", getFileExtensionByPromptTag(tag)) },
                    ),
                    hint = TextDescriptor(TextRes.PromptSaveHint, buildMap {
                        put("len", FILE_NAME_MAX_LENGTH.toString())
                        put("specials", FILE_NAME_SPECIALS)
                    }),
                    value = newFileName,
                ),
            )

            return
        }

        logger.note("BrowserEngine.performPaintingSaveOrExportConfirmed:begin")
        fileName = newFileName

        when (tag) {
            PromptTag.Save -> {
                val bagData = PackableStringBag()
                    .also { it.put(uiEngine.selfPacker(), uiEngine) }
                    .toString()

                performPaintingSaveOrExportBlob(
                    newFileName + FILE_EXT_BPE,
                    Blob(arrayOf(bagData), BlobPropertyBag("application/octet-stream")),
                )
            }

            PromptTag.ExportTap -> performPaintingSaveOrExportBlob(
                newFileName + FILE_EXT_TAP,
                Blob(arrayOf(Uint8Array(uiEngine.exportToTap(newFileName).toTypedArray())), BlobPropertyBag("application/octet-stream")),
            )

            PromptTag.ExportScr -> performPaintingSaveOrExportBlob(
                newFileName + FILE_EXT_SCR,
                Blob(arrayOf(Uint8Array(uiEngine.exportToScr().toTypedArray())), BlobPropertyBag("application/octet-stream")),
            )

            PromptTag.ExportPng -> drawingSheet?.toBlob({ blob: Blob? ->
                if (blob != null) {
                    performPaintingSaveOrExportBlob(
                        newFileName + FILE_EXT_PNG,
                        blob.slice(0, blob.size.toInt(), "application/octet-stream"),
                    )
                }
            })
        }

        uiEngine.execute(UiAction.MenuClick)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state, dialog = null)

        logger.note("BrowserEngine.performPaintingSaveOrExportConfirmed:end")
    }

    private fun performPaintingSaveOrExportBlob(fullFileName: String, blob: Blob) {
        (document.createElement("a") as HTMLAnchorElement).also {
            it.download = fullFileName
            it.href = URL.createObjectURL(blob)
            it.click()
        }
    }

    private fun tryLoadFromStorage() {
        try {
            val bagData = window.localStorage.getItem(KEY_STATE)

            if (bagData != null) {
                UnpackableStringBag(bagData).getStuff(Unpacker())
                _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
            }
        } catch (t: Throwable) {
            logger.note(t.toString())
        }
    }

    private fun trySaveToStorage() {
        var historyStepsLimit = uiEngine.state.historySteps

        while (true) {
            val bagData = PackableStringBag()
                .also { it.put(Packer(historyStepsLimit), this) }
                .toString()

            try {
                window.localStorage.setItem(KEY_STATE, bagData)
                break
            } catch (t: Throwable) {
                logger.trace("BrowserEngine.trySaveToStorage:setItem failed") {
                    put("error", t.toString())
                }
            }

            if (historyStepsLimit == 0) {
                try {
                    window.localStorage.removeItem(KEY_STATE)
                } catch (t: Throwable) {
                    logger.trace("BrowserEngine.trySaveToStorage:removeItem failed") {
                        put("error", t.toString())
                    }
                }

                break
            }

            historyStepsLimit /= 2
        }

        logger.note("BrowserEngine.trySaveToStorage:saved") {
            put("originalHistoryStepsLimit", uiEngine.state.historySteps.toString())
            put("actualHistoryStepsLimit", historyStepsLimit.toString())
        }
    }

    private fun getFileExtensionByPromptTag(tag: PromptTag) = when (tag) {
        PromptTag.Save -> FILE_EXT_BPE
        PromptTag.ExportTap -> FILE_EXT_TAP
        PromptTag.ExportScr -> FILE_EXT_SCR
        PromptTag.ExportPng -> FILE_EXT_PNG
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun translateToSheet(drawingPoint: Pair<Int, Int>, drawingWidth: Int, drawingHeight: Int): Pair<Int, Int>? =
        sheetController.translateToSheet(drawingPoint.first, drawingPoint.second, drawingWidth, drawingHeight, _browserStateFlow.value.transform)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun translateToSheet(x: Int, y: Int, drawingWidth: Int, drawingHeight: Int): Pair<Int, Int>? =
        sheetController.translateToSheet(x, y, drawingWidth, drawingHeight, _browserStateFlow.value.transform)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun performSheetAction(drawingPoint: Pair<Int, Int>, sheetPoint: Pair<Int, Int>?, noinline block: (point: Pair<Int, Int>) -> Unit) =
        performSheetAction(drawingPoint.first, drawingPoint.second, sheetPoint, block)

    private fun performSheetAction(drawingX: Int, drawingY: Int, sheetPoint: Pair<Int, Int>?, block: (point: Pair<Int, Int>) -> Unit) {
        val sheetMode = this.sheetMode

        when {
            sheetPoint != null -> {
                when (sheetMode) {
                    is SheetMode.SheetScale ->
                        if ((abs(drawingX - sheetMode.drawingX) > SCALE_POINT_THRESHOLD ||
                                    abs(drawingY - sheetMode.drawingY) > SCALE_POINT_THRESHOLD)
                        ) {
                            this.sheetMode = SheetMode.Sheet
                        }

                    !is SheetMode.Sheet -> {
                        this.sheetMode = SheetMode.Sheet
                        uiEngine.execute(UiAction.SheetEnter(sheetPoint.first, sheetPoint.second))
                    }

                    else -> Unit
                }

                block(sheetPoint)
            }

            sheetMode !is SheetMode.None -> {
                this.sheetMode = SheetMode.None
                executeUi(UiAction.SheetLeave)
            }
        }
    }

    private companion object {
        private const val KEY_STATE = "state"
        private val AUTOSAVE_TIMEOUT = 1.seconds
        private const val SCALE_POINT_THRESHOLD = 2

        private const val DEFAULT_FILE_NAME = "painting"
        private const val FILE_EXT_BPE = ".bpe"
        private const val FILE_EXT_TAP = ".tap"
        private const val FILE_EXT_SCR = ".scr"
        private const val FILE_EXT_PNG = ".png"

        private const val FILE_NAME_MAX_LENGTH = 8
        private const val FILE_NAME_SPECIALS = "._-=!?()[]{}"
        private val FILE_NAME_REGEX = Regex("^[0-9A-Za-z._\\-=!?()\\[\\]{}]{1,8}$")

        private val CONFIRM_TAG_NEW = object {}
    }

    private enum class PromptTag {
        Save,
        ExportTap,
        ExportScr,
        ExportPng,
    }

    private class Packer(private val historyStepsLimit: Int) : BagStuffPacker<BrowserEngine> {
        override val putInTheBagVersion = 4

        override fun putInTheBag(bag: PackableBag, value: BrowserEngine) {
            bag.put(value.fileName)

            // Put UiEngine at the end, to be able to recover from unpack error in Unpacker
            bag.put(value.uiEngine.selfPacker(historyStepsLimit), value.uiEngine)
        }
    }

    private inner class Unpacker : BagStuffUnpacker<BrowserEngine> {
        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BrowserEngine {
            requireSupportedStuffVersion("BrowserEngine", 4, version)

            if (version < 4) {
                uiEngine.selfUnpacker().getOutOfTheBag(version, bag)
                fileName = DEFAULT_FILE_NAME
            } else {
                val fileName = bag.getString()
                bag.getStuff(uiEngine.selfUnpacker())
                this@BrowserEngine.fileName = fileName
            }

            return this@BrowserEngine
        }
    }
}

sealed interface SheetMode {
    data object None : SheetMode
    data object Sheet : SheetMode
    data class SheetScale(val drawingX: Int, val drawingY: Int, val sheetX: Int, val sheetY: Int, var scale: Double) : SheetMode
    data class Move(val sheetX: Int, val sheetY: Int, val scale: Double) : SheetMode
    data class PinchZoom(val sheetX: Int, val sheetY: Int, val scale: Double, val distance: Double) : SheetMode
}
