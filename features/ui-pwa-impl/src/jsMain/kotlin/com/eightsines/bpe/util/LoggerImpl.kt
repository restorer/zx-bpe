package com.eightsines.bpe.util

class LoggerImpl : Logger {
    override fun log(severity: Severity, message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) {
        val sb = StringBuilder()

        sb.append(
            when (severity) {
                Severity.Trace -> "..."
                Severity.Note -> "---"
                Severity.General -> "==="
                Severity.Critical -> "!!!"
            }
        )

        if (message.isNotEmpty()) {
            sb.append(' ')
            sb.append(message)
        }

        if (argumentsBuilder != null) {
            val arguments = LinkedHashMap<String, String>().also { argumentsBuilder(it) }
            sb.append(if (message.isEmpty()) " " else "\n")
            sb.append(arguments.entries.joinToString(separator = "\n") { "${it.key} = ${it.value}" })
        }

        when (severity) {
            Severity.Trace -> console.asDynamic().debug(sb.toString())
            Severity.Note -> console.log(sb.toString())
            Severity.General -> console.info(sb.toString())
            Severity.Critical -> console.warn(sb.toString())
        }
    }
}
