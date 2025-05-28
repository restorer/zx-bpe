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
        val creatorNameDescriptor: NameDescriptor,
        val shouldCheckCreatorException: Boolean,
        val primitiveDescriptor: Primitive,
    ) : BagDescriptor

    data class Stuff(
        val classDescriptor: DeclarationDescriptor,
        val staffPackerDescriptor: NameDescriptor,
        val staffUnpackerDescriptor: NameDescriptor,
        val wares: List<BagStuffWareDescriptor>,
        val generatedSimpleName: String?,
        val shouldGeneratePacker: Boolean,
        val shouldGenerateUnpacker: Boolean,
        val sourceSymbol: KSNode,
        val sourceFile: KSFile,
    ) : BagDescriptor
}

data class BagStuffWareDescriptor(
    val fieldName: String,
    val index: Int,
    val version: Int,
    val typeDescriptor: TypeDescriptor,
    val fieldPackerDescriptor: FunctionDescriptor?,
    val fieldUnpackerDescriptor: FunctionDescriptor?,
    val sourceSymbol: KSNode,
)
