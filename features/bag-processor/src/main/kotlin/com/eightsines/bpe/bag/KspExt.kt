package com.eightsines.bpe.bag

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlin.reflect.KClass

val KClass<*>.nameDescriptor: NameDescriptor
    get() {
        val simpleName = requireNotNull(this.simpleName)
        val qualifiedName = requireNotNull(this.qualifiedName)

        return NameDescriptor(qualifiedName.dropLast(simpleName.length + 1), simpleName)
    }

val KSDeclaration.declarationDescriptor: DeclarationDescriptor
    get() = DeclarationDescriptor(this@declarationDescriptor.nameDescriptor, typeParameters.size)

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
    get() = if (declaration is KSTypeParameter) {
        TypeDescriptor.Star
    } else {
        TypeDescriptor.Type(
            declaration.nameDescriptor,
            isMarkedNullable,
            arguments.map { it.type?.typeDescriptor ?: TypeDescriptor.Star },
        )
    }

fun Resolver.getClassDeclarationByName(currentPackageName: String, className: String): KSClassDeclaration? {
    getClassDeclarationByName(className)?.let { return it }
    getClassDeclarationByName("$currentPackageName.$className")?.let { return it }
    return null
}

fun Resolver.getClassDescriptorByName(currentPackageName: String, className: String): DeclarationDescriptor? =
    getClassDeclarationByName(currentPackageName, className)?.declarationDescriptor

fun Resolver.getFunctionDescriptorByName(currentPackageName: String, functionName: String): FunctionDescriptor? {
    val declaration = getFunctionDeclarationByName(currentPackageName, functionName) ?: return null
    val packageName = declaration.packageName.asString().ifEmpty { currentPackageName }

    return FunctionDescriptor(
        NameDescriptor(
            packageName,
            requireNotNull(declaration.qualifiedName).asString().substring(packageName.length + 1),
        ),
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
