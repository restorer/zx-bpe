package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

class BagSinglefieldParser(private val logger: KSPLogger) {
    fun parse(resolver: Resolver, classDeclaration: KSClassDeclaration): BagDescriptor.Singlefield? {
        val classQualifiedName = classDeclaration.qualifiedName?.asString() ?: return null

        return if (classDeclaration.modifiers.contains(Modifier.VALUE)) {
            parseValueClass(classDeclaration, classQualifiedName)
        } else {
            val annotation = classDeclaration.annotations.firstOrNull { it.annotationQualifiedName == ANNOTATION_QUALIFIED_NAME }

            if (annotation != null) {
                parseSinglefield(resolver, classDeclaration, classQualifiedName, annotation)
            } else {
                null
            }
        }
    }

    private fun parseValueClass(
        classDeclaration: KSClassDeclaration,
        classQualifiedName: String,
    ): BagDescriptor.Singlefield? {
        val constructorParameters = classDeclaration.primaryConstructor?.parameters ?: return null

        if (constructorParameters.size != 1) {
            return null
        }

        val constructorParameterName = constructorParameters[0].name?.asString() ?: return null
        val constructorParameterTypeQualifiedName = constructorParameters[0].type.typeQualifiedName ?: return null
        val bagPrimitiveDescriptor = BagDescriptor.Primitive.of(constructorParameterTypeQualifiedName) ?: return null

        return BagDescriptor.Singlefield(
            classQualifiedName = classQualifiedName,
            fieldName = constructorParameterName,
            creatorQualifiedName = classQualifiedName,
            shouldCheckCreatorException = false,
            bagPrimitiveDescriptor = bagPrimitiveDescriptor,
        )
    }

    private fun parseSinglefield(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classQualifiedName: String,
        annotation: KSAnnotation,
    ): BagDescriptor.Singlefield? {
        val fieldName = annotation.getArgumentValue<String>(ARGUMENT_FIELD_NAME) ?: return null
        val creatorName = annotation.getArgumentValue<String>(ARGUMENT_CREATOR_NAME) ?: return null

        val fieldTypeQualifiedName = classDeclaration.getAllProperties()
            .firstOrNull { it.simpleName.asString() == fieldName }
            ?.type
            ?.typeQualifiedName

        if (fieldTypeQualifiedName == null) {
            logger.error("@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": has no field \"$fieldName\"", classDeclaration)
            return null
        }

        val bagPrimitiveDescriptor = BagDescriptor.Primitive.of(fieldTypeQualifiedName)

        if (bagPrimitiveDescriptor == null) {
            logger.error(
                "@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": unsupported primitive type \"$fieldTypeQualifiedName\" of field \"$fieldName\"",
                classDeclaration,
            )

            return null
        }

        val creatorDeclaration = resolver.getFunctionDeclarationByName(classDeclaration.packageName.asString(), creatorName)
        val creatorQualifiedName = creatorDeclaration?.qualifiedName?.asString()

        if (creatorQualifiedName == null) {
            logger.error("@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": unable to find creator \"$creatorName\"", classDeclaration)
            return null
        }

        val creatorParameters = creatorDeclaration.parameters

        if (creatorParameters.size != 1) {
            logger.error(
                "@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": creator \"$creatorName\" must have exactly 1 parameter (but has ${creatorParameters.size})",
                classDeclaration,
            )

            return null
        }

        val creatorParameterTypeQualifiedName = creatorParameters[0].type.typeQualifiedName

        if (creatorParameterTypeQualifiedName == null) {
            logger.error("@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": unable to resolve parameter tyoe of creator \"$creatorName\"", classDeclaration)
            return null
        }

        if (fieldTypeQualifiedName != creatorParameterTypeQualifiedName) {
            logger.error(
                "@$ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": field (\"$fieldName\") type \"$fieldTypeQualifiedName\" is different from creator (\"$creatorName\") parameter type \"$creatorParameterTypeQualifiedName\"",
                classDeclaration,
            )

            return null
        }

        return BagDescriptor.Singlefield(
            classQualifiedName = classQualifiedName,
            fieldName = fieldName,
            creatorQualifiedName = creatorQualifiedName,
            shouldCheckCreatorException = true,
            bagPrimitiveDescriptor = bagPrimitiveDescriptor,
        )
    }

    private companion object {
        private val ANNOTATION_SIMPLE_NAME = requireNotNull(BagSinglefield::class.simpleName)
        private val ANNOTATION_QUALIFIED_NAME = requireNotNull(BagSinglefield::class.qualifiedName)

        private const val ARGUMENT_FIELD_NAME = "field"
        private const val ARGUMENT_CREATOR_NAME = "creator"
    }
}
