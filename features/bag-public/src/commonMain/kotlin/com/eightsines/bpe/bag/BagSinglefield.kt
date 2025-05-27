package com.eightsines.bpe.bag

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BagSinglefield(val field: String, val creator: String)
