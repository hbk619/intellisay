package com.hbk619.jetbrainsscreenreader

import com.hbk619.jetbrainsscreenreader.caret.CaretMoveListener
import com.hbk619.jetbrainsscreenreader.sound.say
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.progress.BackgroundTaskQueue


class ScreenReaderApp : AppLifecycleListener {
    private val log: Logger = Logger.getInstance(ScreenReaderApp::class.java)
    private val queue = BackgroundTaskQueue(null, "Intelli Say started")

    override fun appStarted() {
        class DisposableThing : Disposable {
            override fun dispose() {
                log.info("Disposed of " + ScreenReaderApp::class.java.name)
            }

        }
        EditorFactory.getInstance().eventMulticaster.addCaretListener(CaretMoveListener(), DisposableThing())

        val speech = say(null, "Intelli say started", "Intelli Say has started")
        queue.run(speech)
    }
}
