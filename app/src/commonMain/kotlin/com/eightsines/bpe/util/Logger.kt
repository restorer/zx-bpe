package com.eightsines.bpe.util

interface Logger {
    fun log(severity: Severity, message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null)

    fun trace(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null) =
        log(Severity.Trace, message, argumentsBuilder)

    fun note(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null) =
        log(Severity.Note, message, argumentsBuilder)

    fun general(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null) =
        log(Severity.General, message, argumentsBuilder)

    fun critical(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)? = null) =
        log(Severity.Critical, message, argumentsBuilder)
}

enum class Severity {
    Trace,
    Note,
    General,
    Critical,
}
