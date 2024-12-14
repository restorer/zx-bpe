package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.resources.TextRes
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.Window
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader

class BrowserEngine(
    private val logger: Logger,
    private val window: Window,
    private val document: Document,
    private val uiEngine: UiEngine,
) {
    private val _browserStateFlow = MutableStateFlow(BrowserState(uiState = uiEngine.state))

    val browserStateFlow: Flow<BrowserState>
        get() = _browserStateFlow

    init {
        document.addEventListener("visibilitychange", {
            if (document.asDynamic().visibilityState == "hidden") {
                val bagData = PackableStringBag()
                    .also { uiEngine.putInTheBagSelf(it) }
                    .toString()

                try {
                    window.localStorage.setItem(KEY_STATE, bagData)
                } catch (t: Throwable) {
                    logger.general(t.toString())
                }
            }
        })

        try {
            val bagData = window.localStorage.getItem(KEY_STATE)

            if (bagData != null) {
                uiEngine.getOutOfTheBagSelf(UnpackableStringBag(bagData))
                _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
            }
        } catch (t: Throwable) {
            logger.note(t.toString())
        }
    }

    fun execute(action: BrowserAction) = when (action) {
        is BrowserAction.Ui -> executeUi(action)
        is BrowserAction.AlertHide -> executeAlertHide()
        is BrowserAction.PaintingNew -> executePaintingNew()
        is BrowserAction.PaintingLoad -> executePaintingLoad(action)
        is BrowserAction.PaintingSave -> executePaintingSave()
        is BrowserAction.PaintingExport -> executePaintingExport()
    }

    private fun executeUi(action: BrowserAction.Ui) {
        uiEngine.execute(action.action)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
    }

    private fun executeAlertHide() {
        _browserStateFlow.value = _browserStateFlow.value.copy(alertText = null)
    }

    private fun executePaintingNew() {
        // TODO: confirm

        uiEngine.clearSelf()
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)
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
            if (reader.error != null) {
                logger.general("BrowserEngine.executeLoad:error (error)") {
                    put("error", reader.error.toString())
                }

                _browserStateFlow.value = _browserStateFlow.value.copy(alertText = TextRes.AlertLoadReaderError)
            } else {
                val bagData = reader.result as? String

                if (bagData == null) {
                    logger.general("BrowserEngine.executeLoad:error (result is null)")
                    _browserStateFlow.value = _browserStateFlow.value.copy(alertText = TextRes.AlertLoadNullResult)
                } else {
                    logger.trace("BrowserEngine.executeLoad:unpacking")

                    try {
                        uiEngine.getOutOfTheBagSelf(UnpackableStringBag(bagData))
                        uiEngine.execute(UiAction.MenuClick)
                        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)

                        logger.note("BrowserEngine.executeLoad:end")
                    } catch (e: BagUnpackException) {
                        logger.general("BrowserEngine.executeLoad:error (unpack)") {
                            put("exception", e.toString())
                        }

                        _browserStateFlow.value = _browserStateFlow.value.copy(alertText = TextRes.AlertLoadUnpackError)
                    }
                }
            }

            action.inputElement.value = ""
        })

        logger.trace("BrowserEngine.executeLoad:reading") {
            put("name", webFile.name)
        }

        reader.readAsText(webFile)
        logger.trace("BrowserEngine.executeLoad:waiting")
    }

    private fun executePaintingSave() {
        logger.note("BrowserEngine.executeSave:begin")

        val bagData = PackableStringBag()
            .also { uiEngine.putInTheBagSelf(it) }
            .toString()

        logger.trace("BrowserEngine.executeSave:packed")

        (document.createElement("a") as HTMLAnchorElement).apply {
            download = "painting.bpe"
            href = URL.createObjectURL(Blob(arrayOf(bagData), BlobPropertyBag("application/octet-stream")))
            click()
        }

        uiEngine.execute(UiAction.MenuClick)
        _browserStateFlow.value = _browserStateFlow.value.copy(uiState = uiEngine.state)

        logger.note("BrowserEngine.executeSave:end")
    }

    private fun executePaintingExport() {
        _browserStateFlow.value = _browserStateFlow.value.copy(alertText = TextRes.AlertExportNotImplemented)
    }

    private companion object {
        private const val KEY_STATE = "state"
    }
}
