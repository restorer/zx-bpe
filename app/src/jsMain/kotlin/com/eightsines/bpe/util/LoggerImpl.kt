package com.eightsines.bpe.util

class LoggerImpl : Logger {
    override fun trace(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) {
        console.asDynamic().debug(buildMessage(SEVERITY_TRACE, message, argumentsBuilder))
    }

    override fun log(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) {
        console.info(buildMessage(SEVERITY_LOG, message, argumentsBuilder))
    }

    override fun critical(message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?) {
        console.warn(buildMessage(SEVERITY_CRITICAL, message, argumentsBuilder))
    }

    private fun buildMessage(severity: String, message: String, argumentsBuilder: (MutableMap<String, String>.() -> Unit)?): String {
        val sb = StringBuilder(severity)

        if (message.isNotEmpty()) {
            sb.append(' ')
            sb.append(message)
        }

        if (argumentsBuilder != null) {
            val arguments = LinkedHashMap<String, String>().also { argumentsBuilder(it) }
            sb.append(if (message.isEmpty()) " " else "\n")
            sb.append(arguments.entries.joinToString(separator = "\n") { "${it.key} = ${it.value}" })
        }

        return sb.toString()
    }

    private companion object {
        private const val SEVERITY_TRACE = "---"
        private const val SEVERITY_LOG = "==="
        private const val SEVERITY_CRITICAL = "!!!"
    }
}
