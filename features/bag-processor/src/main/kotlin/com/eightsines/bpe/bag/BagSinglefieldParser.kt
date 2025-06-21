package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

class BagSinglefieldParser(private val logger: KSPLogger) {
    fun parse(resolver: Resolver, classDeclaration: KSClassDeclaration): BagDescriptor.Singlefield? {
        val classDescriptor = classDeclaration.declarationDescriptor

        // Class declaration has "VALUE" modifier, when class is in the same module,
        // bub "INLINE", when class is in the dependency module.
        return if (classDeclaration.modifiers.contains(Modifier.VALUE) || classDeclaration.modifiers.contains(Modifier.INLINE)) {
            parseValueClass(classDeclaration, classDescriptor)
        } else {
            val annotation = classDeclaration.annotations.firstOrNull { it.nameDescriptor == ANNOTATION_NAME_DESCRIPTOR }

            if (annotation != null) {
                parseSinglefield(resolver, classDeclaration, classDescriptor, annotation)
            } else {
                null
            }
        }
    }

    private fun parseValueClass(
        classDeclaration: KSClassDeclaration,
        classDescriptor: DeclarationDescriptor,
    ): BagDescriptor.Singlefield? {
        val constructorParameters = classDeclaration.primaryConstructor?.parameters ?: return null

        if (constructorParameters.size != 1) {
            return null
        }

        val constructorParameterName = constructorParameters[0].name?.asString() ?: return null
        val bagPrimitiveDescriptor = BagDescriptor.Primitive.of(constructorParameters[0].type.typeDescriptor) ?: return null

        return BagDescriptor.Singlefield(
            classDescriptor = classDescriptor,
            fieldName = constructorParameterName,
            creatorDescriptor = FunctionDescriptor(
                nameDescriptor = classDescriptor.nameDescriptor,
                returnTypeDescriptor = classDescriptor.asRawTypeDescriptor(),
                parameters = listOf(FunctionParameterDescriptor(constructorParameterName, constructorParameters[0].type.typeDescriptor)),
            ),
            shouldCheckCreatorException = false,
            primitiveDescriptor = bagPrimitiveDescriptor,
        )
    }

    private fun parseSinglefield(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classDescriptor: DeclarationDescriptor,
        annotation: KSAnnotation,
    ): BagDescriptor.Singlefield? {
        val fieldName = annotation.getArgumentValue<String>(ARGUMENT_FIELD) ?: return null
        val creatorName = annotation.getArgumentValue<String>(ARGUMENT_CREATOR) ?: return null

        val fieldTypeDescriptor = classDeclaration.getAllProperties()
            .firstOrNull { it.simpleName.asString() == fieldName }
            ?.type
            ?.typeDescriptor

        if (fieldTypeDescriptor == null) {
            logger.error("@$ANNOTATION_NAME of \"$classDescriptor\": has no field \"$fieldName\"", classDeclaration)
            return null
        }

        val primitiveDescriptor = BagDescriptor.Primitive.of(fieldTypeDescriptor)

        if (primitiveDescriptor == null) {
            logger.error(
                "@$ANNOTATION_NAME of \"$classDescriptor\": unsupported primitive type \"$fieldTypeDescriptor\" of field \"$fieldName\"",
                classDeclaration,
            )

            return null
        }

        val creatorDescriptor = resolver.getFunctionDescriptorByName(classDeclaration.packageName.asString(), creatorName)
            ?: resolver.getFunctionDescriptorByName(classDeclaration.packageName.asString(), "${classDescriptor.nameDescriptor.simpleName}.$creatorName")

        if (creatorDescriptor == null) {
            logger.error("@$ANNOTATION_NAME of \"$classDescriptor\": unable to find creator \"$creatorName\"", classDeclaration)
            return null
        }

        val creatorParameters = creatorDescriptor.parameters

        if (creatorParameters.size != 1) {
            logger.error(
                "@$ANNOTATION_NAME of \"$classDescriptor\": creator \"$creatorName\" must have exactly 1 parameter (but has ${creatorParameters.size})",
                classDeclaration,
            )

            return null
        }

        if (fieldTypeDescriptor != creatorParameters[0].typeDescriptor) {
            logger.error(
                "@$ANNOTATION_NAME of \"$classDescriptor\": field (\"$fieldName\") type \"$fieldTypeDescriptor\" is different from creator (\"$creatorName\") parameter type \"${creatorParameters[0].typeDescriptor}\"",
                classDeclaration,
            )

            return null
        }

        return BagDescriptor.Singlefield(
            classDescriptor = classDescriptor,
            fieldName = fieldName,
            creatorDescriptor = creatorDescriptor,
            shouldCheckCreatorException = true,
            primitiveDescriptor = primitiveDescriptor,
        )
    }

    private companion object {
        private val ANNOTATION_NAME = requireNotNull(BagSinglefield::class.simpleName)
        private val ANNOTATION_NAME_DESCRIPTOR = BagSinglefield::class.nameDescriptor

        private const val ARGUMENT_FIELD = "field"
        private const val ARGUMENT_CREATOR = "creator"
    }
}
