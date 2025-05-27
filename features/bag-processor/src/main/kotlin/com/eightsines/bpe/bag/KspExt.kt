package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference

val KSAnnotation.annotationQualifiedName: String?
    get() = annotationType.resolve().declaration.qualifiedName?.asString()

inline fun <reified T> KSAnnotation.getArgumentValue(name: String) =
    arguments.firstOrNull { it.name?.asString() == name }?.value as? T
        ?: defaultArguments.firstOrNull { it.name?.asString() == name }?.value as? T

val KSTypeReference.typeQualifiedName: String?
    get() = resolve().typeQualifiedName

val KSType.typeQualifiedName: String?
    get() {
        val qualifiedName = declaration.qualifiedName?.asString()

        return when {
            qualifiedName == null -> null
            isMarkedNullable -> "${qualifiedName}?"
            else -> qualifiedName
        }
    }

fun Resolver.getFunctionDeclarationByName(currentPackageName: String, functionName: String): KSFunctionDeclaration? {
    var declaration = getFunctionDeclarationsByName(getKSNameFromString(functionName)).firstOrNull()

    if (declaration != null) {
        return declaration
    }

    declaration = getFunctionDeclarationsByName(getKSNameFromString("${currentPackageName}.$functionName")).firstOrNull()

    if (declaration != null) {
        return declaration
    }

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

    declaration = getFunctionDeclarationsByName(getKSNameFromString(companionFunctionName)).firstOrNull()

    if (declaration != null) {
        return declaration
    }

    return getFunctionDeclarationsByName(getKSNameFromString("${currentPackageName}.$companionFunctionName")).firstOrNull()
}

private const val COMPANION_NAME = "Companion"
