package com.hbk619.intellisay.project

import com.hbk619.intellisay.sound.sayText
import com.intellij.compiler.CompilerMessageImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.compiler.CompilerMessage

class CompileErrors: AnAction() {
        override fun actionPerformed(event: AnActionEvent) {
            val projectState = ProjectState.instance

            if (projectState.lastCompileErrors.isNotEmpty()) {
                sayText(event.project, "Compile error messages", createSpeechText(projectState.lastCompileErrors))
            } else {
                sayText(event.project, "Compile error messages", "No compiler errors")
            }
        }

    private fun createSpeechText(messages: List<CompilerMessage>) =
        messages.joinToString(" ") {
            val message = it as CompilerMessageImpl
            "${message.message} file ${message.virtualFile.name} line ${message.line} colum ${message.column}"
        }
}