package com.hbk619.jetbrainsscreenreader

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import com.intellij.openapi.diagnostic.Logger;

class CaretMoveListener() : CaretListener {
    private val beepLock = Any()
    private val LOG: Logger = Logger.getInstance(CaretMoveListener::class.java)

    override fun caretPositionChanged(e: CaretEvent) {
        val caretModel = e.editor.caretModel
        val logicalLine = caretModel.logicalPosition.line

        val document: Document = e.editor.document
        val startOffset: Int = document.getLineStartOffset(logicalLine)
        val endOffset: Int = document.getLineEndOffset(logicalLine)

        val markupModel = DocumentMarkupModel.forDocument(document, e.editor.project, true)

        val highlighters = markupModel.allHighlighters

        for (highlighter in highlighters) {
            if (highlighter.startOffset >= startOffset && highlighter.endOffset <= endOffset && highlighter.getErrorStripeMarkColor(e.editor.colorsScheme) != null) {
                val column = caretModel.logicalPosition.column
                val caretOffset = startOffset + column
                if (highlighter.textRange.containsOffset(caretOffset)) {
                    playSound("Submarine.aiff")
                } else {
                    playSound("Frog.aiff")
                }
                LOG.info("Error on line " + (logicalLine + 1))
                break
            }
        }
    }

    fun playSound(soundName: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(beepLock) {
                try {
                    val path = CaretMoveListener::class.java.getResource("sounds/" + soundName)
                    val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(path)
                    val clip = AudioSystem.getClip()
                    clip.open(audioInputStream)
                    clip.start()
                } catch (e: Exception) {
                    LOG.error(e)
                }
            }
        }
    }
}