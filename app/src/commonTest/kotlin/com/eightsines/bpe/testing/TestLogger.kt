package com.eightsines.bpe.testing

import com.eightsines.bpe.util.Logger
import com.eightsines.bpe.util.Severity

class TestLogger : Logger {
    override fun log(severity: Severity, message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) = Unit
}
