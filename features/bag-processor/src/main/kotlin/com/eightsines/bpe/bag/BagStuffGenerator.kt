package com.eightsines.bpe.bag

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo

class BagStuffGenerator(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) {
    fun generate(descriptor: BagDescriptor.Stuff, descriptorResolver: (String) -> BagDescriptor?): Boolean {
        if ((!descriptor.shouldGeneratePacker && !descriptor.shouldGenerateUnpacker) || descriptor.generatedSimpleName == null) {
            return true
        }

        val classTypeName: TypeName = if (descriptor.numClassTypeParameters > 0) {
            ClassName(descriptor.classPackageName, descriptor.classSimpleName)
                .parameterizedBy(List(descriptor.numClassTypeParameters) { STAR })
        } else {
            ClassName(descriptor.classPackageName, descriptor.classSimpleName)
        }

        val stuffBuilder = TypeSpec.objectBuilder(descriptor.generatedSimpleName).addOriginatingKSFile(descriptor.sourceFile)

        if (descriptor.shouldGeneratePacker && !generatePacker(descriptor, classTypeName, stuffBuilder, descriptorResolver)) {
            return false
        }

        if (descriptor.shouldGenerateUnpacker && !generateUnpacker(descriptor, classTypeName, stuffBuilder, descriptorResolver)) {
            return false
        }

        FileSpec.builder(descriptor.classPackageName, descriptor.generatedSimpleName)
            .addType(stuffBuilder.build())
            .build()
            .writeTo(codeGenerator, aggregating = true)

        return true
    }

    private fun generatePacker(
        descriptor: BagDescriptor.Stuff,
        classTypeName: TypeName,
        stuffBuilder: TypeSpec.Builder,
        descriptorResolver: (String) -> BagDescriptor?,
    ): Boolean {
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
        descriptorResolver: (String) -> BagDescriptor?,
    ): Boolean {
        if (ware.packerQualifiedName != null) {
            // TODO: докидывать параметры по названиям в функции-пакере
            funBuilder.addStatement("${ware.packerQualifiedName}(bag, value.%N)", ware.parameterName)
            return true
        }

        val wareDescriptor = descriptorResolver(ware.typeQualifiedName)

        if (wareDescriptor == null) {
            logger.error("Unable to resolve packer for field \"${ware.parameterName}\" of type \"${ware.typeQualifiedName}\"", ware.sourceSymbol)
            return false
        }

        when (wareDescriptor) {
            is BagDescriptor.Primitive -> funBuilder.addStatement("bag.put(value.%N)", ware.parameterName)
            is BagDescriptor.Singlefield -> funBuilder.addStatement("bag.put(value.%N.%N)", ware.parameterName, wareDescriptor.fieldName)

            is BagDescriptor.Stuff -> funBuilder.addStatement(
                "bag.put(%T, value.%N)",
                ClassName.bestGuess(wareDescriptor.packerQualifiedName),
                ware.parameterName,
            )
        }

        return true
    }

    private fun generateUnpacker(
        descriptor: BagDescriptor.Stuff,
        classTypeName: TypeName,
        stuffBuilder: TypeSpec.Builder,
        descriptorResolver: (String) -> BagDescriptor?,
    ): Boolean {
        stuffBuilder.addSuperinterface(BagStuffUnpacker::class.asClassName().parameterizedBy(classTypeName))

        val funBuilder = FunSpec.builder("getOutOfTheBag")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("version", Int::class.asClassName())
            .addParameter("bag", UnpackableBag::class.asClassName())
            .returns(classTypeName)

        funBuilder.addStatement(
            "com.eightsines.bpe.bag.requireSupportedStuffVersion(%S, %L, version)",
            descriptor.classSimpleName,
            descriptor.wares.maxOf { it.version },
        )

        for (ware in descriptor.wares) {
            if (!generateWareUnpacker(funBuilder, ware, descriptorResolver)) {
                return false
            }
        }

        funBuilder.addCode(
            buildCodeBlock {
                add("return %L(\n", (classTypeName as? ParameterizedTypeName)?.rawType ?: classTypeName)
                indent()

                for (ware in descriptor.wares) {
                    addStatement("%N = %N,", ware.parameterName, ware.parameterName)
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
        descriptorResolver: (String) -> BagDescriptor?,
    ): Boolean {
        if (ware.unpackerQualifiedName != null) {
            // TODO: докидывать параметры по названиям в функции-анпакере
            funBuilder.addStatement("val %N = ${ware.unpackerQualifiedName}(bag)", ware.parameterName)
            return true
        }

        val wareDescriptor = descriptorResolver(ware.typeQualifiedName)

        if (wareDescriptor == null) {
            logger.error("Unable to resolve unpacker for field \"${ware.parameterName}\" of type \"${ware.typeQualifiedName}\"", ware.sourceSymbol)
            return false
        }

        when (wareDescriptor) {
            is BagDescriptor.Primitive -> funBuilder.addStatement("val %N = bag.%N()", ware.parameterName, wareDescriptor.unpackMethod)

            is BagDescriptor.Singlefield -> if (wareDescriptor.shouldCheckCreatorException) {
                funBuilder.addStatement(
                    "val %N = com.eightsines.bpe.bag.requireNoIllegalArgumentException { %L(bag.%N()) }",
                    ware.parameterName,
                    wareDescriptor.creatorQualifiedName,
                    wareDescriptor.bagPrimitiveDescriptor.unpackMethod
                )
            } else {
                funBuilder.addStatement(
                    "val %N = %L(bag.%N())",
                    ware.parameterName,
                    wareDescriptor.creatorQualifiedName,
                    wareDescriptor.bagPrimitiveDescriptor.unpackMethod
                )
            }

            is BagDescriptor.Stuff -> funBuilder.addStatement(
                "val %N = bag.getStuff(%T)",
                ware.parameterName,
                ClassName.bestGuess(wareDescriptor.unpackerQualifiedName),
            )
        }

        return true
    }
}
