package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
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
    fun generate(descriptor: BagDescriptor.Stuff, descriptorResolver: (TypeDescriptor) -> BagDescriptor?): Boolean {
        if ((!descriptor.shouldGeneratePacker && !descriptor.shouldGenerateUnpacker) || descriptor.generatedSimpleName == null) {
            return true
        }

        val stuffBuilder = TypeSpec.objectBuilder(descriptor.generatedSimpleName).addOriginatingKSFile(descriptor.sourceFile)

        if (descriptor.shouldGeneratePacker &&
            !generatePacker(descriptor, descriptor.classDescriptor, stuffBuilder, descriptorResolver)
        ) {
            return false
        }

        if (descriptor.shouldGenerateUnpacker &&
            !generateUnpacker(descriptor, descriptor.classDescriptor, stuffBuilder, descriptorResolver)
        ) {
            return false
        }

        FileSpec.builder(descriptor.classDescriptor.nameDescriptor.packageName, descriptor.generatedSimpleName)
            .addType(stuffBuilder.build())
            .build()
            .writeTo(codeGenerator, aggregating = true)

        return true
    }

    private fun generatePacker(
        descriptor: BagDescriptor.Stuff,
        classDescriptor: DeclarationDescriptor,
        stuffBuilder: TypeSpec.Builder,
        descriptorResolver: (TypeDescriptor) -> BagDescriptor?,
    ): Boolean {
        val classTypeName = classDescriptor.pouetTypeName
        stuffBuilder.addSuperinterface(BagStuffPacker::class.asClassName().parameterizedBy(classTypeName))

        stuffBuilder.addProperty(
            PropertySpec.builder("putInTheBagVersion", Int::class.asClassName())
                .addModifiers(KModifier.OVERRIDE)
                .initializer("%L", descriptor.wares.maxOf { it.version })
                .build()
        )

        val funBuilder = FunSpec.builder("putInTheBag")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("bag", PackableBag::class.asClassName())
            .addParameter("value", classTypeName)

        for (ware in descriptor.wares) {
            if (!generateWarePacker(funBuilder, ware, descriptorResolver)) {
                return false
            }
        }

        stuffBuilder.addFunction(funBuilder.build())
        return true
    }

    private fun generateWarePacker(
        funBuilder: FunSpec.Builder,
        ware: BagStuffWareDescriptor,
        descriptorResolver: (TypeDescriptor) -> BagDescriptor?,
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

        val descriptor = descriptorResolver(ware.typeDescriptor)

        if (descriptor == null) {
            logger.error("Unable to resolve packer for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"", ware.sourceSymbol)
            return false
        }

        when (descriptor) {
            is BagDescriptor.Primitive -> funBuilder.addStatement("bag.put(`value`.%N)", ware.fieldName)
            is BagDescriptor.Singlefield -> funBuilder.addStatement("bag.put(`value`.%N.%N)", ware.fieldName, descriptor.fieldName)

            is BagDescriptor.Stuff -> if (descriptor.packerDescriptor == null) {
                logger.error("Packer is missing for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"", ware.sourceSymbol)
                return false
            } else {
                funBuilder.addStatement(
                    "bag.put(%T, `value`.%N)",
                    descriptor.packerDescriptor.pouetClassName,
                    ware.fieldName,
                )
            }
        }

        return true
    }

    private fun generateUnpacker(
        descriptor: BagDescriptor.Stuff,
        classDescriptor: DeclarationDescriptor,
        stuffBuilder: TypeSpec.Builder,
        descriptorResolver: (TypeDescriptor) -> BagDescriptor?,
    ): Boolean {
        val classTypeName = classDescriptor.pouetTypeName
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
            descriptor.wares.maxOf { it.version },
        )

        for (ware in descriptor.wares) {
            if (!generateWareUnpacker(funBuilder, ware, descriptorResolver)) {
                return false
            }
        }

        funBuilder.addCode(
            buildCodeBlock {
                add("return %M(\n", classDescriptor.nameDescriptor.pouetMemberName)
                indent()

                for (ware in descriptor.wares) {
                    addStatement("%N = %N,", ware.fieldName, ware.fieldName)
                }

                unindent()
                add(")\n")
            }
        )

        stuffBuilder.addFunction(funBuilder.build())
        return true
    }

    private fun generateWareUnpacker(
        funBuilder: FunSpec.Builder,
        ware: BagStuffWareDescriptor,
        descriptorResolver: (TypeDescriptor) -> BagDescriptor?,
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

        val wareDescriptor = descriptorResolver(ware.typeDescriptor)

        if (wareDescriptor == null) {
            logger.error("Unable to resolve unpacker for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"", ware.sourceSymbol)
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

            is BagDescriptor.Stuff -> if (wareDescriptor.unpackerDescriptor == null) {
                logger.error("Unpacker is missing for field \"${ware.sourceClassDescriptor}::${ware.fieldName}\" of type \"${ware.typeDescriptor}\"", ware.sourceSymbol)
                return false
            } else {
                funBuilder.addStatement(
                    "val %N: %T = bag.getStuff(%T)",
                    ware.fieldName,
                    ware.typeDescriptor.pouetTypeName,
                    wareDescriptor.unpackerDescriptor.pouetClassName,
                )
            }
        }

        return true
    }
}
