package io.github.perforators.internal

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStreamWriter

class UseCaseGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generate(function: KSFunctionDeclaration) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(true, function.containingFile!!),
            packageName = function.packageName(),
            fileName = function.className()
        ).use {
            it.writer().use { writer -> writer.generate(function) }
        }
    }

    private fun OutputStreamWriter.generate(function: KSFunctionDeclaration) {
        appendPackage(function)
        appendImports()
        appendClassDeclaration(function) {
            appendFunctionDeclaration(function) {
                appendFunctionBody(function)
            }
        }
    }

    private fun OutputStreamWriter.appendPackage(function: KSFunctionDeclaration) {
        val packageName = function.packageName()
        if (packageName.isNotEmpty()) {
            append("$packageName\n\n")
        }
    }

    private fun OutputStreamWriter.appendImports() {
        append("import javax.inject.Inject\n\n")
    }

    private fun OutputStreamWriter.appendClassDeclaration(
        function: KSFunctionDeclaration,
        appendContent: () -> Unit
    ) {
        val className = function.className()
        val parent = function.parentDeclaration as KSClassDeclaration
        append("class $className @Inject constructor(private val target: ${parent.simpleName.asString()}) {\n")
        appendContent()
        append("}\n")
    }

    private fun OutputStreamWriter.appendFunctionDeclaration(
        function: KSFunctionDeclaration,
        appendBody: () -> Unit
    ) {
        val suspend = if (function.isSuspend()) "suspend " else ""
        val arguments = function.arguments()
        val resultType = function.returnType!!.resolve().declaration.simpleName.asString()
        append("\t${suspend}operator fun invoke($arguments): $resultType {\n")
        appendBody()
        append("\t}\n")
    }

    private fun OutputStreamWriter.appendFunctionBody(function: KSFunctionDeclaration) {
        append("\t\treturn target.${function.simpleName.asString()}(${function.argumentsNames()})\n")
    }

    private fun KSFunctionDeclaration.arguments() = parameters.joinToString(separator = ", ") {
        "${it.name?.asString()}: ${it.type.resolve().declaration.simpleName.asString()}"
    }

    private fun KSFunctionDeclaration.argumentsNames() = parameters.joinToString(separator = ", ") {
        "${it.name?.asString()}"
    }

    private fun KSFunctionDeclaration.packageName() =
        (parentDeclaration as KSClassDeclaration).containingFile?.packageName?.asString() ?: ""

    private fun KSFunctionDeclaration.className() =
        "${simpleName.asString().capitalize()}UseCase"

    private fun KSFunctionDeclaration.isSuspend() = modifiers.contains(Modifier.SUSPEND)

    private fun String.capitalize() = replaceFirstChar {
        if (it.isLowerCase()) it.uppercaseChar() else it
    }
}
