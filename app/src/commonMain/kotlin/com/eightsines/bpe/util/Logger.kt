package com.eightsines.bpe.util

interface Logger {
    fun trace(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null)
    fun log(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null)
    fun critical(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null)
}
