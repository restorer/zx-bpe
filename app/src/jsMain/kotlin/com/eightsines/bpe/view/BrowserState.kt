package com.eightsines.bpe.view

import com.eightsines.bpe.presentation.UiState
import com.eightsines.bpe.resources.TextResId

data class BrowserState(val uiState: UiState, val alertText: TextResId? = null)
