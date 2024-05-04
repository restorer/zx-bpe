package com.eightsines.bpe.engine.util

@JsModule("uuid")
@JsNonModule
external val uuid: dynamic

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class UidFactory {
    @Suppress("UnsafeCastFromDynamic")
    actual fun createUid(): String = uuid.v4()
}
