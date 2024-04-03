package cc.unitmesh.devti.settings

import cc.unitmesh.devti.fullWidthCell
import cc.unitmesh.devti.llms.LlmFactory
import cc.unitmesh.devti.util.LLMCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.swing.JLabel

fun Panel.testConnection(project: Project?) {
    row {
        // test result
        val result = JLabel("")
        button("Test LLM") {
            if (project == null) {
                return@button
            }
            // test custom engine
            LLMCoroutineScope.scope(project).launch {
                try {
                    val flowString: Flow<String> = LlmFactory.instance.create(project).stream("hi", "")
                    flowString.collect {
                        result.text += it
                    }
                } catch (e: Exception) {
                    result.text = e.message ?: "Unknown error"
                }
            }
        }

        fullWidthCell(result)
    }
}