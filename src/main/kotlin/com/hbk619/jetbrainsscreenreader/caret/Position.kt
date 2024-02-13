package com.hbk619.jetbrainsscreenreader.caret

import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.RangeHighlighter


class Position(document: Document, caretModel: CaretModel) {
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var caretOffset: Int = 0

    init {
        val logicalLine = caretModel.logicalPosition.line
        this.startOffset = document.getLineStartOffset(logicalLine)
        this.endOffset = document.getLineEndOffset(logicalLine)
        this.caretOffset = caretModel.logicalPosition.column + startOffset
    }

    fun isCaretWithinHighlight(highlighter: RangeHighlighter): Boolean {
        val onLine = highlighter.startOffset >= startOffset && highlighter.endOffset <= endOffset
        return onLine || highlighter.textRange.containsOffset(caretOffset)
    }

    fun isCaretBeforeIssue(highlighter: RangeHighlighter): Boolean {
        return highlighter.startOffset > caretOffset
    }

    fun isCaretAfterIssue(highlighter: RangeHighlighter): Boolean {
        return highlighter.endOffset < caretOffset
    }

    fun isCaretOnIssue(highlighter: RangeHighlighter): Boolean {
        return highlighter.textRange.containsOffset(caretOffset)
    }
}