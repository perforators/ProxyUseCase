package io.github.perforators.internal

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

internal class FunctionVisitor(
    codeGenerator: CodeGenerator
) : KSVisitorVoid() {

    private val useCaseGenerator = UseCaseGenerator(codeGenerator)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        classDeclaration.getDeclaredFunctions()
            .filter {
                it.isPublic() && it.isMember() && it.isNotConstructor() && it.isNotExtension() && it.isNotGeneric()
            }
            .forEach { it.accept(this, data) }
    }

    private fun KSFunctionDeclaration.isMember() = functionKind == FunctionKind.MEMBER

    private fun KSFunctionDeclaration.isNotConstructor() = !isConstructor()

    private fun KSFunctionDeclaration.isNotExtension() = extensionReceiver == null

    private fun KSFunctionDeclaration.isNotGeneric() = typeParameters.isEmpty()

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        useCaseGenerator.generate(function)
    }
}
