package com.eightsines.bpe.bag

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

class BagStuffParser(private val logger: KSPLogger) {
    fun parseAll(resolver: Resolver): List<BagDescriptor.Stuff> = buildList {
        for (symbol in resolver.getSymbolsWithAnnotation(STUFF_ANNOTATION_NAME_DESCRIPTOR.qualifiedName)) {
            when (symbol) {
                is KSFile -> addAll(parseFile(resolver, symbol))
                is KSClassDeclaration -> parseClass(resolver, symbol)?.let(::add)
                else -> logger.error("@$STUFF_ANNOTATION_NAME: unsupported target $symbol")
            }
        }
    }

    fun parseClass(resolver: Resolver, classDeclaration: KSClassDeclaration): BagDescriptor.Stuff? {
        val annotations = classDeclaration.annotations
            .filter { it.nameDescriptor == STUFF_ANNOTATION_NAME_DESCRIPTOR }
            .toList()

        return when {
            annotations.size > 1 -> {
                logger.error("@$STUFF_ANNOTATION_NAME of \"${classDeclaration.declarationDescriptor}\": more then one stuff annotation is not supported for classes")
                null
            }

            annotations.size == 1 -> {
                val annotation = annotations[0]

                if (!annotation.getArgumentValue<String>(STUFF_ARGUMENT_OF).isNullOrEmpty()) {
                    logger.error("@$STUFF_ANNOTATION_NAME of \"${classDeclaration.declarationDescriptor}\": argument \"$STUFF_ARGUMENT_OF\" is not supported for classes")
                    null
                } else {
                    parseStuff(
                        resolver = resolver,
                        classDeclaration = classDeclaration,
                        classDescriptor = classDeclaration.declarationDescriptor,
                        annotation = annotation,
                        currentPackageName = classDeclaration.packageName.asString(),
                        sourceSymbol = classDeclaration,
                        sourceFile = classDeclaration.containingFile,
                    )
                }
            }

            else -> null
        }
    }

    private fun parseFile(resolver: Resolver, declarationContainer: KSFile): List<BagDescriptor.Stuff> = buildList {
        for (annotation in declarationContainer.annotations.filter { it.nameDescriptor == STUFF_ANNOTATION_NAME_DESCRIPTOR }) {
            val ofClass = annotation.getArgumentValue<String>(STUFF_ARGUMENT_OF) ?: ""

            if (ofClass.isEmpty()) {
                logger.error(
                    "@$STUFF_ANNOTATION_NAME of \"${declarationContainer.fileName}\": argument \"$STUFF_ARGUMENT_OF\" is not supported for classes",
                    declarationContainer,
                )

                continue
            }

            val currentPackageName = declarationContainer.packageName.asString()
            val classDeclaration = resolver.getClassDeclarationByName(currentPackageName, ofClass)

            if (classDeclaration == null) {
                logger.error("@$STUFF_ANNOTATION_NAME of \"${declarationContainer.fileName}\": class \"$ofClass\" is not found", declarationContainer)
                continue
            }

            parseStuff(
                resolver = resolver,
                classDeclaration = classDeclaration,
                classDescriptor = classDeclaration.declarationDescriptor,
                annotation = annotation,
                currentPackageName = classDeclaration.packageName.asString(),
                sourceSymbol = declarationContainer,
                sourceFile = declarationContainer,
            )?.let(::add)
        }
    }


    private fun parseStuff(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classDescriptor: DeclarationDescriptor,
        annotation: KSAnnotation,
        currentPackageName: String,
        sourceSymbol: KSNode,
        sourceFile: KSFile?,
    ): BagDescriptor.Stuff? {
        val packerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_PACKER) ?: ""
        val unpackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_UNPACKER) ?: ""

        val polymorphicOfDescriptor = annotation.getArgumentValue<KSType>(STUFF_ARGUMENT_POLYMORPHIC_OF)
            ?.typeDescriptor
            ?.let { it as? TypeDescriptor.Type }
            ?.nameDescriptor

        val polymorphicId = annotation.getArgumentValue<Int>(STUFF_ARGUMENT_POLYMORPHIC_ID) ?: 0
        val isPolymorphic = annotation.getArgumentValue<Boolean>(STUFF_ARGUMENT_IS_POLYMORPHIC) ?: false

        if ((polymorphicOfDescriptor != null && polymorphicId == 0) || (polymorphicOfDescriptor == null && polymorphicId != 0)) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": \"$STUFF_ARGUMENT_POLYMORPHIC_OF\" and \"$STUFF_ARGUMENT_POLYMORPHIC_ID\" must be set together",
                sourceSymbol,
            )

            return null
        }

        if (polymorphicOfDescriptor != null && isPolymorphic) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": \"$STUFF_ARGUMENT_POLYMORPHIC_OF\" and \"$STUFF_ARGUMENT_IS_POLYMORPHIC\" must not be used together",
                sourceSymbol,
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
                    sourceSymbol,
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
            sourceSymbol = sourceSymbol,
            sourceFile = sourceFile,
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

        if (!annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD).isNullOrEmpty()) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor.$fieldName\": \"$WARE_ARGUMENT_FIELD\" argument is not allowed on property",
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

        val fieldName = annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD) ?: ""

        if (fieldName.isEmpty()) {
            logger.error(
                "@$WARE_ANNOTATION_NAME of \"$classDescriptor\": \"$WARE_ARGUMENT_FIELD\" argument is required",
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
        val index = annotation.getArgumentValue<Int>(WARE_ARGUMENT_INDEX) ?: return null
        val customTypeDescriptor = annotation.getArgumentValue<KSType>(WARE_ARGUMENT_TYPE)?.typeDescriptor
        val packerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_PACKER) ?: ""
        val unpackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_UNPACKER) ?: ""
        val version = annotation.getArgumentValue<Int>(WARE_ARGUMENT_VERSION) ?: 1
        val fallback = annotation.getArgumentValue<String>(WARE_ARGUMENT_FALLBACK) ?: ""

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
            fallbackValue = fallback.ifEmpty { null },
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

        private const val STUFF_ARGUMENT_OF = "of"
        private const val STUFF_ARGUMENT_PACKER = "packer"
        private const val STUFF_ARGUMENT_UNPACKER = "unpacker"
        private const val STUFF_ARGUMENT_POLYMORPHIC_OF = "polymorphicOf"
        private const val STUFF_ARGUMENT_POLYMORPHIC_ID = "polymorphicId"
        private const val STUFF_ARGUMENT_IS_POLYMORPHIC = "isPolymorphic"
        private const val STUFF_SKIP_GENERATION_NAME = "_"

        private const val WARE_ARGUMENT_INDEX = "index"
        private const val WARE_ARGUMENT_FIELD = "field"
        private const val WARE_ARGUMENT_TYPE = "type"
        private const val WARE_ARGUMENT_PACKER = "packer"
        private const val WARE_ARGUMENT_UNPACKER = "unpacker"
        private const val WARE_ARGUMENT_VERSION = "version"
        private const val WARE_ARGUMENT_FALLBACK = "fallback"

        private const val GENERATED_SUFFIX_STUFF = "Stuff"
        private const val GENERATED_SUFFIX_POLYMORPHIC_STUFF = "PolymorphicStuff"
    }
}
