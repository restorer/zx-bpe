package com.eightsines.bpe.exporters

import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TapExporterTest {
    @Test
    fun shouldCreateTapProgram() = performTest(
        arrange = { BasicProgram() to TapFile() },
        act = { (program, tap) ->
            // 10 REM Made with BPE
            // 20 INK 2: PAPER 3: FLASH 0: BRIGHT 1: INVERSE 0: OVER 0: BORDER 4: CLS
            // 30 PRINT "FOO"
            // 40 PRINT #0; AT 1,2; "BAR"
            // 50 PAUSE 1: GO TO 50

            BasicLine(10).apply {
                append(BasicLine.TOKEN_REM)
                append("Made with BPE")
                program.appendLine(this)
            }

            BasicLine(20).apply {
                append(BasicLine.TOKEN_INK)
                appendNumber(2)
                append(':')

                append(BasicLine.TOKEN_PAPER)
                appendNumber(3)
                append(':')

                append(BasicLine.TOKEN_FLASH)
                appendNumber(0)
                append(':')

                append(BasicLine.TOKEN_BRIGHT)
                appendNumber(1)
                append(':')

                append(BasicLine.TOKEN_INVERSE)
                appendNumber(0)
                append(':')

                append(BasicLine.TOKEN_OVER)
                appendNumber(0)
                append(':')

                append(BasicLine.TOKEN_BORDER)
                appendNumber(4)
                append(':')

                append(BasicLine.TOKEN_CLS)
                program.appendLine(this)
            }

            BasicLine(30).apply {
                append(BasicLine.TOKEN_PRINT)
                append("\"FOO\"")
                program.appendLine(this)
            }

            BasicLine(40).apply {
                append(BasicLine.TOKEN_PRINT)
                append('#')
                appendNumber(0)
                append("; ")
                append(BasicLine.TOKEN_AT)
                appendNumber(1)
                append(',')
                appendNumber(2)
                append("; \"BAR\"")
                program.appendLine(this)
            }

            BasicLine(50).apply {
                append(BasicLine.TOKEN_PAUSE)
                appendNumber(1)
                append(':')
                append(BasicLine.TOKEN_GO_TO)
                appendNumber(50)
                program.appendLine(this)
            }

            tap.appendProgram("TEST", program.buffer, 10)
            tap.buffer
        },
        assert = {
            assertEquals(
                listOf(
                    0x13, 0x00, 0x00, 0x00, 0x54, 0x45, 0x53, 0x54, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0xa1, 0x00,
                    0x0a, 0x00, 0xa1, 0x00, 0x1c, 0xa3, 0x00, 0xff, 0x00, 0x0a, 0x0f, 0x00, 0xea, 0x4d, 0x61, 0x64,
                    0x65, 0x20, 0x77, 0x69, 0x74, 0x68, 0x20, 0x42, 0x50, 0x45, 0x0d, 0x00, 0x14, 0x41, 0x00, 0xd9,
                    0x32, 0x0e, 0x00, 0x00, 0x02, 0x00, 0x00, 0x3a, 0xda, 0x33, 0x0e, 0x00, 0x00, 0x03, 0x00, 0x00,
                    0x3a, 0xdb, 0x30, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3a, 0xdc, 0x31, 0x0e, 0x00, 0x00, 0x01,
                    0x00, 0x00, 0x3a, 0xdd, 0x30, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3a, 0xde, 0x30, 0x0e, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x3a, 0xe7, 0x34, 0x0e, 0x00, 0x00, 0x04, 0x00, 0x00, 0x3a, 0xfb, 0x0d,
                    0x00, 0x1e, 0x07, 0x00, 0xf5, 0x22, 0x46, 0x4f, 0x4f, 0x22, 0x0d, 0x00, 0x28, 0x23, 0x00, 0xf5,
                    0x23, 0x30, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3b, 0x20, 0xac, 0x31, 0x0e, 0x00, 0x00, 0x01,
                    0x00, 0x00, 0x2c, 0x32, 0x0e, 0x00, 0x00, 0x02, 0x00, 0x00, 0x3b, 0x20, 0x22, 0x42, 0x41, 0x52,
                    0x22, 0x0d, 0x00, 0x32, 0x13, 0x00, 0xf2, 0x31, 0x0e, 0x00, 0x00, 0x01, 0x00, 0x00, 0x3a, 0xec,
                    0x35, 0x30, 0x0e, 0x00, 0x00, 0x32, 0x00, 0x00, 0x0d, 0xb5,
                ).map { it.toByte() },
                it,
            )
        },
    )
}
