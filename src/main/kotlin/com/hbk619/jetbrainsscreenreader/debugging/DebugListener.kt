package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.SpeechQueue
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl


class DebugListener : AnAction() {
    private val queue = SpeechQueue(null, "Evaluating variable")
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return queue.say("No project open")

        val session = XDebuggerManager.getInstance(project).currentSession
        val frame = session?.currentStackFrame ?: return queue.say("Not debugging or on a breakpoint")

        val editor = event.getData(CommonDataKeys.EDITOR) ?: return queue.say("Editor not focused. Press escape")
        val selection = editor.selectionModel
        val selected = selection.selectedText ?: return queue.say("Nothing selected")

        frame.evaluator?.evaluate(
            XExpressionImpl(selected, null, null, EvaluationMode.EXPRESSION),
            EvaluationCallback(queue),
            SourcePosition(editor)
        ) ?: return queue.say("No evaluator found, are you on a breakpoint?")
    }
}