package com.eightsines.bpe.bag

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode

sealed interface BagDescriptor {
    data class Primitive(
        val typeQualifiedName: String,
        val unpackMethod: String,
    ) : BagDescriptor {
        companion object {
            private val DESCRIPTORS = buildMap {
                Primitive("kotlin.Boolean", "getBoolean").also { put(it.typeQualifiedName, it) }
                Primitive("kotlin.Int", "getInt").also { put(it.typeQualifiedName, it) }
                Primitive("kotlin.String", "getString").also { put(it.typeQualifiedName, it) }

                Primitive("kotlin.Boolean?", "getBooleanOrNull").also { put(it.typeQualifiedName, it) }
                Primitive("kotlin.Int?", "getIntOrNull").also { put(it.typeQualifiedName, it) }
                Primitive("kotlin.String?", "getStringOrNull").also { put(it.typeQualifiedName, it) }
            }

            fun of(typeQualifiedName: String): Primitive? = DESCRIPTORS[typeQualifiedName]
        }
    }

    data class Singlefield(
        val classQualifiedName: String,
        val fieldName: String,
        val creatorQualifiedName: String,
        val shouldCheckCreatorException: Boolean,
        val bagPrimitiveDescriptor: Primitive,
    ) : BagDescriptor

    data class Stuff(
        val classQualifiedName: String,
        val classPackageName: String,
        val classSimpleName: String,
        val numClassTypeParameters: Int,
        val sourceSymbol: KSNode,
        val sourceFile: KSFile,
        val packerQualifiedName: String,
        val unpackerQualifiedName: String,
        val wares: List<BagStuffWareDescriptor>,
        val generatedSimpleName: String?,
        val shouldGeneratePacker: Boolean,
        val shouldGenerateUnpacker: Boolean,
    ) : BagDescriptor
}

data class BagStuffWareDescriptor(
    val parameterName: String,
    val sourceSymbol: KSNode,
    val index: Int,
    val version: Int,
    val typeQualifiedName: String,
    val packerQualifiedName: String?,
    val unpackerQualifiedName: String?,
)
