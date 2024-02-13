package com.hbk619.jetbrainsscreenreader.caret

import com.hbk619.jetbrainsscreenreader.settings.AppSettingsState
import com.hbk619.jetbrainsscreenreader.sound.Player
import com.hbk619.jetbrainsscreenreader.sound.Sound
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil

class CaretMoveListener : CaretListener {
    private val log: Logger = Logger.getInstance(CaretMoveListener::class.java)
    private var previousLine = 0
    private val queue = BackgroundTaskQueue(null, "Playing sound")

    override fun caretPositionChanged(e: CaretEvent) {
        val caretModel = e.editor.caretModel
        val logicalLine = caretModel.logicalPosition.line

        val document: Document = e.editor.document
        val position = Position(document, caretModel)

        val markupModel = DocumentMarkupModel.forDocument(document, e.editor.project, true)
        val highlighters = markupModel.allHighlighters

        val appSettings = AppSettingsState.instance
        for (highlighter in highlighters) {
            val tooltip = highlighter.errorStripeTooltip
            if ( tooltip !is HighlightInfo) continue

            if (position.isCaretWithinHighlight(highlighter)) {
                val type = tooltip.type.getSeverity(null)
                when (type) {
                    HighlightSeverity.WARNING -> handleWarning(previousLine, logicalLine, appSettings.warningsOn)
                    HighlightSeverity.ERROR -> handleError(highlighter, position, appSettings.errorsOn)
                    else -> continue
                }

                log.debug("Error on line " + (logicalLine + 1))
                break
            }
        }
        handleBreakpoints(e.editor, previousLine, logicalLine, appSettings.breakpointsOn)

        previousLine = logicalLine
    }

    private fun handleBreakpoints(editor: Editor, previousLine: Int, currentLine: Int, breakpointsOn: Boolean) {
        val project = editor.project
        if (project != null && previousLine != currentLine && breakpointsOn) {
            val breakpoints =
                XBreakpointUtil.findSelectedBreakpoint(project, editor)
            if (breakpoints.second != null) {
                playSound(Sound.BREAKPOINT)
            }
        }
    }

    private fun handleError(highlighter: RangeHighlighter, position: Position, errorsOn: Boolean) {
        if (!errorsOn) return
        if (position.isCaretOnIssue(highlighter)) {
            playSound(Sound.ERROR)
        } else {
            playSound(Sound.WARNING)
        }
    }

    private fun handleWarning(previousLine: Int, logicalLine: Int, warningsEnabled: Boolean) {
        if (previousLine != logicalLine && warningsEnabled) {
            playSound(Sound.WARNING)
        }
    }

    private fun playSound(sound: Sound) {
        queue.run(Player(null, "Playing sound", sound))
    }
}