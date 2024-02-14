package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl

const val speechTitle = "IntelliSay debugger"

class DebugListener : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return sayText(null, speechTitle,  "No project open")

        val session = XDebuggerManager.getInstance(project).currentSession
        val frame = session?.currentStackFrame ?: return sayText(null, speechTitle,  "Not debugging or on a breakpoint")

        val editor = event.getData(CommonDataKeys.EDITOR) ?: return sayText(null, speechTitle,  "Editor not focused. Press escape")
        val selection = editor.selectionModel
        val selected = selection.selectedText ?: return sayText(null, speechTitle,  "Nothing selected")

        frame.evaluator?.evaluate(
            XExpressionImpl(selected, null, null, EvaluationMode.EXPRESSION),
            EvaluationCallback(),
            SourcePosition(editor)
        ) ?: return sayText(null, speechTitle,  "No evaluator found, are you on a breakpoint?")
    }
}