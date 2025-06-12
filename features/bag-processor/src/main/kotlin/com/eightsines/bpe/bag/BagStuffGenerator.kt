package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo

class BagStuffGenerator(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) {
    interface Resolver {
        fun resolveName(nameReference: NameDescriptorReference): NameDescriptor?
        fun resolveType(typeDescriptor: TypeDescriptor): BagDescriptor?
        fun resolvePolymorphicCases(nameDescriptor: NameDescriptor): List<BagDescriptor.Stuff>
    }

    fun generate(descriptor: BagDescriptor.Stuff, resolver: Resolver): Boolean {
        val generateInfo = descriptor.generateInfo ?: return true

        if (descriptor.sourceFile == null) {
            logger.error(
                "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": not generating Stuff, because class is from dependency module",
                descriptor.sourceSymbol,
            )

            return false
        }

        val polymorphicCases = if (generateInfo.isPolymorphic) {
            val cases = resolver.resolvePolymorphicCases(descriptor.classDescriptor.nameDescriptor)
                .map { requireNotNull(it.polymorphicCase) to it }
                .sortedBy { it.first.id }

            if (cases.isEmpty()) {
                logger.error(
                    "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": polymorphic cases must be specified",
                    descriptor.sourceSymbol,
                )

                return false
            }

            if (cases[0].first.id != 1) {
                logger.error(
                    "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": polymorphic cases IDs must starts with 1",
                    descriptor.sourceSymbol,
                )

                return false
            }

            for (i in 1..<cases.size) {
                if (cases[i - 1].first.id == cases[i].first.id) {
                    logger.error(
                        "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": polymorphic cases IDs must be unique (\"${cases[i - 1].second.classDescriptor}\" and \"${cases[i].second.classDescriptor}\")",
                        cases[i].second.sourceSymbol,
                    )

                    return false
                }

                if (cases[i - 1].first.id + 1 != cases[i].first.id) {
                    logger.error(
                        "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": polymorphic cases IDs must be consecutive (\"${cases[i - 1].second.classDescriptor}\" and \"${cases[i].second.classDescriptor}\")",
                        cases[i].second.sourceSymbol,
                    )

                    return false
                }
            }

            cases
        } else {
            emptyList()
        }

        if (!generateInfo.isPolymorphic && descriptor.wares.isEmpty()) {
            logger.error(
                "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": no fields annotated with @${BagStuffParser.WARE_ANNOTATION_NAME} found",
                descriptor.sourceSymbol,
            )

            return false
        }

        val stuffBuilder = TypeSpec.objectBuilder(generateInfo.nameDescriptor.simpleName)
            .addOriginatingKSFile(descriptor.sourceFile)

        if (generateInfo.shouldGeneratePacker && !generatePacker(descriptor, polymorphicCases, stuffBuilder, resolver)) {
            return false
        }

        if (generateInfo.shouldGenerateUnpacker && !generateUnpacker(descriptor, polymorphicCases, stuffBuilder, resolver)) {
            return false
        }

        FileSpec.builder(generateInfo.nameDescriptor.packageName, generateInfo.nameDescriptor.simpleName)
            .addType(stuffBuilder.build())
            .build()
            .writeTo(codeGenerator, aggregating = true)

        return true
    }

    private fun generatePacker(
        descriptor: BagDescriptor.Stuff,
        polymorphicCases: List<Pair<BagStuffPolymorphicCase, BagDescriptor.Stuff>>,
        stuffBuilder: TypeSpec.Builder,
        resolver: Resolver,
    ): Boolean {
        val classTypeName = descriptor.classDescriptor.pouetTypeName
        stuffBuilder.addSuperinterface(BagStuffPacker::class.asClassName().parameterizedBy(classTypeName))

        stuffBuilder.addProperty(
            PropertySpec.builder("putInTheBagVersion", Int::class.asClassName())
                .addModifiers(KModifier.OVERRIDE)
                .initializer("%L", descriptor.wares.maxOfOrNull { it.version } ?: 1)
                .build()
        )

        val funBuilder = FunSpec.builder("putInTheBag")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("bag", PackableBag::class.asClassName())
            .addParameter("value", classTypeName)

        var isPolymorphicPackerNullable = false

        if (polymorphicCases.isNotEmpty()) {
            funBuilder.addCode(
                buildCodeBlock {
                    add("val polymorphicPacker = when (`value`) {\n")
                    indent()

                    for ((case, caseStuff) in polymorphicCases) {
                        add("is %T -> {\n", caseStuff.classDescriptor.pouetTypeName)
                        indent()

                        addStatement("bag.put(%L)", case.id)

                        if (caseStuff.packerReference != null) {
                            val packerDescriptor = resolver.resolveName(caseStuff.packerReference)

                            if (packerDescriptor == null) {
                                logger.error(
                                    "Unable to resolve packer for polymorphic case \"${caseStuff.classDescriptor}\" of base \"${descriptor.classDescriptor}\"",
                                    descriptor.sourceSymbol,
                                )

                                return false
                            }

                            addStatement("%T", packerDescriptor.pouetClassName)
                        } else {
                            isPolymorphicPackerNullable = true
                            addStatement("null")
                        }

                        unindent()
                        add("}\n")
                    }

                    if (!descriptor.isSealed) {
                        addStatement("else -> throw IllegalStateException(\"Unknown polymorphic case: \$value\")")
                    }

                    unindent()
                    add("}\n")
                }
            )
        }

        for (ware in descriptor.wares) {
            if (!generateWarePacker(funBuilder, ware, resolver)) {
                return false
            }
        }

        if (polymorphicCases.isNotEmpty()) {
            if (isPolymorphicPackerNullable) {
                funBuilder.addCode(
                    buildCodeBlock {
                        add("if (polymorphicPacker != null) {\n")
                        indent()
                        addStatement("bag.put(polymorphicPacker, value)")
                        unindent()
                        add("}\n")
                    }
                )
            } else {
                funBuilder.addStatement("bag.put(polymorphicPacker, value)")
            }
        }

        stuffBuilder.addFunction(funBuilder.build())
        return true
    }

    private fun generateWarePacker(
        funBuilder: FunSpec.Builder,
        ware: BagStuffWareDescriptor,
        resolver: Resolver,
    ): Boolean {
        if (ware.packerDescriptor != null) {
            funBuilder.addCode(
                buildCodeBlock {
                    add("%M(\n", ware.packerDescriptor.pouetMemberName)
                    indent()

                    for (parameter in ware.packerDescriptor.parameters) {
                        if (parameter.name == "bag" || parameter.name == "value") {
                            addStatement("%N = %N,", parameter.name, parameter.name)
                        } else {
                            addStatement("%N = `value`.%N,", parameter.name, parameter.name)
                        }
                    }

                    unindent()
                    add(")\n")
                }
            )

            return true
        }

        val descriptor = resolver.resolveType(ware.typeDescriptor)

        if (descriptor == null) {
            logger.error(
                "Unable to resolve packer for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"",
                ware.sourceSymbol,
            )

            return false
        }

        when (descriptor) {
            is BagDescriptor.Primitive -> funBuilder.addStatement("bag.put(`value`.%N)", ware.fieldName)
            is BagDescriptor.Singlefield -> funBuilder.addStatement("bag.put(`value`.%N.%N)", ware.fieldName, descriptor.fieldName)

            is BagDescriptor.Stuff -> if (descriptor.packerReference == null) {
                logger.error(
                    "Packer is missing for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"",
                    ware.sourceSymbol
                )
                return false
            } else {
                val pouetClassName = resolver.resolveName(descriptor.packerReference)?.pouetClassName

                if (pouetClassName == null) {
                    logger.error(
                        "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": unable to find stuff-packer class \"${descriptor.packerReference}\"",
                        descriptor.sourceSymbol,
                    )

                    return false
                }

                funBuilder.addStatement("bag.put(%T, `value`.%N)", pouetClassName, ware.fieldName)
            }
        }

        return true
    }

    private fun generateUnpacker(
        descriptor: BagDescriptor.Stuff,
        polymorphicCases: List<Pair<BagStuffPolymorphicCase, BagDescriptor.Stuff>>,
        stuffBuilder: TypeSpec.Builder,
        resolver: Resolver,
    ): Boolean {
        if (descriptor.wares.isNotEmpty() && polymorphicCases.isNotEmpty()) {
            logger.error(
                "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${descriptor.classDescriptor}\": generation of unpacker with both wares and polymorphic cases is not supported",
                descriptor.sourceSymbol,
            )

            return false
        }

        val classTypeName = descriptor.classDescriptor.pouetTypeName
        stuffBuilder.addSuperinterface(BagStuffUnpacker::class.asClassName().parameterizedBy(classTypeName))

        val funBuilder = FunSpec.builder("getOutOfTheBag")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("version", Int::class.asClassName())
            .addParameter("bag", UnpackableBag::class.asClassName())
            .returns(classTypeName)

        funBuilder.addStatement(
            "%M(%S, %L, version)",
            MemberName("com.eightsines.bpe.bag", "requireSupportedStuffVersion"),
            descriptor.classDescriptor.nameDescriptor.simpleName,
            descriptor.wares.maxOfOrNull { it.version } ?: 1,
        )

        if (polymorphicCases.isNotEmpty()) {
            funBuilder.addStatement("val polymorphicId = bag.getInt()")
        }

        for (ware in descriptor.wares) {
            if (!generateWareUnpacker(funBuilder, ware, resolver)) {
                return false
            }
        }

        if (polymorphicCases.isEmpty()) {
            funBuilder.addCode(
                buildCodeBlock {
                    add("return %M(\n", descriptor.classDescriptor.nameDescriptor.pouetMemberName)
                    indent()

                    for (ware in descriptor.wares) {
                        addStatement("%N = %N,", ware.fieldName, ware.fieldName)
                    }

                    unindent()
                    add(")\n")
                }
            )
        } else {
            funBuilder.addCode(
                buildCodeBlock {
                    add("return when (polymorphicId) {\n")
                    indent()

                    for ((case, caseStuff) in polymorphicCases) {
                        if (caseStuff.unpackerReference != null) {
                            val unpackerDescriptor = resolver.resolveName(caseStuff.unpackerReference)

                            if (unpackerDescriptor == null) {
                                logger.error(
                                    "Unable to resolve unpacker for polymorphic case \"${caseStuff.classDescriptor}\" of base \"${descriptor.classDescriptor}\"",
                                    descriptor.sourceSymbol,
                                )

                                return false
                            }

                            addStatement("%L -> bag.getStuff(%T)", case.id, unpackerDescriptor.pouetClassName)
                        } else {
                            addStatement("%L -> %T", case.id, caseStuff.classDescriptor.asRawTypeDescriptor().pouetTypeName)
                        }
                    }

                    addStatement(
                        "else -> throw %T(%S, polymorphicId)",
                        ClassName("com.eightsines.bpe.bag", "UnknownPolymorphicTypeBagUnpackException"),
                        descriptor.classDescriptor.nameDescriptor.simpleName,
                    )

                    unindent()
                    add("}\n")
                }
            )
        }

        stuffBuilder.addFunction(funBuilder.build())
        return true
    }

    private fun generateWareUnpacker(
        funBuilder: FunSpec.Builder,
        ware: BagStuffWareDescriptor,
        resolver: Resolver,
    ): Boolean {
        if (ware.unpackerDescriptor != null) {
            funBuilder.addCode(
                buildCodeBlock {
                    add("val %N = %M(\n", ware.fieldName, ware.unpackerDescriptor.pouetMemberName)
                    indent()

                    for (parameter in ware.unpackerDescriptor.parameters) {
                        addStatement("%N = %N,", parameter.name, parameter.name)
                    }

                    unindent()
                    add(")\n")
                }
            )

            return true
        }

        val wareDescriptor = resolver.resolveType(ware.typeDescriptor)

        if (wareDescriptor == null) {
            logger.error(
                "Unable to resolve unpacker for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"",
                ware.sourceSymbol,
            )

            return false
        }

        when (wareDescriptor) {
            is BagDescriptor.Primitive -> funBuilder.addStatement("val %N = bag.%N()", ware.fieldName, wareDescriptor.unpackMethod)

            is BagDescriptor.Singlefield -> if (wareDescriptor.shouldCheckCreatorException) {
                funBuilder.addStatement(
                    "val %N = %M { %M(bag.%N()) }",
                    ware.fieldName,
                    MemberName("com.eightsines.bpe.bag", "requireNoIllegalArgumentException"),
                    wareDescriptor.creatorDescriptor.pouetMemberName,
                    wareDescriptor.primitiveDescriptor.unpackMethod
                )
            } else {
                funBuilder.addStatement(
                    "val %N = %M(bag.%N())",
                    ware.fieldName,
                    wareDescriptor.creatorDescriptor.pouetMemberName,
                    wareDescriptor.primitiveDescriptor.unpackMethod
                )
            }

            is BagDescriptor.Stuff -> if (wareDescriptor.unpackerReference == null) {
                logger.error(
                    "Unpacker is missing for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"",
                    ware.sourceSymbol
                )
                return false
            } else {
                val pouetClassName = resolver.resolveName(wareDescriptor.unpackerReference)?.pouetClassName

                if (pouetClassName == null) {
                    logger.error(
                        "@${BagStuffParser.STUFF_ANNOTATION_NAME} of \"${wareDescriptor.classDescriptor}\": unable to find stuff-unpacker class \"${wareDescriptor.unpackerReference}\"",
                        wareDescriptor.sourceSymbol,
                    )

                    return false
                }

                funBuilder.addStatement(
                    "val %N: %T = bag.getStuff(%T)",
                    ware.fieldName,
                    ware.typeDescriptor.pouetTypeName,
                    pouetClassName,
                )
            }
        }

        return true
    }
}
