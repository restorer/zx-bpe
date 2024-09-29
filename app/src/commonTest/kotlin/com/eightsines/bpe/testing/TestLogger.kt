package com.eightsines.bpe.testing

import com.eightsines.bpe.util.Logger

class TestLogger : Logger {
    override fun trace(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) = Unit
    override fun log(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) = Unit
    override fun critical(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) = Unit
}
