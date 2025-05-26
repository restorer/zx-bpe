package com.eightsines.bpe.bag

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BagEnum(val value: String, val creator: String)
