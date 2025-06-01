package com.eightsines.bpe.bag

sealed interface NameDescriptorReference {
    data class Strict(val packageName: String, val simpleName: String) : NameDescriptorReference {
        override fun toString(): String = "$packageName/$simpleName"
    }

    data class Unresolved(val currentPackageName: String, val className: String) : NameDescriptorReference {
        override fun toString(): String = className
    }
}

data class NameDescriptor(val packageName: String, val simpleName: String) {
    val qualifiedName: String = "$packageName.$simpleName"
    override fun toString(): String = "$packageName/$simpleName"
}

data class DeclarationDescriptor(
    val nameDescriptor: NameDescriptor,
    val numTypeParameters: Int = 0,
) {
    override fun toString(): String = buildString {
        append(nameDescriptor.toString())

        if (numTypeParameters > 0) {
            append('<')

            repeat(numTypeParameters) {
                append('*')

                if (it > 0) {
                    append(',')
                }
            }

            append('>')
        }
    }
}

sealed interface TypeDescriptor {
    val rawTypeDescriptor: TypeDescriptor

    data object Star : TypeDescriptor {
        override val rawTypeDescriptor: TypeDescriptor = this
        override fun toString(): String = "*"
    }

    data class Type(
        val nameDescriptor: NameDescriptor,
        val isNullable: Boolean,
        val typeParameterDescriptors: List<TypeDescriptor> = emptyList(),
    ) : TypeDescriptor {
        override val rawTypeDescriptor: TypeDescriptor =
            if (typeParameterDescriptors.isEmpty()) this else copy(typeParameterDescriptors = emptyList())

        override fun toString(): String = buildString {
            append(nameDescriptor.toString())

            if (typeParameterDescriptors.isNotEmpty()) {
                append('<')

                typeParameterDescriptors.forEachIndexed { index, typeDescriptor ->
                    append(typeDescriptor.toString())

                    if (index > 0) {
                        append(',')
                    }
                }

                append('>')
            }

            if (isNullable) {
                append('?')
            }
        }
    }
}

data class FunctionParameterDescriptor(val name: String, val typeDescriptor: TypeDescriptor)

data class FunctionDescriptor(
    val nameDescriptor: NameDescriptor,
    val returnTypeDescriptor: TypeDescriptor?,
    val parameters: List<FunctionParameterDescriptor>,
) {
    override fun toString(): String = nameDescriptor.toString()
}

fun DeclarationDescriptor.asRawTypeDescriptor(): TypeDescriptor =
    TypeDescriptor.Type(nameDescriptor, false)
