package com.eightsines.bpe.bag

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
@Repeatable
annotation class BagStuff(
    val of: String = "",
    val packer: String = "",
    val unpacker: String = "",
    val polymorphicOf: KClass<*> = Nothing::class,
    val polymorphicId: Int = 0,
    val isPolymorphic: Boolean = false,
)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Repeatable
annotation class BagStuffWare(
    val index: Int,
    val field: String = "",
    val type: KClass<*> = Nothing::class,
    val packer: String = "",
    val unpacker: String = "",
    val version: Int = 1,
    val fallback: String = "",
)
