package com.eightsines.bpe.bag

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class BagProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BagProcessor(environment.logger, environment.codeGenerator)
}

class BagProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private val stuffParser = BagStuffParser(logger)
    private val stuffGenerator = BagStuffGenerator(logger, codeGenerator)
    private val singlefieldParser = BagSinglefieldParser(logger)

    private val allDescriptors = mutableMapOf<String, BagDescriptor>()
    private val stuffDescriptors = mutableSetOf<BagDescriptor.Stuff>()
    private val alreadyGeneratedStuffs = mutableSetOf<BagDescriptor.Stuff>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        for (descriptor in stuffParser.parseAll(resolver)) {
            allDescriptors[descriptor.classQualifiedName] = descriptor
            stuffDescriptors.add(descriptor)
        }

        for (descriptor in stuffDescriptors) {
            if (alreadyGeneratedStuffs.contains(descriptor)) {
                continue
            }

            if (stuffGenerator.generate(descriptor) { resolveDescriptor(resolver, it) }) {
                alreadyGeneratedStuffs.add(descriptor)
            }
        }

        return emptyList()
    }

    private fun resolveDescriptor(resolver: Resolver, typeQualifiedName: String): BagDescriptor? {
        allDescriptors[typeQualifiedName]?.let { return@resolveDescriptor it }

        BagDescriptor.Primitive.of(typeQualifiedName)?.let {
            allDescriptors[typeQualifiedName] = it
            return@resolveDescriptor it
        }

        resolver.getClassDeclarationByName(typeQualifiedName)
            ?.let { singlefieldParser.parse(resolver, it) }
            ?.let {
                allDescriptors[typeQualifiedName] = it
                return@resolveDescriptor it
            }

        return null
    }
}
