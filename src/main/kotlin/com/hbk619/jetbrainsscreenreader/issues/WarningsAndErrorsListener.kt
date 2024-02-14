package com.hbk619.jetbrainsscreenreader.issues

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager

data class Issue(val type: HighlightSeverity, val message: String, val line: Int)
class WarningsAndErrors: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project

        if (project == null) {
            sayText(null, "No project open", "No project open")
            return
        }

        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor == null) {
            sayText(null, "No file open", "No file open")
            return
        }

        val document: Document = editor.document

        val markupModel = DocumentMarkupModel.forDocument(document, project, true)
        val highlighters = markupModel.allHighlighters

        val issueHighlights = highlighters.filter { highlighter -> highlighter.errorStripeTooltip != null && highlighter.errorStripeTooltip is HighlightInfo }
        val properIssues: List<Issue?> = issueHighlights.map {toIssue(it, editor) }.toList()
        val grouped = properIssues.groupBy { it?.type }

        val numberOfErrors = grouped[HighlightSeverity.ERROR]?.size ?: 0
        if (numberOfErrors > 0) {
            sayText(project, "Number of errors", "$numberOfErrors errors")
        } else {
            sayText(project, "Number of errors", "No errors")
        }

        val numberOfWarnings = grouped[HighlightSeverity.WARNING]?.size ?: 0
        if (numberOfWarnings > 0) {
            sayText(project, "Number of warnings", "$numberOfWarnings warnings")
        } else {
            sayText(project, "Number of warnings", "No warnings")
        }

    }

    private fun toIssue(highlighter: RangeHighlighter, editor: Editor): Issue? {
            val tooltip = highlighter.errorStripeTooltip
            if (tooltip is HighlightInfo) {
                val type = tooltip.type.getSeverity(null)
                return when (type) {
                    HighlightSeverity.WARNING -> Issue(HighlightSeverity.WARNING, tooltip.description, editor.offsetToLogicalPosition(highlighter.startOffset).line + 1)
                    HighlightSeverity.ERROR -> Issue(HighlightSeverity.ERROR, tooltip.description, editor.offsetToLogicalPosition(highlighter.startOffset).line + 1)
                    else -> null
                }
            } else {
                return null
            }
    }
}