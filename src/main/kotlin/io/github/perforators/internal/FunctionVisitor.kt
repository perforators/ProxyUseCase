package io.github.perforators.internal

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ksp.writeTo

internal class FunctionVisitor(
    private val codeGenerator: CodeGenerator
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        classDeclaration.getDeclaredFunctions()
            .filter {
                it.isPublicOrInternal() && it.isMember() && it.isNotConstructor()
            }
            .forEach { it.accept(this, data) }
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        UseCase(function)
            .asFileSpec()
            .writeTo(codeGenerator, Dependencies(true, function.containingFile!!))
    }
}
