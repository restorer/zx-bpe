package com.eightsines.bpe.view

import com.eightsines.bpe.util.Logger
import org.w3c.dom.Document

class UnhandledErrorView(document: Document, private val logger: Logger) {
    private val error = document.querySelector(".js-error")
    private val errorHeader = document.querySelector(".js-error-header")
    private val errorContent = document.querySelector(".js-error-content")

    init {
        errorHeader?.addEventListener(EVENT_CLICK, {
            error?.classList?.add(CLASS_HIDDEN)
        })
    }

    fun show(throwable: Throwable) {
        val errorText = throwable.stackTraceToString()

        logger.critical(errorText)
        errorContent?.textContent = errorText
        error?.classList?.remove(CLASS_HIDDEN)
    }

    private companion object {
        private const val CLASS_HIDDEN = "hidden"
        private const val EVENT_CLICK = "click"
    }
}
