package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

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

        val staffPackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_STAFF_PACKER_NAME)
        val staffUnpackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_STAFF_UNPACKER_NAME)
        val suffixName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_SUFFIX_NAME) ?: STUFF_ARGUMENT_SUFFIX_DEFAULT

        val generatedSimpleName = classDescriptor.nameDescriptor.simpleName.replace(".", "_") + "_$suffixName"

        val staffPackerDescriptor = if (staffPackerName.isNullOrEmpty()) {
            NameDescriptor(currentPackageName, generatedSimpleName)
        } else {
            val descriptor = resolver.getClassDescriptorByName(currentPackageName, staffPackerName)

            if (descriptor == null) {
                logger.error("@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": unable to find staff packer class \"$staffPackerName\"", classDeclaration)
                return null
            }

            descriptor.nameDescriptor
        }

        val unpackerNameDescriptor = if (staffUnpackerName.isNullOrEmpty()) {
            NameDescriptor(currentPackageName, generatedSimpleName)
        } else {
            val descriptor = resolver.getClassDescriptorByName(currentPackageName, staffUnpackerName)

            if (descriptor == null) {
                logger.error("@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": unable to find staff unpacker class \"$staffUnpackerName\"", classDeclaration)
                return null
            }

            descriptor.nameDescriptor
        }

        val wares = classDeclaration.primaryConstructor
            ?.parameters
            ?.flatMap { parameter ->
                parameter.annotations
                    .filter { it.nameDescriptor == WARE_ANNOTATION_NAME_DESCRIPTOR }
                    .mapNotNull {
                        parseWare(resolver, classDescriptor, currentPackageName, parameter, it)
                    }
            }
            ?.sortedBy { it.index }

        if (!wares.isNullOrEmpty()) {
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
                        "@$WARE_ANNOTATION_NAME of \"${wares[i].fieldName}\": indexes must be unique",
                        wares[i].sourceSymbol,
                    )

                    return null
                }

                if (wares[i - 1].index + 1 != wares[i].index) {
                    logger.error(
                        "@$WARE_ANNOTATION_NAME of \"${wares[i].fieldName}\": indexes must be consecutive",
                        wares[i].sourceSymbol,
                    )

                    return null
                }
            }
        } else if (staffPackerName.isNullOrEmpty() || staffUnpackerName.isNullOrEmpty()) {
            logger.error(
                "@$STUFF_ANNOTATION_NAME of \"$classDescriptor\": no fields annotated with @$WARE_ANNOTATION_NAME found",
                classDeclaration,
            )

            return null
        }

        return BagDescriptor.Stuff(
            classDescriptor = classDescriptor,
            staffPackerDescriptor = staffPackerDescriptor,
            staffUnpackerDescriptor = unpackerNameDescriptor,
            wares = wares ?: emptyList(),
            generatedSimpleName = if (staffPackerName.isNullOrEmpty() || staffUnpackerName.isNullOrEmpty()) generatedSimpleName else null,
            shouldGeneratePacker = staffPackerName.isNullOrEmpty(),
            shouldGenerateUnpacker = staffUnpackerName.isNullOrEmpty(),
            sourceSymbol = classDeclaration,
            sourceFile = requireNotNull(classDeclaration.containingFile),
        )
    }

    private fun parseWare(
        resolver: Resolver,
        classDescriptor: DeclarationDescriptor,
        currentPackageName: String,
        parameterDeclaration: KSValueParameter,
        annotation: KSAnnotation,
    ): BagStuffWareDescriptor? {
        val fieldName = requireNotNull(parameterDeclaration.name).asString()

        val index = annotation.getArgumentValue<Int>(WARE_ARGUMENT_INDEX_NAME) ?: return null
        val version = annotation.getArgumentValue<Int>(WARE_ARGUMENT_VERSION_NAME) ?: WARE_ARGUMENT_VERSION_DEFAULT
        val fieldPackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD_PACKER_NAME)
        val fieldUnpackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_FIELD_UNPACKER_NAME)

        val fieldPackerDescriptor = if (fieldPackerName.isNullOrEmpty()) {
            null
        } else {
            val descriptor = resolver.getFunctionDescriptorByName(currentPackageName, fieldPackerName)

            if (descriptor == null) {
                logger.error(
                    "@BagStuff of \"$classDescriptor\", parameter \"$fieldName\": unable to find field packer function \"$fieldPackerName\"",
                    parameterDeclaration,
                )

                return null
            }

            descriptor
        }

        val fieldUnpackerDescriptor = if (fieldUnpackerName.isNullOrEmpty()) {
            null
        } else {
            val descriptor = resolver.getFunctionDescriptorByName(currentPackageName, fieldUnpackerName)

            if (descriptor == null) {
                logger.error(
                    "@BagStuff of \"$classDescriptor\", parameter \"$fieldName\": unable to find field unpacker function \"$fieldUnpackerName\"",
                    parameterDeclaration,
                )

                return null
            }

            descriptor
        }

        if (fieldName == "crate") {
            logger.warn(">>> $fieldName :: ${parameterDeclaration.type.resolve().annotations}")
        }

        return BagStuffWareDescriptor(
            fieldName = fieldName,
            index = index,
            version = version,
            typeDescriptor = parameterDeclaration.type.typeDescriptor,
            fieldPackerDescriptor = fieldPackerDescriptor,
            fieldUnpackerDescriptor = fieldUnpackerDescriptor,
            sourceSymbol = parameterDeclaration,
        )
    }

    private companion object {
        private val STUFF_ANNOTATION_NAME = requireNotNull(BagStuff::class.simpleName)
        private val STUFF_ANNOTATION_NAME_DESCRIPTOR = BagStuff::class.nameDescriptor

        private val WARE_ANNOTATION_NAME = requireNotNull(BagStuffWare::class.simpleName)
        private val WARE_ANNOTATION_NAME_DESCRIPTOR = BagStuffWare::class.nameDescriptor

        private const val STUFF_ARGUMENT_STAFF_PACKER_NAME = "staffPacker"
        private const val STUFF_ARGUMENT_STAFF_UNPACKER_NAME = "staffUnpacker"

        private const val STUFF_ARGUMENT_SUFFIX_NAME = "suffix"
        private const val STUFF_ARGUMENT_SUFFIX_DEFAULT = "Stuff" // defaultArguments is not working for JS

        private const val WARE_ARGUMENT_INDEX_NAME = "index"
        private const val WARE_ARGUMENT_FIELD_PACKER_NAME = "fieldPacker"
        private const val WARE_ARGUMENT_FIELD_UNPACKER_NAME = "fieldUnpacker"

        private const val WARE_ARGUMENT_VERSION_NAME = "version"
        private const val WARE_ARGUMENT_VERSION_DEFAULT = 1 // defaultArguments is not working for JS
    }
}
