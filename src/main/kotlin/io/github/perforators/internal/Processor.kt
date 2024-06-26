package io.github.perforators.internal

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

internal class Processor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GENERATE_USE_CASE_ANNOTATION)
        val (valid, invalid) = symbols.partition { it.validate() }
        valid.filterIsInstance<KSClassDeclaration>().forEach {
            require(it.isPublicOrInternal()) {
                "Class with annotation $GENERATE_USE_CASE_ANNOTATION " +
                    "must be public or internal, but ${it.simpleName.asString()} not!"
            }
            it.accept(FunctionVisitor(codeGenerator), Unit)
        }
        return invalid
    }

    companion object {
        private const val GENERATE_USE_CASE_ANNOTATION = "io.github.perforators.GenerateUseCases"
    }
}
