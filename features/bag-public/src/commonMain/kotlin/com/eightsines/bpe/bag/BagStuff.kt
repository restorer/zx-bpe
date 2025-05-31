package com.eightsines.bpe.bag

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BagStuff(val packer: String = "", val unpacker: String = "", val isPolymorphic: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Repeatable
annotation class BagStuffWare(
    val index: Int,
    val field: String = "",
    val type: KClass<*> = Nothing::class,
    val packer: String = "",
    val unpacker: String = "",
    val version: Int = 1,
)
