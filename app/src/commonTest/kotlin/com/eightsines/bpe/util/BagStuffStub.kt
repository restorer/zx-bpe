package com.eightsines.bpe.util

data class BagStuffStub(
    val booleanValue: Boolean,
    val intValue: Int,
    val stringValue: String,
) {
    companion object : BagStuffPacker<BagStuffStub>, BagStuffUnpacker<BagStuffStub> {
        override val putInTheBagVersion = 1

        override fun putInTheBag(bag: PackableBag, value: BagStuffStub) {
            bag.put(value.booleanValue)
            bag.put(value.intValue)
            bag.put(value.stringValue)
        }

        override fun getOutOfTheBag(version: Int, bag: UnpackableBag): BagStuffStub {
            if (version != 1) {
                throw UnsupportedVersionBagUnpackException("BagStuffStub", version)
            }

            return BagStuffStub(
                booleanValue = bag.getBoolean(),
                intValue = bag.getInt(),
                stringValue = bag.getString(),
            )
        }
    }
}
