package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiAction
import com.eightsines.bpe.presentation.UiEngine
import com.eightsines.bpe.presentation.UiState
import com.eightsines.bpe.util.BagUnpackException
import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.PackableStringBag
import com.eightsines.bpe.util.UnpackableStringBag
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
    var onUpdate: ((UiState) -> Unit)? = null

    fun ready() = onUpdate?.invoke(uiEngine.state)

    fun execute(action: BrowserAction) = when (action) {
        is BrowserAction.Ui -> executeUi(action)
        is BrowserAction.Load -> executeLoad(action)
        is BrowserAction.Save -> executeSave()
    }

    private fun executeUi(action: BrowserAction.Ui) {
        uiEngine.execute(action.action)
        onUpdate?.invoke(uiEngine.state)
    }

    private fun executeLoad(action: BrowserAction.Load) {
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

                window.alert("Failed to read painting (reader error)")
            } else {
                val bagData = reader.result as? String

                if (bagData == null) {
                    logger.general("BrowserEngine.executeLoad:error (result is null)")
                    window.alert("Failed to read painting (result is null)")
                } else {
                    logger.trace("BrowserEngine.executeLoad:unpacking")

                    try {
                        uiEngine.getOutOfTheBagSelf(UnpackableStringBag(bagData))
                        uiEngine.execute(UiAction.MenuClick)
                        onUpdate?.invoke(uiEngine.state)

                        logger.note("BrowserEngine.executeLoad:end")
                    } catch (e: BagUnpackException) {
                        logger.general("BrowserEngine.executeLoad:error (unpack)") {
                            put("exception", e.toString())
                        }

                        window.alert("Failed to read painting (unpack)")
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

    private fun executeSave() {
        logger.note("BrowserEngine.executeSave:begin")

        val bagData = PackableStringBag()
            .also { uiEngine.putInTheBagSelf(it) }
            .toString()

        logger.trace("BrowserEngine.executeSave:packed")

        (document.createElement("a") as HTMLAnchorElement).apply {
            download = "painting.bpe"
            href = URL.createObjectURL(Blob(arrayOf(bagData), BlobPropertyBag("text/plain")))
            click()
        }

        uiEngine.execute(UiAction.MenuClick)
        onUpdate?.invoke(uiEngine.state)

        logger.note("BrowserEngine.executeSave:end")
    }
}
