package com.eightsines.bpe.bag

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

class BagStuffParser(private val logger: KSPLogger) {
    fun parseAll(resolver: Resolver): List<BagDescriptor.Stuff> = buildList {
        for (declaration in resolver.getSymbolsWithAnnotation(STUFF_ANNOTATION_NAME_DESCRIPTOR.qualifiedName).filterIsInstance<KSClassDeclaration>()) {
            parse(resolver, declaration)?.let { add(it) }
        }
    }

    private fun parse(resolver: Resolver, classDeclaration: KSClassDeclaration): BagDescriptor.Stuff? {
        val annotation = classDeclaration.annotations.firstOrNull { it.nameDescriptor == STUFF_ANNOTATION_NAME_DESCRIPTOR }

        return if (annotation != null) {
            parseStuff(resolver, classDeclaration, classDeclaration.declarationDescriptor, annotation)
        } else {
            null
        }
    }

    private fun parseStuff(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classDescriptor: DeclarationDescriptor,
        annotation: KSAnnotation,
    ): BagDescriptor.Stuff? {
        val currentPackageName = classDeclaration.packageName.asString()

        val packerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_PACKER_NAME) ?: ""
        val unpackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_UNPACKER_NAME) ?: ""

        val polymorphicOfDescriptor = annotation.getArgumentValue<KSType>(STUFF_ARGUMENT_POLYMORPHIC_OF_NAME)
            ?.typeDescriptor
            ?.let { it as? TypeDescriptor.Type }
            ?.nameDescriptor

        val polymorphicId = annotation.getArgumentValue<Int>(STUFF_ARGUMENT_POLYMORPHIC_ID_NAME) ?: 0
        val isPolymorphic = annotation.getArgumentValue<Boolean>(STUFF_ARGUMENT_IS_POLYMORPHIC_NAME) ?: false

        if ((polymorphicOfDescriptor != null && polymorphicId == 0) || (polymorphicOfDescriptor == null && polymorphicId != 0)) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": \"$STUFF_ARGUMENT_POLYMORPHIC_OF_NAME\" and \"$STUFF_ARGUMENT_POLYMORPHIC_ID_NAME\" must be set together",
                classDeclaration,
            )

            return null
        }

        if (polymorphicOfDescriptor != null && isPolymorphic) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": \"$STUFF_ARGUMENT_POLYMORPHIC_OF_NAME\" and \"$STUFF_ARGUMENT_IS_POLYMORPHIC_NAME\" must not be used together",
                classDeclaration,
            )

            return null
        }

        val generatedSimpleName = buildString {
            append(classDescriptor.nameDescriptor.simpleName.replace(".", "_"))
            append('_')
            append(if (polymorphicOfDescriptor != null) GENERATED_SUFFIX_POLYMORPHIC_STUFF else GENERATED_SUFFIX_STUFF)
        }

        val packerDescriptor = when {
            packerName.isEmpty() -> NameDescriptorReference.Strict(currentPackageName, generatedSimpleName)
            packerName == STUFF_SKIP_GENERATION_NAME -> null
            else -> NameDescriptorReference.Unresolved(currentPackageName, packerName)
        }

        val unpackerDescriptor = when {
            unpackerName.isEmpty() -> NameDescriptorReference.Strict(currentPackageName, generatedSimpleName)
            unpackerName == STUFF_SKIP_GENERATION_NAME -> null
            else -> NameDescriptorReference.Unresolved(currentPackageName, unpackerName)
        }

        val propertiesMap: Map<String, KSPropertyDeclaration> =
            classDeclaration.getAllProperties().associateBy { it.simpleName.asString() }

        val propertyWares = classDeclaration.getDeclaredProperties()
            .mapNotNull { parsePropertyWare(resolver, classDescriptor, currentPackageName, it) }

        val referenceWares = classDeclaration.annotations
            .mapNotNull { parseReferenceWare(resolver, classDeclaration, classDescriptor, currentPackageName, propertiesMap, it) }

        val wares = (propertyWares + referenceWares).toList().sortedBy { it.index }

        if (!wares.isEmpty()) {
            if (wares[0].index != 1) {
                logger.error(
                    "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": indexes of @$WARE_ANNOTATION_NAME must starts with 1",
                    classDeclaration,
                )

                return null
            }

            for (i in 1..<wares.size) {
                if (wares[i - 1].index == wares[i].index) {
                    logger.error(
                        "@$WARE_ANNOTATION_NAME of \"$classDescriptor.${wares[i].fieldName}\": indexes must be unique",
                        wares[i].sourceSymbol,
                    )

                    return null
                }

                if (wares[i - 1].index + 1 != wares[i].index) {
                    logger.error(
                        "@$WARE_ANNOTATION_NAME of \"$classDescriptor.${wares[i].fieldName}\": indexes must be consecutive",
                        wares[i].sourceSymbol,
                    )

                    return null
                }
            }
        }

        return BagDescriptor.Stuff(
            classDescriptor = classDescriptor,
            isSealed = classDeclaration.modifiers.contains(Modifier.SEALED),
            packerReference = packerDescriptor,
            unpackerReference = unpackerDescriptor,
            polymorphicCase = if (polymorphicOfDescriptor != null) {
                BagStuffPolymorphicCase(polymorphicOfDescriptor, polymorphicId)
            } else {
                null
            },
            wares = wares,
            generateInfo = if (packerName.isEmpty() || unpackerName.isEmpty()) {
                BagStuffGenerateInfo(
                    NameDescriptor(classDescriptor.nameDescriptor.packageName, generatedSimpleName),
                    shouldGeneratePacker = packerName.isEmpty(),
                    shouldGenerateUnpacker = unpackerName.isEmpty(),
                    isPolymorphic = isPolymorphic,
                )
            } else {
                null
            },
            sourceSymbol = classDeclaration,
            sourceFile = requireNotNull(classDeclaration.containingFile),
        )
    }

    private fun parsePropertyWare(
        resolver: Resolver,
        classDescriptor: DeclarationDescriptor,
        currentPackageName: String,
        propertyDeclaration: KSPropertyDeclaration,
    ): BagStuffWareDescriptor? {
        val annotations = propertyDeclaration.annotations
            .filter { it.nameDescriptor == WARE_ANNOTATION_NAME_DESCRIPTOR }
            .toList()

        if (annotations.isEmpty()) {
            return null
        }

        val fieldName = propertyDeclaration.simpleName.asString()

        if (annotations.size > 1) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor.$fieldName\": multiple ware annotations is not allowed on property",
                propertyDeclaration,
            )

            return null
        }

        val annotation = annotations[0]

        if (!annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD_NAME).isNullOrEmpty()) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor.$fieldName\": \"$WARE_ARGUMENT_FIELD_NAME\" argument is not allowed on property",
                propertyDeclaration,
            )

            return null
        }

        return parseWare(
            resolver = resolver,
            classDescriptor = classDescriptor,
            currentPackageName = currentPackageName,
            annotation = annotation,
            fieldName = fieldName,
            sourceTypeDescriptor = propertyDeclaration.type.typeDescriptor,
            sourceSymbol = propertyDeclaration,
        )
    }

    private fun parseReferenceWare(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classDescriptor: DeclarationDescriptor,
        currentPackageName: String,
        propertiesMap: Map<String, KSPropertyDeclaration>,
        annotation: KSAnnotation,
    ): BagStuffWareDescriptor? {
        if (annotation.nameDescriptor != WARE_ANNOTATION_NAME_DESCRIPTOR) {
            return null
        }

        val fieldName = annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD_NAME) ?: ""

        if (fieldName.isEmpty()) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor\": \"$WARE_ARGUMENT_FIELD_NAME\" argument is required",
                classDeclaration,
            )

            return null
        }

        val propertyDeclaration = propertiesMap[fieldName]

        if (propertyDeclaration == null) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor\": property \"$fieldName\" is not found",
                classDeclaration,
            )

            return null
        }

        return parseWare(
            resolver = resolver,
            classDescriptor = classDescriptor,
            currentPackageName = currentPackageName,
            annotation = annotation,
            fieldName = fieldName,
            sourceTypeDescriptor = propertyDeclaration.type.typeDescriptor,
            sourceSymbol = propertyDeclaration,
        )
    }

    private fun parseWare(
        resolver: Resolver,
        classDescriptor: DeclarationDescriptor,
        currentPackageName: String,
        annotation: KSAnnotation,
        fieldName: String,
        sourceTypeDescriptor: TypeDescriptor,
        sourceSymbol: KSNode,
    ): BagStuffWareDescriptor? {
        val index = annotation.getArgumentValue<Int>(WARE_ARGUMENT_INDEX_NAME) ?: return null
        val customTypeDescriptor = annotation.getArgumentValue<KSType>(WARE_ARGUMENT_TYPE_NAME)?.typeDescriptor
        val packerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_PACKER_NAME) ?: ""
        val unpackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_UNPACKER_NAME) ?: ""
        val version = annotation.getArgumentValue<Int>(WARE_ARGUMENT_VERSION_NAME) ?: 1

        val typeDescriptor = if (customTypeDescriptor == null || customTypeDescriptor == NOTHING_DESCRIPTOR) {
            sourceTypeDescriptor
        } else {
            customTypeDescriptor
        }

        if (typeDescriptor is TypeDescriptor.Star) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\", parameter \"$fieldName\": unable resolve type",
                sourceSymbol,
            )

            return null
        }

        val packerDescriptor = if (packerName.isEmpty()) {
            null
        } else {
            val descriptor = resolver.getFunctionDescriptorByName(currentPackageName, packerName)
                ?: resolver.getFunctionDescriptorByName(currentPackageName, "${classDescriptor.nameDescriptor.simpleName}.$packerName")

            if (descriptor == null) {
                logger.error(
                    "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\", parameter \"$fieldName\": unable to find field packer function \"$packerName\"",
                    sourceSymbol,
                )

                return null
            }

            descriptor
        }

        val unpackerDescriptor = if (unpackerName.isEmpty()) {
            null
        } else {
            val descriptor = resolver.getFunctionDescriptorByName(currentPackageName, unpackerName)
                ?: resolver.getFunctionDescriptorByName(currentPackageName, "${classDescriptor.nameDescriptor.simpleName}.$unpackerName")

            if (descriptor == null) {
                logger.error(
                    "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\", parameter \"$fieldName\": unable to find field unpacker function \"$unpackerName\"",
                    sourceSymbol,
                )

                return null
            }

            descriptor
        }

        return BagStuffWareDescriptor(
            fieldName = fieldName,
            index = index,
            version = version,
            typeDescriptor = typeDescriptor,
            packerDescriptor = packerDescriptor,
            unpackerDescriptor = unpackerDescriptor,
            sourceClassDescriptor = classDescriptor,
            sourceSymbol = sourceSymbol,
        )
    }

    companion object {
        val STUFF_ANNOTATION_NAME = requireNotNull(BagStuff::class.simpleName)
        private val STUFF_ANNOTATION_NAME_DESCRIPTOR = BagStuff::class.nameDescriptor

        val WARE_ANNOTATION_NAME = requireNotNull(BagStuffWare::class.simpleName)
        private val WARE_ANNOTATION_NAME_DESCRIPTOR = BagStuffWare::class.nameDescriptor

        private val NOTHING_DESCRIPTOR = TypeDescriptor.Type(NameDescriptor("kotlin", "Nothing"), false)

        private const val STUFF_ARGUMENT_PACKER_NAME = "packer"
        private const val STUFF_ARGUMENT_UNPACKER_NAME = "unpacker"
        private const val STUFF_SKIP_GENERATION_NAME = "_"

        private const val STUFF_ARGUMENT_POLYMORPHIC_OF_NAME = "polymorphicOf"
        private const val STUFF_ARGUMENT_POLYMORPHIC_ID_NAME = "polymorphicId"
        private const val STUFF_ARGUMENT_IS_POLYMORPHIC_NAME = "isPolymorphic"

        private const val WARE_ARGUMENT_INDEX_NAME = "index"
        private const val WARE_ARGUMENT_FIELD_NAME = "field"
        private const val WARE_ARGUMENT_TYPE_NAME = "type"
        private const val WARE_ARGUMENT_PACKER_NAME = "packer"
        private const val WARE_ARGUMENT_UNPACKER_NAME = "unpacker"
        private const val WARE_ARGUMENT_VERSION_NAME = "version"

        private const val GENERATED_SUFFIX_STUFF = "Stuff"
        private const val GENERATED_SUFFIX_POLYMORPHIC_STUFF = "PolymorphicStuff"
    }
}
