package io.github.perforators.internal

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

internal class UseCase(private val targetFunction: KSFunctionDeclaration) {

    private val classDeclaration: KSClassDeclaration =
        targetFunction.parentDeclaration as KSClassDeclaration

    private val packageName: String =
        classDeclaration.containingFile?.packageName?.asString() ?: ""

    private val name: String =
        "${targetFunction.simpleName.asString().capitalize()}UseCase"

    private val classResolver = classDeclaration.typeParameters.toTypeParameterResolver()

    private val functionResolver = targetFunction.typeParameters.toTypeParameterResolver(classResolver)

    fun asFileSpec(): FileSpec = FileSpec.builder(packageName, name)
        .addType(createClass())
        .build()

    private fun createClass() = TypeSpec.classBuilder(name)
        .addModifiers(defineClassModifiers())
        .addTypeVariables(classDeclaration.typeVariables(classResolver))
        .primaryConstructor(createPrimaryConstructor())
        .addProperty(
            PropertySpec.builder(TARGET_PROPERTY_NAME, classDeclaration.toTypeName(classResolver))
                .initializer(TARGET_PROPERTY_NAME)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addFunction(createFunction())
        .build()

    private fun defineClassModifiers() = buildList {
        classDeclaration.getVisibility().toKModifier()?.let(::add)
    }

    private fun createPrimaryConstructor() = FunSpec.constructorBuilder()
        .addAnnotation(ClassName("javax.inject", "Inject"))
        .addParameter(TARGET_PROPERTY_NAME, classDeclaration.toTypeName(classResolver))
        .build()

    private fun createFunction() = FunSpec.builder("invoke")
        .addModifiers(defineFunctionModifiers())
        .addReceiverIfExist()
        .addTypeVariables(targetFunction.typeVariables(functionResolver))
        .addParameters(defineFunctionParameters())
        .returns(targetFunction.returnType!!.toTypeName(functionResolver))
        .beginControlFlow("return with(target)")
        .addStatement("${targetFunction.simpleName.asString()}(${targetFunction.argumentsNames})")
        .endControlFlow()
        .build()

    private fun defineFunctionModifiers() = buildList {
        add(KModifier.OPERATOR)
        if (targetFunction.isSuspend()) {
            add(KModifier.SUSPEND)
        }
        targetFunction.getVisibility().toKModifier()?.let(::add)
    }

    private fun FunSpec.Builder.addReceiverIfExist(): FunSpec.Builder {
        targetFunction.extensionReceiver?.let {
            receiver(it.toTypeName(functionResolver))
        }
        return this
    }

    private fun defineFunctionParameters() = targetFunction.parameters.map {
        val typeName = it.type.toTypeName(functionResolver)
        ParameterSpec.builder(it.name?.asString() ?: "", typeName).build()
    }

    private fun String.capitalize() = replaceFirstChar {
        if (it.isLowerCase()) it.uppercaseChar() else it
    }

    companion object {
        private const val TARGET_PROPERTY_NAME = "target"
    }
}
