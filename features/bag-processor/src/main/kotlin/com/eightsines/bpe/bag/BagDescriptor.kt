package com.eightsines.bpe.bag

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode

sealed interface BagDescriptor {
    data class Primitive(
        val typeDescriptor: TypeDescriptor,
        val unpackMethod: String,
    ) : BagDescriptor {
        companion object {
            private val DESCRIPTORS = buildMap {
                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "Boolean"), false), "getBoolean").also { put(it.typeDescriptor, it) }
                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "Int"), false), "getInt").also { put(it.typeDescriptor, it) }
                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "String"), false), "getString").also { put(it.typeDescriptor, it) }

                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "Boolean"), true), "getBooleanOrNull").also { put(it.typeDescriptor, it) }
                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "Int"), true), "getIntOrNull").also { put(it.typeDescriptor, it) }
                Primitive(TypeDescriptor.Type(NameDescriptor("kotlin", "String"), true), "getStringOrNull").also { put(it.typeDescriptor, it) }
            }

            fun of(typeDescriptor: TypeDescriptor): Primitive? = DESCRIPTORS[typeDescriptor]
        }
    }

    data class Singlefield(
        val classDescriptor: DeclarationDescriptor,
        val fieldName: String,
        val creatorDescriptor: FunctionDescriptor,
        val shouldCheckCreatorException: Boolean,
        val primitiveDescriptor: Primitive,
    ) : BagDescriptor

    data class Stuff(
        val classDescriptor: DeclarationDescriptor,
        val isSealed: Boolean,
        val packerReference: NameDescriptorReference?,
        val unpackerReference: NameDescriptorReference?,
        val polymorphicCase: BagStuffPolymorphicCase?,
        val wares: List<BagStuffWareDescriptor>,
        val generateInfo: BagStuffGenerateInfo?,
        val sourceSymbol: KSNode,
        val sourceFile: KSFile?,
    ) : BagDescriptor
}

data class BagStuffGenerateInfo(
    val nameDescriptor: NameDescriptor,
    val shouldGeneratePacker: Boolean,
    val shouldGenerateUnpacker: Boolean,
    val isPolymorphic: Boolean,
)

data class BagStuffPolymorphicCase(
    val baseDescriptor: NameDescriptor,
    val id: Int,
)

data class BagStuffWareDescriptor(
    val fieldName: String,
    val index: Int,
    val version: Int,
    val typeDescriptor: TypeDescriptor,
    val packerDescriptor: FunctionDescriptor?,
    val unpackerDescriptor: FunctionDescriptor?,
    val fallbackValue: String?,
    val sourceClassDescriptor: DeclarationDescriptor,
    val sourceSymbol: KSNode,
)
