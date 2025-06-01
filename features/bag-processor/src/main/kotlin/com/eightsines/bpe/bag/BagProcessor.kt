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

class BagProcessor(logger: KSPLogger, codeGenerator: CodeGenerator) : SymbolProcessor {
    private val stuffParser = BagStuffParser(logger)
    private val stuffGenerator = BagStuffGenerator(logger, codeGenerator)
    private val singlefieldParser = BagSinglefieldParser(logger)

    private val allDescriptors = mutableMapOf<TypeDescriptor, BagDescriptor>()
    private val candidatesStuffMap = mutableMapOf<String, BagDescriptor.Stuff>()
    private val polymorphicOfStuffMap = mutableMapOf<NameDescriptor, MutableList<BagDescriptor.Stuff>>()
    private val alreadyGeneratedStuffs = mutableSetOf<BagDescriptor.Stuff>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val generatorResolver = object : BagStuffGenerator.Resolver {
            override fun resolveName(nameReference: NameDescriptorReference): NameDescriptor? = resolveName(resolver, nameReference)
            override fun resolveType(typeDescriptor: TypeDescriptor): BagDescriptor? = resolveType(resolver, typeDescriptor)

            override fun resolvePolymorphicCases(nameDescriptor: NameDescriptor) =
                this@BagProcessor.resolvePolymorphicCases(nameDescriptor)
        }

        for (descriptor in stuffParser.parseAll(resolver)) {
            allDescriptors[descriptor.classDescriptor.asRawTypeDescriptor()] = descriptor

            if (descriptor.generateInfo != null) {
                candidatesStuffMap[descriptor.generateInfo.nameDescriptor.qualifiedName] = descriptor
            }

            if (descriptor.polymorphicCase != null) {
                polymorphicOfStuffMap
                    .computeIfAbsent(descriptor.polymorphicCase.baseDescriptor) { mutableListOf() }
                    .add(descriptor)
            }
        }

        for (descriptor in candidatesStuffMap.values) {
            if (alreadyGeneratedStuffs.contains(descriptor)) {
                continue
            }

            if (stuffGenerator.generate(descriptor, generatorResolver)) {
                alreadyGeneratedStuffs.add(descriptor)
            }
        }

        return emptyList()
    }

    private fun resolveName(resolver: Resolver, nameReference: NameDescriptorReference): NameDescriptor? = when (nameReference) {
        is NameDescriptorReference.Strict ->
            NameDescriptor(nameReference.packageName, nameReference.simpleName)

        is NameDescriptorReference.Unresolved ->
            resolver.getClassDescriptorByName(nameReference.currentPackageName, nameReference.className)?.nameDescriptor
                ?: candidatesStuffMap[nameReference.className]?.generateInfo?.nameDescriptor
                ?: candidatesStuffMap["${nameReference.currentPackageName}.${nameReference.className}"]?.generateInfo?.nameDescriptor
    }

    private fun resolveType(resolver: Resolver, typeDescriptor: TypeDescriptor): BagDescriptor? {
        val typeDescriptor = typeDescriptor.rawTypeDescriptor as? TypeDescriptor.Type ?: return null

        allDescriptors[typeDescriptor]?.let { return it }

        BagDescriptor.Primitive.of(typeDescriptor)?.let {
            allDescriptors[typeDescriptor] = it
            return it
        }

        resolver.getClassDeclarationByName(typeDescriptor.nameDescriptor.qualifiedName)
            ?.let { singlefieldParser.parse(resolver, it) }
            ?.let {
                allDescriptors[typeDescriptor] = it
                return it
            }

        return null
    }

    private fun resolvePolymorphicCases(nameDescriptor: NameDescriptor): List<BagDescriptor.Stuff> =
        polymorphicOfStuffMap[nameDescriptor]?.toList() ?: emptyList()
}
