package com.eightsines.bpe.test

inline fun <S, R> performTest(arrange: () -> S, act: (S) -> R, assert: (R) -> Unit) =
    assert(act(arrange()))
