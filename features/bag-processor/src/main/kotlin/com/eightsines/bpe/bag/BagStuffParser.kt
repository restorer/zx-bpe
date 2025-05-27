package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

class BagStuffParser(private val logger: KSPLogger) {
    fun parseAll(resolver: Resolver): List<BagDescriptor.Stuff> = buildList {
        for (declaration in resolver.getSymbolsWithAnnotation(STUFF_ANNOTATION_QUALIFIED_NAME).filterIsInstance<KSClassDeclaration>()) {
            parse(resolver, declaration)?.let { add(it) }
        }
    }

    private fun parse(resolver: Resolver, classDeclaration: KSClassDeclaration): BagDescriptor.Stuff? {
        val classQualifiedName = classDeclaration.qualifiedName?.asString() ?: return null
        val annotation = classDeclaration.annotations.firstOrNull { it.annotationQualifiedName == STUFF_ANNOTATION_QUALIFIED_NAME }

        return if (annotation != null) {
            parseStuff(resolver, classDeclaration, classQualifiedName, annotation)
        } else {
            null
        }
    }

    private fun parseStuff(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classQualifiedName: String,
        annotation: KSAnnotation,
    ): BagDescriptor.Stuff? {
        val currentPackageName = classDeclaration.packageName.asString()

        val packerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(STUFF_ARGUMENT_UNPACKER_NAME)

        val generatedSimpleName = if (packerName.isNullOrEmpty() || unpackerName.isNullOrEmpty()) {
            classDeclaration.simpleName.asString() + GENERATED_STUFF_SUFFIX
        } else {
            null
        }

        val packerQualifiedName = if (packerName.isNullOrEmpty()) {
            "$currentPackageName.$generatedSimpleName"
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, packerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error("@$STUFF_ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": unable to find packer-function \"$packerName\"", classDeclaration)
                return null
            }

            qualifiedName
        }

        val unpackerQualifiedName = if (unpackerName.isNullOrEmpty()) {
            "$currentPackageName.$generatedSimpleName"
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, unpackerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error("@$STUFF_ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": unable to find unpacker-function \"$unpackerName\"", classDeclaration)
                return null
            }

            qualifiedName
        }

        val wares = classDeclaration.primaryConstructor
            ?.parameters
            ?.flatMap { parameter ->
                parameter.annotations
                    .filter { it.annotationQualifiedName == WARE_ANNOTATION_QUALIFIED_NAME }
                    .mapNotNull {
                        parseWare(resolver, classQualifiedName, currentPackageName, parameter, it)
                    }
            }
            ?.sortedBy { it.index }

        if (wares.isNullOrEmpty()) {
            logger.error(
                "@$STUFF_ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": no fields annotated with @$WARE_ANNOTATION_SIMPLE_NAME found",
                classDeclaration,
            )

            return null
        }

        if (wares[0].index != 1) {
            logger.error(
                "@$STUFF_ANNOTATION_SIMPLE_NAME of \"$classQualifiedName\": indexes of @$WARE_ANNOTATION_SIMPLE_NAME must starts with 1",
                classDeclaration,
            )

            return null
        }

        for (i in 1..<wares.size) {
            if (wares[i - 1].index == wares[i].index) {
                logger.error(
                    "@$WARE_ANNOTATION_SIMPLE_NAME of \"${wares[i].parameterName}\": indexes must be unique",
                    wares[i].sourceSymbol,
                )

                return null
            }

            if (wares[i - 1].index + 1 != wares[i].index) {
                logger.error(
                    "@$WARE_ANNOTATION_SIMPLE_NAME of \"${wares[i].parameterName}\": indexes must be consecutive",
                    wares[i].sourceSymbol,
                )

                return null
            }
        }

        return BagDescriptor.Stuff(
            classQualifiedName = classQualifiedName,
            classPackageName = currentPackageName,
            classSimpleName = classDeclaration.simpleName.asString(),
            numClassTypeParameters = classDeclaration.typeParameters.size,
            sourceSymbol = classDeclaration,
            sourceFile = requireNotNull(classDeclaration.containingFile),
            packerQualifiedName = packerQualifiedName,
            unpackerQualifiedName = unpackerQualifiedName,
            wares = wares,
            generatedSimpleName = generatedSimpleName,
            shouldGeneratePacker = packerName.isNullOrEmpty(),
            shouldGenerateUnpacker = unpackerName.isNullOrEmpty(),
        )
    }

    private fun parseWare(
        resolver: Resolver,
        classQualifiedName: String,
        currentPackageName: String,
        parameterDeclaration: KSValueParameter,
        annotation: KSAnnotation,
    ): BagStuffWareDescriptor? {
        val parameterName = requireNotNull(parameterDeclaration.name).asString()

        val index = annotation.getArgumentValue<Int>(WARE_ARGUMENT_INDEX_NAME) ?: return null
        val version = annotation.getArgumentValue<Int>(WARE_ARGUMENT_VERSION_NAME) ?: WARE_ARGUMENT_VERSION_DEFAULT
        val packerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(WARE_ARGUMENT_UNPACKER_NAME)

        val packerQualifiedName = if (packerName.isNullOrEmpty()) {
            null
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, packerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error(
                    "@BagStuff of \"$classQualifiedName\", parameter \"$parameterName\": unable to find packer-function \"$packerName\"",
                    parameterDeclaration,
                )

                return null
            }

            qualifiedName
        }

        val unpackerQualifiedName = if (unpackerName.isNullOrEmpty()) {
            null
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, unpackerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error(
                    "@BagStuff of \"$classQualifiedName\", parameter \"$parameterName\": unable to find unpacker-function \"$unpackerName\"",
                    parameterDeclaration,
                )

                return null
            }

            qualifiedName
        }

        val parameterType = parameterDeclaration.type.resolve()
        val typeQualifiedName = parameterType.typeQualifiedName

        if (typeQualifiedName == null) {
            logger.error(
                "@BagStuff of \"$classQualifiedName\", parameter \"$parameterName\": unable resolve type",
                parameterDeclaration,
            )

            return null
        }

        return BagStuffWareDescriptor(
            parameterName = parameterName,
            sourceSymbol = parameterDeclaration,
            index = index,
            version = version,
            typeQualifiedName = typeQualifiedName,
            packerQualifiedName = packerQualifiedName,
            unpackerQualifiedName = unpackerQualifiedName,
        )
    }

    private companion object {
        private val STUFF_ANNOTATION_SIMPLE_NAME = requireNotNull(BagStuff::class.simpleName)
        private val STUFF_ANNOTATION_QUALIFIED_NAME = requireNotNull(BagStuff::class.qualifiedName)

        private val WARE_ANNOTATION_SIMPLE_NAME = requireNotNull(BagStuffWare::class.simpleName)
        private val WARE_ANNOTATION_QUALIFIED_NAME = requireNotNull(BagStuffWare::class.qualifiedName)

        private const val STUFF_ARGUMENT_PACKER_NAME = "packer"
        private const val STUFF_ARGUMENT_UNPACKER_NAME = "unpacker"

        private const val WARE_ARGUMENT_INDEX_NAME = "index"
        private const val WARE_ARGUMENT_VERSION_NAME = "version"
        private const val WARE_ARGUMENT_VERSION_DEFAULT = 1 // defaultArguments is not working for JS
        private const val WARE_ARGUMENT_PACKER_NAME = "packer"
        private const val WARE_ARGUMENT_UNPACKER_NAME = "unpacker"

        private const val GENERATED_STUFF_SUFFIX = "Stuff"
    }
}
