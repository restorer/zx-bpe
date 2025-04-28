package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestStuff
import com.eightsines.bpe.testing.TestStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnpackableStringBagV2Test {
    // @Test
    // fun makeExample() {
    //     val exampleData = PackableStringBagV2().also {
    //         it.put(null as Boolean?)
    //         it.put(false)
    //         it.put(true)
    //         it.put(null as Int?)
    //         it.put(4)
    //         it.put(64)
    //         it.put(16384)
    //         it.put(1073741824)
    //         it.put(TestStuff, null)
    //         it.put(TestStuff.VersionedPackerUnpacker(4), TestStuffMother.TestStuff)
    //         it.put(TestStuff.VersionedPackerUnpacker(64), TestStuffMother.TestStuff)
    //         it.put(TestStuff.VersionedPackerUnpacker(16384), TestStuffMother.TestStuff)
    //         it.put(TestStuff.VersionedPackerUnpacker(1073741824), TestStuffMother.TestStuff)
    //         it.put(null as String?)
    //         it.put("Test")
    //         it.put("ThisIsTheTestAndYetAnotherThisIsTheTestAndYetAnother")
    //     }.toString()
    //
    //     println("---\n$exampleData\n---")
    //     throw RuntimeException("Example")
    // }

    @Test
    fun shouldUnpackExample() = performTest(
        arrange = {
            UnpackableStringBagV2(
                "BAG2AkYGgoAUAAAZAAAAAAqEJUCqm6OrMzAFAISoFVN0dWZmAdAACEqBVTdHVmZgFQAAAACEqBVTdHVmZgIKKjK5ugANFRoaXNJc1RoZVRlc3RBbmRZZXRBbm90aGVyVGhpc0lzVGhlVGVzdEFuZFlldEFub3RoZXIA=",
            )
        },
        act = {
            listOf(
                it.getBooleanOrNull(),
                it.getBoolean(),
                it.getBoolean(),
                it.getIntOrNull(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getInt(),
                it.getStuffOrNull(TestStuff),
                it.getStuff(TestStuff.VersionedPackerUnpacker(4)),
                it.getStuff(TestStuff.VersionedPackerUnpacker(64)),
                it.getStuff(TestStuff.VersionedPackerUnpacker(16384)),
                it.getStuff(TestStuff.VersionedPackerUnpacker(1073741824)),
                it.getStringOrNull(),
                it.getString(),
                it.getString(),
            )
        },
        assert = {
            assertEquals(
                listOf(
                    null,
                    false,
                    true,
                    null,
                    4,
                    64,
                    16384,
                    1073741824,
                    null,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    TestStuffMother.TestStuff,
                    null,
                    "Test",
                    "ThisIsTheTestAndYetAnotherThisIsTheTestAndYetAnother",
                ),
                it,
            )
        },
    )
}
