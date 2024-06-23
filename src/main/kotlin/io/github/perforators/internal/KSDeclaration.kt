package io.github.perforators.internal

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun KSDeclaration.typeVariables(
    typeParameterResolver: TypeParameterResolver
): List<TypeVariableName> = typeParameters.map { it.toTypeVariableName(typeParameterResolver) }

internal fun KSDeclaration.isPublicOrInternal() = isPublic() || isInternal()

internal fun KSClassDeclaration.toTypeName(
    typeParameterResolver: TypeParameterResolver
): TypeName = toClassName().parameterizedBy(typeVariables(typeParameterResolver))

internal fun KSFunctionDeclaration.isMember() = functionKind == FunctionKind.MEMBER

internal fun KSFunctionDeclaration.isNotConstructor() = !isConstructor()

internal fun KSFunctionDeclaration.isSuspend() = modifiers.contains(Modifier.SUSPEND)

internal val KSFunctionDeclaration.argumentsNames
    get() = parameters.joinToString(separator = ", ") { "${it.name?.asString()}" }
