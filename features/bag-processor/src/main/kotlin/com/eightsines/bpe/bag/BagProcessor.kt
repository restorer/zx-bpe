package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo

class BagProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BagProcessor(environment.logger, environment.codeGenerator)
}

class BagProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private val stuffDescriptors = mutableMapOf<String, BagStuffDescriptor>()
    private val enumDescriptors = mutableMapOf<String, BagEnumDescriptor>()
    private val generatedStuffs = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        stuffDescriptors.putAll(parseStuffDescriptors(resolver))
        enumDescriptors.putAll(parseEnumDescriptors(resolver))

        generateStuffs(stuffDescriptors.values)
        return emptyList()
    }

    private fun generateStuffs(descriptors: Collection<BagStuffDescriptor>) {
        for (descriptor in descriptors) {
            if (!generatedStuffs.contains(descriptor.classQualifiedName)) {
                generateSingleStuff(descriptor)
                generatedStuffs.add(descriptor.classQualifiedName)
            }
        }
    }

    private fun generateSingleStuff(descriptor: BagStuffDescriptor) {
        if (descriptor.customPackerQualifiedName != null && descriptor.customUnpackerQualifiedName != null) {
            return
        }

        val classNameType = ClassName(descriptor.packageName, descriptor.classSimpleName)

        val stuffObjectName = "${descriptor.classSimpleName}$GENERATED_STUFF_SUFFIX"
        val stuffObjectTypeSpec = TypeSpec.objectBuilder(stuffObjectName).addOriginatingKSFile(descriptor.containingFile)

        if (descriptor.customPackerQualifiedName == null) {
            stuffObjectTypeSpec.addSuperinterface(BagStuffPacker::class.asClassName().parameterizedBy(classNameType))

            stuffObjectTypeSpec.addProperty(
                PropertySpec.builder("putInTheBagVersion", Int::class.asClassName())
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%L", descriptor.wares.maxOfOrNull { it.version } ?: 0)
                    .build()
            )

            val funSpec = FunSpec.builder("putInTheBag")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bag", PackableBag::class.asClassName())
                .addParameter("value", classNameType)

            for (ware in descriptor.wares) {
                val parameterEnumDescriptor = enumDescriptors[ware.typeQualifiedName]
                val parameterStuffDescriptor = stuffDescriptors[ware.typeQualifiedName]

                when {
                    ware.customPackerQualifiedName != null ->
                        funSpec.addStatement("${ware.customPackerQualifiedName}(bag, value.%N)", ware.parameterName)

                    UNPACK_METHODS.contains(ware.typeQualifiedName) ->
                        funSpec.addStatement("bag.put(value.%N)", ware.parameterName)

                    parameterEnumDescriptor != null ->
                        funSpec.addStatement("bag.put(value.%N.%N)", ware.parameterName, parameterEnumDescriptor.valueName)

                    parameterStuffDescriptor != null -> if (parameterStuffDescriptor.customPackerQualifiedName != null) {
                        funSpec.addStatement("bag.put(%T, value.%N)", parameterStuffDescriptor.customPackerQualifiedName, ware.parameterName)
                    } else {
                        val generatedPackerQualifiedName = ClassName(
                            parameterStuffDescriptor.packageName,
                            "${parameterStuffDescriptor.classSimpleName}$GENERATED_STUFF_SUFFIX",
                        )

                        funSpec.addStatement("bag.put(%T, value.%N)", generatedPackerQualifiedName, ware.parameterName)
                    }

                    else -> {
                        logger.error(
                            "@BagStuff of \"${descriptor.classQualifiedName}\": unable to detect packer for \"${ware.parameterName}\" or type \"${ware.typeQualifiedName}\"",
                            descriptor.containingSymbol,
                        )

                        return
                    }
                }
            }

            stuffObjectTypeSpec.addFunction(funSpec.build())
        }

        if (descriptor.customUnpackerQualifiedName == null) {
            stuffObjectTypeSpec.addSuperinterface(BagStuffUnpacker::class.asClassName().parameterizedBy(classNameType))
        }

        FileSpec.builder(descriptor.packageName, stuffObjectName)
            .addType(stuffObjectTypeSpec.build())
            .build()
            .writeTo(codeGenerator, aggregating = true)
    }

    private fun parseStuffDescriptors(resolver: Resolver) = buildMap {
        resolver.walkAnnotations<KSClassDeclaration>(BAG_STUFF_ANNOTATION_QUALIFIED_NAME) { declaration, annotation ->
            declaration.qualifiedName?.asString()?.let { qualifiedName ->
                parseSingleStuffDescriptor(resolver, declaration, qualifiedName, annotation)?.let {
                    put(it.classQualifiedName, it)
                }
            }
        }
    }

    private fun parseSingleStuffDescriptor(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classQualifiedName: String,
        annotation: KSAnnotation,
    ): BagStuffDescriptor? {
        val currentPackageName = classDeclaration.packageName.asString()

        val packerName = annotation.getArgumentValue<String>(BAG_STUFF_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(BAG_STUFF_UNPACKER_NAME)

        val customPackerQualifiedName = if (packerName.isNullOrEmpty()) {
            null
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, packerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error("@BagStuff of \"$classQualifiedName\": unable to find packer-function \"$packerName\"", classDeclaration)
                return null
            }

            qualifiedName
        }

        val customUnpackerQualifiedName = if (unpackerName.isNullOrEmpty()) {
            null
        } else {
            val qualifiedName = resolver.getFunctionDeclarationByName(currentPackageName, unpackerName)
                ?.qualifiedName
                ?.asString()

            if (qualifiedName == null) {
                logger.error("@BagStuff of \"$classQualifiedName\": unable to find unpacker-function \"$unpackerName\"", classDeclaration)
                return null
            }

            qualifiedName
        }

        val wares = classDeclaration.primaryConstructor
            ?.parameters
            ?.flatMap { parameter ->
                parameter.annotations
                    .filter { it.annotationQualifiedName == BAG_STUFF_WARE_ANNOTATION_QUALIFIED_NAME }
                    .mapNotNull {
                        parseSingleStuffWareDescriptor(resolver, classQualifiedName, currentPackageName, parameter, it)
                    }
            }

        return BagStuffDescriptor(
            classQualifiedName = classQualifiedName,
            containingSymbol = classDeclaration,
            containingFile = requireNotNull(classDeclaration.containingFile),
            packageName = currentPackageName,
            classSimpleName = classDeclaration.simpleName.asString(),
            customPackerQualifiedName = customPackerQualifiedName,
            customUnpackerQualifiedName = customUnpackerQualifiedName,
            wares = wares?.sortedBy { it.index } ?: emptyList(),
        )
    }

    private fun parseSingleStuffWareDescriptor(
        resolver: Resolver,
        classQualifiedName: String,
        currentPackageName: String,
        parameterDeclaration: KSValueParameter,
        annotation: KSAnnotation,
    ): BagStuffWareDescriptor? {
        val parameterName = requireNotNull(parameterDeclaration.name).asString()

        val index = annotation.getArgumentValue<Int>(BAG_STUFF_WARE_INDEX_NAME) ?: return null
        val version = annotation.getArgumentValue<Int>(BAG_STUFF_WARE_VERSION_NAME) ?: BAG_STUFF_WARE_VERSION_DEFAULT
        val packerName = annotation.getArgumentValue<String>(BAG_STUFF_WARE_PACKER_NAME)
        val unpackerName = annotation.getArgumentValue<String>(BAG_STUFF_WARE_UNPACKER_NAME)

        val customPackerQualifiedName = if (packerName.isNullOrEmpty()) {
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

        val customUnpackerQualifiedName = if (unpackerName.isNullOrEmpty()) {
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

        val typeQualifiedName = parameterDeclaration.type.typeQualifiedName

        if (typeQualifiedName == null) {
            logger.error(
                "@BagStuff of \"$classQualifiedName\", parameter \"$parameterName\": unable resolve type",
                parameterDeclaration,
            )

            return null
        }

        return BagStuffWareDescriptor(
            parameterName = parameterName,
            index = index,
            version = version,
            typeQualifiedName = typeQualifiedName,
            customPackerQualifiedName = customPackerQualifiedName,
            customUnpackerQualifiedName = customUnpackerQualifiedName,
        )
    }

    private fun parseEnumDescriptors(resolver: Resolver) = buildMap {
        resolver.walkAnnotations<KSClassDeclaration>(BAG_ENUM_ANNOTATION_QUALIFIED_NAME) { declaration, annotation ->
            declaration.qualifiedName?.asString()?.let { qualifiedName ->
                parseSingleEnumDescriptor(resolver, declaration, qualifiedName, annotation)?.let {
                    put(it.classQualifiedName, it)
                }
            }
        }
    }

    private fun parseSingleEnumDescriptor(
        resolver: Resolver,
        classDeclaration: KSClassDeclaration,
        classQualifiedName: String,
        annotation: KSAnnotation,
    ): BagEnumDescriptor? {
        val valueName = annotation.getArgumentValue<String>(BAG_ENUM_VALUE_NAME) ?: return null
        val creatorName = annotation.getArgumentValue<String>(BAG_ENUM_CREATOR_NAME) ?: return null

        val valueTypeQualifiedName = classDeclaration.getAllProperties()
            .firstOrNull { it.simpleName.asString() == valueName }
            ?.type
            ?.typeQualifiedName

        if (valueTypeQualifiedName == null) {
            logger.error("@BagEnum of \"$classQualifiedName\": has no value-property \"$valueName\"", classDeclaration)
            return null
        }

        val creatorDeclaration = resolver.getFunctionDeclarationByName(classDeclaration.packageName.asString(), creatorName)
        val creatorQualifiedName = creatorDeclaration?.qualifiedName?.asString()

        if (creatorQualifiedName == null) {
            logger.error("@BagEnum of \"$classQualifiedName\": unable to find creator-function \"$creatorName\"", classDeclaration)
            return null
        }

        if (creatorDeclaration.parameters.size != 1) {
            logger.error(
                "@BagEnum of \"$classQualifiedName\": creator-function \"$creatorName\" must have exactly 1 parameter (but has ${creatorDeclaration.parameters.size})",
                classDeclaration,
            )

            return null
        }

        val creatorParameterTypeQualifiedName = creatorDeclaration.parameters[0].type.typeQualifiedName ?: return null

        if (valueTypeQualifiedName != creatorParameterTypeQualifiedName) {
            logger.error(
                "@BagEnum of \"$classQualifiedName\": value-property \"$valueName\" type \"$valueTypeQualifiedName\" is different from creator-function \"$creatorName\" parameter type \"$creatorParameterTypeQualifiedName\"",
                classDeclaration,
            )

            return null
        }

        return BagEnumDescriptor(
            classQualifiedName = classQualifiedName,
            valueName = valueName,
            creatorQualifiedName = creatorQualifiedName,
            typeQualifiedName = valueTypeQualifiedName,
        )
    }

    private companion object {
        private val BAG_STUFF_ANNOTATION_QUALIFIED_NAME = requireNotNull(BagStuff::class.qualifiedName)
        private val BAG_STUFF_WARE_ANNOTATION_QUALIFIED_NAME = requireNotNull(BagStuffWare::class.qualifiedName)
        private val BAG_ENUM_ANNOTATION_QUALIFIED_NAME = requireNotNull(BagEnum::class.qualifiedName)

        private const val BAG_STUFF_PACKER_NAME = "packer"
        private const val BAG_STUFF_UNPACKER_NAME = "unpacker"

        private const val BAG_STUFF_WARE_INDEX_NAME = "index"
        private const val BAG_STUFF_WARE_PACKER_NAME = "packer"
        private const val BAG_STUFF_WARE_UNPACKER_NAME = "unpacker"
        private const val BAG_STUFF_WARE_VERSION_NAME = "version"
        private const val BAG_STUFF_WARE_VERSION_DEFAULT = 1 // defaultArguments is not working for JS

        private const val BAG_ENUM_VALUE_NAME = "value"
        private const val BAG_ENUM_CREATOR_NAME = "creator"

        private const val GENERATED_STUFF_SUFFIX = "Stuff"

        private val UNPACK_METHODS = buildMap {
            put("kotlin.Boolean", "getBoolean")
            put("kotlin.Boolean?", "getBooleanOrNull")

            put("kotlin.Int", "getInt")
            put("kotlin.Int?", "getIntOrNull")

            put("kotlin.String", "getString")
            put("kotlin.String?", "getStringOrNull")
        }
    }
}

data class BagStuffDescriptor(
    val classQualifiedName: String,
    val containingSymbol: KSNode,
    val containingFile: KSFile,
    val packageName: String,
    val classSimpleName: String,
    val customPackerQualifiedName: String?,
    val customUnpackerQualifiedName: String?,
    val wares: List<BagStuffWareDescriptor>,
)

data class BagStuffWareDescriptor(
    val parameterName: String,
    val index: Int,
    val version: Int,
    val typeQualifiedName: String,
    val customPackerQualifiedName: String?,
    val customUnpackerQualifiedName: String?,
)

data class BagEnumDescriptor(
    val classQualifiedName: String,
    val valueName: String,
    val creatorQualifiedName: String,
    val typeQualifiedName: String,
)

/*

val unpackMethod = UNPACK_METHODS[valueQualifiedType]

if (unpackMethod == null) {
    logger.error(
        "@BagEnum of \"$classQualifiedName\": unsupported unpacker for value-property \"$valueName\" of type \"$valueQualifiedType\"",
        declaration,
    )

    continue
}

*/
