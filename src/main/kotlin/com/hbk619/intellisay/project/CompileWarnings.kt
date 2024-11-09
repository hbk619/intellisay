package com.hbk619.intellisay.project

import com.hbk619.intellisay.sound.sayText
import com.intellij.compiler.CompilerMessageImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.compiler.CompilerMessage

class CompileWarnings: AnAction() {
        override fun actionPerformed(event: AnActionEvent) {
            val projectState = ProjectState.instance

            if (projectState.lastCompileWarnings.isNotEmpty()) {
                sayText(event.project, "Compile warning messages", createSpeechText(projectState.lastCompileWarnings))
            } else {
                sayText(event.project, "Compile warning messages", "No compiler warnings")
            }
        }

    private fun createSpeechText(messages: List<CompilerMessage>) =
        messages.joinToString(" ") {
            val message = it as CompilerMessageImpl
            "${message.message} file ${message.virtualFile.name} line ${message.line} colum ${message.column}"
        }
}