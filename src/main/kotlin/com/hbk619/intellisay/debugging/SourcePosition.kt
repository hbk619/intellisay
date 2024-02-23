package com.hbk619.intellisay.debugging

import com.intellij.navigation.EmptyNavigatable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.xdebugger.XSourcePosition

class SourcePosition(private val editor: Editor) : XSourcePosition {
    override fun getLine(): Int {
        return editor.caretModel.logicalPosition.line
    }

    override fun getOffset(): Int {
        return 0
    }

    override fun getFile(): VirtualFile {
        return editor.virtualFile
    }

    override fun createNavigatable(project: Project): Navigatable {
        return EmptyNavigatable.INSTANCE
    }
}