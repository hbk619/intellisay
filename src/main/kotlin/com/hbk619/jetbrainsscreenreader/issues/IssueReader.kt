package com.hbk619.jetbrainsscreenreader.issues

import com.hbk619.jetbrainsscreenreader.caret.Position
import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class IssueReader : AnAction() {
    
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

        val caretModel = editor.caretModel
        val document: Document = editor.document
        val position = Position(document, caretModel)

        val markupModel = DocumentMarkupModel.forDocument(document, editor.project, true)
        val highlighters = markupModel.allHighlighters
        var foundIssue = false

        for (highlighter in highlighters) {
            val tooltip = highlighter.errorStripeTooltip
            if ( tooltip !is HighlightInfo) continue

            if (position.isCaretWithinHighlight(highlighter)) {
                val type = tooltip.type.getSeverity(null)
                foundIssue = when (type) {
                    HighlightSeverity.WARNING -> handleIssue(project, highlighter, position, tooltip)
                    HighlightSeverity.ERROR -> handleIssue(project, highlighter, position, tooltip)
                    else -> continue
                }
                break
            }
        }

        if (!foundIssue) {
            sayText(project, "Reading issue details", "No issues on line")
        }
    }

    private fun handleIssue(project: Project, highlighter: RangeHighlighter, position: Position, tooltip: HighlightInfo): Boolean {
        if (position.isCaretOnIssue(highlighter)) {
            sayText(project, "Reading issue details", tooltip.description)
        } else if (position.isCaretBeforeIssue(highlighter)){
            sayText(project, "Reading issue details - not there yet", "Not on issue, go right or press f2")
        } else if (position.isCaretAfterIssue(highlighter)){
            sayText(project, "Reading issue details - not there yet", "Not on issue, go left")
        } else {
            sayText(project, "Reading issue details", "Can't find issue, sorry")
        }

        return true
    }
}