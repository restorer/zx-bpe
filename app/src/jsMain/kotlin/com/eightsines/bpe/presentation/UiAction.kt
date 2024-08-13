package com.eightsines.bpe.presentation

sealed interface UiAction {
    data object ToolboxMenuClick : UiAction
}
