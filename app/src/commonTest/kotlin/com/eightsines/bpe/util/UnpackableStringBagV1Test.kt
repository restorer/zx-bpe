package com.eightsines.bpe.util

import com.eightsines.bpe.testing.TestStuff
import com.eightsines.bpe.testing.TestStuffMother
import com.eightsines.bpe.testing.performTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnpackableStringBagV1Test {
    @Test
    fun shouldUnpackExample() = performTest(
        arrange = {
            UnpackableStringBagV1(
                "BAG1_bB_i4I40n4000N40000000_u4bI2As5StuffU40bI2As5Stufff4000bI2As5StuffF40000000bI2As5Stuff_s4TestS34ThisIsTheTestAndYetAnotherThisIsTheTestAndYetAnother",
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
