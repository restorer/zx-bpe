package com.eightsines.bpe.testing

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.UnsupportedVersionBagUnpackException

data class TestStuff(
    val booleanValue: Boolean,
    val intValue: Int,
    val stringValue: String,
) {
    open class VersionedPackerUnpacker(private val requiredVersion: Int) : BagStuffPacker<TestStuff>, BagStuffUnpacker<TestStuff> {
        override val putInTheBagVersion = requiredVersion

        override fun putInTheBag(bag: PackableBag, value: TestStuff) {
            bag.put(value.booleanValue)
            bag.put(value.intValue)
            bag.put(value.stringValue)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): TestStuff {
            // Differs from requireSupportedStuffVersion() in that it checks for an exact version match
            if (version != requiredVersion) {
                throw UnsupportedVersionBagUnpackException("TestStuff", version)
            }

            return TestStuff(
                booleanValue = bag.getBoolean(),
                intValue = bag.getInt(),
                stringValue = bag.getString(),
            )
        }
    }

    companion object : VersionedPackerUnpacker(1)
}

object TestStuffMother {
    val TestStuff = TestStuff(
        booleanValue = false,
        intValue = 42,
        stringValue = "Stuff",
    )
}
