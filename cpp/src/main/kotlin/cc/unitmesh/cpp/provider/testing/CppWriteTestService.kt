package cc.unitmesh.cpp.provider.testing

import cc.unitmesh.cpp.util.CppContextPrettify
import cc.unitmesh.devti.context.ClassContext
import cc.unitmesh.devti.provider.WriteTestService
import cc.unitmesh.devti.provider.context.TestFileContext
import com.intellij.execution.configurations.RunProfile
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.cidr.cpp.execution.testing.CMakeTestRunConfiguration
import com.jetbrains.cidr.lang.psi.OCFunctionDeclaration

private val String.nameWithoutExtension: String
    get() {
        val lastDot = lastIndexOf('.')
        return if (lastDot == -1) this else substring(0, lastDot)
    }

// use Google Test or CATCH?
class CppWriteTestService : WriteTestService() {
    override fun runConfigurationClass(project: Project): Class<out RunProfile> = CMakeTestRunConfiguration::class.java

    override fun isApplicable(element: PsiElement): Boolean {
        return true
    }

    override fun findOrCreateTestFile(sourceFile: PsiFile, project: Project, element: PsiElement): TestFileContext? {
        // 1. check project root test folder, if not exist, create it
        val baseDir = project.guessProjectDir() ?: return null

        val sourceFilePath = sourceFile.virtualFile.name

        val testFilePath = sourceFilePath.nameWithoutExtension + "_test" + "." + sourceFile.virtualFile.extension
        val testFile = WriteAction.computeAndWait<VirtualFile?, Throwable> {
            baseDir.findOrCreateChildData(this, testFilePath)
        } ?: return null

        val currentClass = when (element) {
            is OCFunctionDeclaration -> {
                CppContextPrettify.printParentStructure(element)
            }

            else -> null
        }

        val relatedClasses = lookupRelevantClass(project, element)

        return TestFileContext(
            true,
            testFile,
            relatedClasses,
            "",
            sourceFile.language,
            currentClass,
            emptyList()
        )
    }

    override fun lookupRelevantClass(project: Project, element: PsiElement): List<ClassContext> {
        return listOf()
    }

}
