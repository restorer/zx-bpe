package com.eightsines.bpe.bag

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class BagSinglefield(val field: String, val creator: String)
