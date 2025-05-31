package com.eightsines.bpe.bag

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

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

        val packerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_UNPACKER_NAME)

        val isPolymorphic = annotation.getArgumentValue<Boolean>(STUFF_ARGUMENT_IS_POLYMORPHIC_NAME)
            ?: STUFF_ARGUMENT_IS_POLYMORPHIC_DEFAULT

        val generatedSimpleName = buildString {
            append(classDescriptor.nameDescriptor.simpleName.replace(".", "_"))
            append('_')
            append(if (isPolymorphic) GENERATED_SUFFIX_POLYMORPHIC_STUFF else GENERATED_SUFFIX_STUFF)
        }

        val packerDescriptor = when {
            packerName.isNullOrEmpty() -> NameDescriptor(currentPackageName, generatedSimpleName)
            packerName == STUFF_SKIP_GENERATION_NAME -> null

            else -> {
                val descriptor = resolver.getClassDescriptorByName(currentPackageName, packerName)

                if (descriptor == null) {
                    logger.error("@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": unable to find stuff packer class \"$packerName\"", classDeclaration)
                    return null
                }

                descriptor.nameDescriptor
            }
        }

        val unpackerNameDescriptor = when {
            unpackerName.isNullOrEmpty() -> NameDescriptor(currentPackageName, generatedSimpleName)
            unpackerName == STUFF_SKIP_GENERATION_NAME -> null

            else -> {
                val descriptor = resolver.getClassDescriptorByName(currentPackageName, unpackerName)

                if (descriptor == null) {
                    logger.error(
                        "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": unable to find stuff unpacker class \"$unpackerName\"",
                        classDeclaration
                    )
                    return null
                }

                descriptor.nameDescriptor
            }
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
        } else if (packerName.isNullOrEmpty() || unpackerName.isNullOrEmpty()) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": no fields annotated with @$WARE_ANNOTATION_NAME found",
                classDeclaration,
            )

            return null
        }

        return BagDescriptor.Stuff(
            classDescriptor = classDescriptor,
            packerDescriptor = packerDescriptor,
            unpackerDescriptor = unpackerNameDescriptor,
            wares = wares,
            generatedSimpleName = if (packerName.isNullOrEmpty() || unpackerName.isNullOrEmpty()) generatedSimpleName else null,
            shouldGeneratePacker = packerName.isNullOrEmpty(),
            shouldGenerateUnpacker = unpackerName.isNullOrEmpty(),
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

        val fieldName = annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD_NAME)

        if (fieldName.isNullOrEmpty()) {
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
        val packerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_UNPACKER_NAME)
        val version = annotation.getArgumentValue<Int>(WARE_ARGUMENT_VERSION_NAME) ?: WARE_ARGUMENT_VERSION_DEFAULT

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

        val packerDescriptor = if (packerName.isNullOrEmpty()) {
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

        val unpackerDescriptor = if (unpackerName.isNullOrEmpty()) {
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

    private companion object {
        private val STUFF_ANNOTATION_NAME = requireNotNull(BagStuff::class.simpleName)
        private val STUFF_ANNOTATION_NAME_DESCRIPTOR = BagStuff::class.nameDescriptor

        private val WARE_ANNOTATION_NAME = requireNotNull(BagStuffWare::class.simpleName)
        private val WARE_ANNOTATION_NAME_DESCRIPTOR = BagStuffWare::class.nameDescriptor

        private val NOTHING_DESCRIPTOR = TypeDescriptor.Type(NameDescriptor("kotlin", "Nothing"), false)

        private const val STUFF_ARGUMENT_PACKER_NAME = "packer"
        private const val STUFF_ARGUMENT_UNPACKER_NAME = "unpacker"
        private const val STUFF_SKIP_GENERATION_NAME = "_"

        private const val STUFF_ARGUMENT_IS_POLYMORPHIC_NAME = "isPolymorphic"
        private const val STUFF_ARGUMENT_IS_POLYMORPHIC_DEFAULT = false // defaultArguments is not working for JS

        private const val WARE_ARGUMENT_INDEX_NAME = "index"
        private const val WARE_ARGUMENT_FIELD_NAME = "field"
        private const val WARE_ARGUMENT_TYPE_NAME = "type"
        private const val WARE_ARGUMENT_PACKER_NAME = "packer"
        private const val WARE_ARGUMENT_UNPACKER_NAME = "unpacker"

        private const val WARE_ARGUMENT_VERSION_NAME = "version"
        private const val WARE_ARGUMENT_VERSION_DEFAULT = 1 // defaultArguments is not working for JS

        private const val GENERATED_SUFFIX_STUFF = "Stuff"
        private const val GENERATED_SUFFIX_POLYMORPHIC_STUFF = "PolymorphicStuff"
    }
}
