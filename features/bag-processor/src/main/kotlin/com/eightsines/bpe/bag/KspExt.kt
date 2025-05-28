package com.eightsines.bpe.bag

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlin.reflect.KClass

data class NameDescriptor(val packageName: String, val simpleName: String) {
    val qualifiedName: String = "$packageName.$simpleName"
    override fun toString(): String = "$packageName/$simpleName"
}

data class DeclarationDescriptor(
    val nameDescriptor: NameDescriptor,
    val numTypeParameters: Int = 0,
) {
    override fun toString(): String = buildString {
        append(nameDescriptor.toString())

        if (numTypeParameters > 0) {
            append('<')

            repeat(numTypeParameters) {
                append('*')

                if (it > 0) {
                    append(',')
                }
            }

            append('>')
        }
    }
}

sealed interface TypeDescriptor {
    val rawTypeDescriptor: TypeDescriptor

    data object Star : TypeDescriptor {
        override val rawTypeDescriptor: TypeDescriptor = this
        override fun toString(): String = "*"
    }

    data class Type(
        val nameDescriptor: NameDescriptor,
        val isNullable: Boolean,
        val typeParameterDescriptors: List<TypeDescriptor> = emptyList(),
    ) : TypeDescriptor {
        override val rawTypeDescriptor: TypeDescriptor =
            if (typeParameterDescriptors.isEmpty()) this else copy(typeParameterDescriptors = emptyList())

        override fun toString(): String = buildString {
            append(nameDescriptor.toString())

            if (typeParameterDescriptors.isNotEmpty()) {
                append('<')

                typeParameterDescriptors.forEachIndexed { index, typeDescriptor ->
                    append(typeDescriptor.toString())

                    if (index > 0) {
                        append(',')
                    }
                }

                append('>')
            }

            if (isNullable) {
                append('?')
            }
        }
    }
}

fun DeclarationDescriptor.asRawTypeDescriptor(): TypeDescriptor =
    TypeDescriptor.Type(nameDescriptor, false)

data class FunctionParameterDescriptor(val name: String, val typeDescriptor: TypeDescriptor)

data class FunctionDescriptor(
    val nameDescriptor: NameDescriptor,
    val returnTypeDescriptor: TypeDescriptor?,
    val parameters: List<FunctionParameterDescriptor>,
) {
    override fun toString(): String = nameDescriptor.toString()
}

val KClass<*>.nameDescriptor: NameDescriptor
    get() {
        val simpleName = requireNotNull(this.simpleName)
        val qualifiedName = requireNotNull(this.qualifiedName)

        return NameDescriptor(qualifiedName.dropLast(simpleName.length + 1), simpleName)
    }

val KSDeclaration.declarationDescriptor: DeclarationDescriptor
    get() = DeclarationDescriptor(nameDescriptor, typeParameters.size)

val KSDeclaration.nameDescriptor: NameDescriptor
    get() {
        val packageName = this.packageName.asString()
        val qualifiedName = requireNotNull(this.qualifiedName).asString()

        return NameDescriptor(packageName, qualifiedName.substring(packageName.length + 1))
    }

val KSAnnotation.nameDescriptor: NameDescriptor
    get() = annotationType.resolve().declaration.nameDescriptor

inline fun <reified T> KSAnnotation.getArgumentValue(name: String) =
    arguments.firstOrNull { it.name?.asString() == name }?.value as? T
        ?: defaultArguments.firstOrNull { it.name?.asString() == name }?.value as? T

val KSTypeReference.typeDescriptor: TypeDescriptor
    get() = resolve().typeDescriptor

val KSType.typeDescriptor: TypeDescriptor
    get() = TypeDescriptor.Type(
        declaration.nameDescriptor,
        isMarkedNullable,
        arguments.map { it.type?.typeDescriptor ?: TypeDescriptor.Star },
    )

fun Resolver.getClassDescriptorByName(currentPackageName: String, className: String): DeclarationDescriptor? {
    getClassDeclarationByName(className)?.let { return it.declarationDescriptor }
    getClassDeclarationByName("$currentPackageName.$className")?.let { return it.declarationDescriptor }
    return null
}

fun Resolver.getFunctionDescriptorByName(currentPackageName: String, functionName: String): FunctionDescriptor? {
    val declaration = getFunctionDeclarationByName(currentPackageName, functionName) ?: return null

    return FunctionDescriptor(
        NameDescriptor(declaration.packageName.asString().ifEmpty { currentPackageName }, functionName),
        declaration.returnType?.typeDescriptor,
        declaration.parameters.map {
            FunctionParameterDescriptor(it.name?.asString() ?: "", it.type.typeDescriptor)
        }
    )
}

private fun Resolver.getFunctionDeclarationByName(currentPackageName: String, functionName: String): KSFunctionDeclaration? {
    getFunctionDeclarationsByName(getKSNameFromString(functionName))
        .firstOrNull()
        ?.let { return it }

    getFunctionDeclarationsByName(getKSNameFromString("${currentPackageName}.$functionName"))
        .firstOrNull()
        ?.let { return it }

    val parts = functionName.split('.')

    if (parts.size < 2) {
        return null
    }

    val prefixParts = parts.dropLast(1)
    val lastPart = parts.last()

    if (prefixParts.last() == COMPANION_NAME) {
        return null
    }

    val companionFunctionName = "${prefixParts.joinToString(".")}.${COMPANION_NAME}.$lastPart"

    getFunctionDeclarationsByName(getKSNameFromString(companionFunctionName))
        .firstOrNull()
        ?.let { return it }

    return getFunctionDeclarationsByName(getKSNameFromString("${currentPackageName}.$companionFunctionName")).firstOrNull()
}

private const val COMPANION_NAME = "Companion"
