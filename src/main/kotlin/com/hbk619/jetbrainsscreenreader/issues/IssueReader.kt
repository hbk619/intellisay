package com.hbk619.jetbrainsscreenreader.issues

import com.hbk619.jetbrainsscreenreader.sound.Say
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project

class IssueReader() : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Playing sound")

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project

        if (project == null) {
            queue.run(Say(null, "No project open", "No project open"))
            return
        }

        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor == null) {
            queue.run(Say(null, "No file open", "No file open"))
            return
        }


        val caretModel = editor.caretModel
        val logicalLine = caretModel.logicalPosition.line

        val document: Document = editor.document
        val startOffset: Int = document.getLineStartOffset(logicalLine)
        val endOffset: Int = document.getLineEndOffset(logicalLine)
        val markupModel = DocumentMarkupModel.forDocument(document, editor.project, true)
        val highlighters = markupModel.allHighlighters
        var foundIssue = false
        for (highlighter in highlighters) {
            if (highlighter.startOffset >= startOffset && highlighter.endOffset <= endOffset && highlighter.errorStripeTooltip != null) {
                val column = caretModel.logicalPosition.column
                val caretOffset = startOffset + column
                val tooltip = highlighter.errorStripeTooltip
                if ( tooltip !is HighlightInfo) continue

                val type = tooltip.type.getSeverity(null)
                foundIssue = when (type) {
                    HighlightSeverity.WARNING -> handleIssue(project, highlighter, caretOffset, tooltip)
                    HighlightSeverity.ERROR -> handleIssue(project, highlighter, caretOffset, tooltip)
                    else -> continue
                }
                break
            }
        }

        if (!foundIssue) {
            queue.run(Say(project, "Reading issue details", "No issues on line"))
        }
    }

    private fun handleIssue(project: Project, highlighter: RangeHighlighter, caretOffset: Int, tooltip: HighlightInfo): Boolean {
        if (highlighter.textRange.containsOffset(caretOffset)) {
            queue.run(Say(project, "Reading issue details", tooltip.description))
        } else if (highlighter.startOffset > caretOffset){
            queue.run(Say(project, "Reading issue details - not there yet", "Not on issue, go right or press f2"))
        } else if (highlighter.endOffset < caretOffset){
            queue.run(Say(project, "Reading issue details - not there yet", "Not on issue, go left"))
        } else {
            queue.run(Say(project, "Reading issue details", "Can't find issue, sorry"))
        }

        return true
    }
}