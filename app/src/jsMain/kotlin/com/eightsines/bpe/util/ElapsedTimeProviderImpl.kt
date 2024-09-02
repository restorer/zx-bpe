package com.eightsines.bpe.util

import kotlin.js.Date

class ElapsedTimeProviderImpl : ElapsedTimeProvider {
    override fun getElapsedTimeMs() = Date().getMilliseconds()
}
