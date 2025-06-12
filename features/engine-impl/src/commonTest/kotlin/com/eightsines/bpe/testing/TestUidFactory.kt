package com.eightsines.bpe.testing

import com.eightsines.bpe.util.UidFactory

class TestUidFactory : UidFactory {
    private var index = 0

    override fun createUid(): String {
        ++index
        return "testuid-$index"
    }
}
