package com.hbk619.intellisay.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerMessage

class ProjectState {
    var lastCompileErrors: List<CompilerMessage> = listOf()
    var lastCompileWarnings: List<CompilerMessage> = listOf()

    companion object {
        val instance: ProjectState
            get() = ApplicationManager.getApplication().getService(ProjectState::class.java)
    }

}