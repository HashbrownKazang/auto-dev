package cc.unitmesh.devti.language

import cc.unitmesh.devti.language.completion.*
import cc.unitmesh.devti.language.psi.DevInTypes
import cc.unitmesh.devti.language.psi.DevInUsed
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement

class DevInCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(DevInTypes.LANGUAGE_ID), CodeFenceLanguageProvider())
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(DevInTypes.VARIABLE_ID), CustomVariableProvider())
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(DevInTypes.COMMAND_ID), BuiltinCommandProvider())
        extend(
            CompletionType.BASIC,
            (valuePatterns(listOf(BuiltinCommand.FILE, BuiltinCommand.RUN, BuiltinCommand.WRITE))),
            FileReferenceLanguageProvider()
        )
        extend(
            CompletionType.BASIC,
            valuePattern(BuiltinCommand.REV.commandName),
            RevisionReferenceLanguageProvider()
        )
        extend(
            CompletionType.BASIC,
            valuePattern(BuiltinCommand.SYMBOL.commandName),
            SymbolReferenceLanguageProvider()
        )
    }

    private inline fun <reified I : PsiElement> psiElement(): PsiElementPattern.Capture<I> {
        return PlatformPatterns.psiElement(I::class.java)
    }

    private fun baseUsedPattern(): PsiElementPattern.Capture<PsiElement> =
        PlatformPatterns.psiElement()
            .inside(psiElement<DevInUsed>())

    private fun valuePattern(text: String): PsiElementPattern.Capture<PsiElement> =
        baseUsedPattern()
            .withElementType(DevInTypes.COMMAND_PROP)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(DevInTypes.COLON),
                PlatformPatterns.psiElement().withText(text)
            )


    private fun valuePatterns(listOf: List<BuiltinCommand>): ElementPattern<out PsiElement> {
        val patterns = listOf.map { valuePattern(it.commandName) }
        return PlatformPatterns.or(*patterns.toTypedArray())
    }
}
