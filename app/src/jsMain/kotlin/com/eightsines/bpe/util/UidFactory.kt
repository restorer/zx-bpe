package com.eightsines.bpe.util

@JsModule("uuid")
@JsNonModule
external val uuid: dynamic

class UidFactoryImpl : UidFactory {
    @Suppress("UnsafeCastFromDynamic")
    override fun createUid(): String = uuid.v4()
}
