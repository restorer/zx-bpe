package com.eightsines.bpe.bag

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName

val NameDescriptor.pouetClassName: ClassName
    get() = ClassName(packageName, simpleName)

val NameDescriptor.pouetMemberName: MemberName
    get() = MemberName(packageName, simpleName)

val DeclarationDescriptor.pouetTypeName: TypeName
    get() = if (numTypeParameters > 0) {
        nameDescriptor.pouetClassName.parameterizedBy(List(numTypeParameters) { STAR })
    } else {
        nameDescriptor.pouetClassName
    }

val TypeDescriptor.pouetTypeName: TypeName
    get() = when (this) {
        is TypeDescriptor.Star -> STAR

        is TypeDescriptor.Type -> if (typeParameterDescriptors.isNotEmpty()) {
            nameDescriptor.pouetClassName.parameterizedBy(typeParameterDescriptors.map { it.pouetTypeName })
        } else {
            nameDescriptor.pouetClassName
        }
    }

val FunctionDescriptor.pouetMemberName: MemberName
    get() = MemberName(nameDescriptor.packageName, nameDescriptor.simpleName)
