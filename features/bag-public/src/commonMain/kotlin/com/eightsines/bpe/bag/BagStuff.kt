package com.eightsines.bpe.bag

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class BagStuff(
    val packer: String = "",
    val unpacker: String = "",
    val polymorphicOf: KClass<*> = Nothing::class,
    val polymorphicId: Int = 0,
    val isPolymorphic: Boolean = false,
)

@Retention(AnnotationRetention.BINARY)
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
