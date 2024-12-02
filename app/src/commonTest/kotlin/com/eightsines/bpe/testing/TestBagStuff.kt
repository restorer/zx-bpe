package com.eightsines.bpe.testing

import com.eightsines.bpe.util.BagStuffPacker
import com.eightsines.bpe.util.BagStuffUnpacker
import com.eightsines.bpe.util.PackableBag
import com.eightsines.bpe.util.UnpackableBag
import com.eightsines.bpe.util.requireSupportedStuffVersion

data class TestBagStuff(
    val booleanValue: Boolean,
    val intValue: Int,
    val stringValue: String,
) {
    companion object : BagStuffPacker<TestBagStuff>, BagStuffUnpacker<TestBagStuff> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: TestBagStuff) {
            bag.put(value.booleanValue)
            bag.put(value.intValue)
            bag.put(value.stringValue)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): TestBagStuff {
            requireSupportedStuffVersion("BagStuffStub", 1, version)

            return TestBagStuff(
                booleanValue = bag.getBoolean(),
                intValue = bag.getInt(),
                stringValue = bag.getString(),
            )
        }
    }
}

object TestBagStuffMother {
    val TestStuff = TestBagStuff(
        booleanValue = false,
        intValue = 42,
        stringValue = "Stuff",
    )
}
