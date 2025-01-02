package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.resources.TextDescriptor
import com.eightsines.bpe.resources.TextRes
import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.Severity
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnpackableStringBag
import com.eightsines.bpe.util.requireSupportedStuffVersion
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
import org.w3c.dom.Window
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.time.Duration.Companion.seconds

class BrowserEngine(
    private val logger: Logger,
    private val window: Window,
    private val document: Document,
    private val uiEngine: UiEngine,
    mainDispatcher: CoroutineDispatcher,
) {
    private val _browserStateFlow = MutableStateFlow(BrowserState(uiState = uiEngine.state))
    private var isInitiallyLoaded = false
    private var fileName = DEFAULT_FILE_NAME

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
        val severity = if (action is BrowserAction.Ui) Severity.Trace else Severity.Note

        logger.log(severity, "BrowserEngine.execute") {
            put("action", action.toString())
        }

        when (action) {
            is BrowserAction.Ui -> executeUi(action)
            is BrowserAction.DialogHide -> executeDialogHide()
            is BrowserAction.DialogConfirmOk -> executeDialogConfirmOk(action)
            is BrowserAction.DialogPromptOk -> executeDialogPromptOk(action)
            is BrowserAction.PaintingNew -> executePaintingNew()
            is BrowserAction.PaintingLoad -> executePaintingLoad(action)
            is BrowserAction.PaintingSave -> executePaintingSave()
            is BrowserAction.PaintingExportTap -> executePaintingExportTap()
            is BrowserAction.PaintingExportScr -> executePaintingExportScr()
            is BrowserAction.PaintingExportPng -> executePaintingExportPng()
        }
    }

    private fun executeUi(action: BrowserAction.Ui) {
        uiEngine.execute(action.action)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
    }

    private fun executeDialogHide() {
        _browserStateFlow.value = _browserStateFlow.value.copy(dialog = null)
    }

    private fun executeDialogConfirmOk(action: BrowserAction.DialogConfirmOk) {
        if (action.tag === CONFIRM_TAG_NEW) {
            performPaintingNewConfirmed()
        }
    }

    private fun executeDialogPromptOk(action: BrowserAction.DialogPromptOk) {
        if (action.tag === PROMPT_TAG_SAVE ||
            action.tag === PROMPT_TAG_EXPORT_TAP ||
            action.tag === PROMPT_TAG_EXPORT_SCR ||
            action.tag === PROMPT_TAG_EXPORT_PNG
        ) {
            performPaintingSaveOrExportConfirmed(action.tag, action.value)
        }
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
        performShowSaveOrExportPrompt(PROMPT_TAG_SAVE)
    }

    private fun executePaintingExportTap() {
        _browserStateFlow.value = _browserStateFlow.value.copy(dialog = BrowserDialog.Alert(TextRes.AlertExportNotImplemented))
    }

    private fun executePaintingExportScr() {
        performShowSaveOrExportPrompt(PROMPT_TAG_EXPORT_SCR)
    }

    private fun executePaintingExportPng() {
        _browserStateFlow.value = _browserStateFlow.value.copy(dialog = BrowserDialog.Alert(TextRes.AlertExportNotImplemented))
    }

    private fun performShowSaveOrExportPrompt(tag: Any) {
        _browserStateFlow.value = _browserStateFlow.value.copy(
            dialog = BrowserDialog.Prompt(
                tag = tag,
                message = TextRes.PromptSaveMessage,
                value = fileName,
            ),
        )
    }

    private fun performPaintingSaveOrExportConfirmed(tag: Any, promptValue: String) {
        val newFileName = promptValue.trim()

        if (!FILE_NAME_REGEX.matches(newFileName)) {
            _browserStateFlow.value = _browserStateFlow.value.copy(
                dialog = BrowserDialog.Prompt(
                    tag = tag,
                    message = TextRes.PromptSaveMessage,
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

        when (tag) {
            PROMPT_TAG_SAVE -> {
                val bagData = PackableStringBag()
                    .also { it.put(UiEngine.Packer(), uiEngine) }
                    .toString()

                performPaintingSaveOrExportBlob(
                    newFileName + FILE_EXT_BPE,
                    Blob(arrayOf(bagData), BlobPropertyBag("application/octet-stream")),
                )
            }

            PROMPT_TAG_EXPORT_TAP -> Unit

            PROMPT_TAG_EXPORT_SCR -> performPaintingSaveOrExportBlob(
                newFileName + FILE_EXT_SCR,
                Blob(arrayOf(Uint8Array(uiEngine.exportToScr().toTypedArray())), BlobPropertyBag("application/octet-stream")),
            )

            PROMPT_TAG_EXPORT_PNG -> Unit
        }

        uiEngine.execute(UiAction.MenuClick)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state, dialog = null)
        fileName = newFileName

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

    private companion object {
        private const val KEY_STATE = "state"
        private val AUTOSAVE_TIMEOUT = 1.seconds

        private const val DEFAULT_FILE_NAME = "painting"
        private const val FILE_EXT_BPE = ".bpe"
        private const val FILE_EXT_TAP = ".tap"
        private const val FILE_EXT_SCR = ".scr"
        private const val FILE_EXT_PNG = ".png"

        private const val FILE_NAME_MAX_LENGTH = 8
        private const val FILE_NAME_SPECIALS = "._-=!?()[]{}"
        private val FILE_NAME_REGEX = Regex("^[0-9A-Za-z._\\-=!?()\\[\\]{}]{1,8}$")

        private val CONFIRM_TAG_NEW = object {}
        private val PROMPT_TAG_SAVE = object {}
        private val PROMPT_TAG_EXPORT_TAP = object {}
        private val PROMPT_TAG_EXPORT_SCR = object {}
        private val PROMPT_TAG_EXPORT_PNG = object {}
    }

    private class Packer(private val historyStepsLimit: Int) : BagStuffPacker<BrowserEngine> {
        override val putInTheBagVersion = 4

        override fun putInTheBag(bag: PackableBag, value: BrowserEngine) {
            bag.put(value.fileName)

            // Put UiEngine at the end, to be able to recover from unpack error in Unpacker
            bag.put(UiEngine.Packer(historyStepsLimit), value.uiEngine)
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
